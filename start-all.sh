#!/bin/bash
# Colocar en la raíz del repo (hermano de helpdeskpro-db, helpdeskpro-backend, helpdeskpro-frontend).
# Uso: chmod +x start-all.sh   luego   ./start-all.sh
# Para detener: ./stop-all.sh   (o ./stop-all.sh --db para bajar también la base)
#
# Principios de este script:
#   1. Idempotente: se puede correr dos veces sin duplicar servicios (revisa el puerto antes de arrancar).
#   2. Reversible: guarda el ID del grupo de procesos de cada servicio para que stop-all.sh mate exactamente eso.
#   3. Honesto: valida la configuración ANTES de arrancar nada y, al final, verifica que cada servicio responda.
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

# Puertos en un solo lugar. Si cambian, se cambian aquí (y en vite.config.js / application.properties).
BACKEND_PORT=8080
FRONTEND_PORT=5173

# ---------------------------------------------------------------------------
# Funciones auxiliares
# ---------------------------------------------------------------------------

# ¿Hay algo escuchando en este puerto TCP?
# ss -l (listening) -t (TCP) -n (números, no nombres). El espacio después del puerto
# evita falsos positivos (":8080" no debe coincidir con ":80800").
port_in_use() {
  ss -ltn | grep -q ":$1 "
}

# ¿El archivo (o el entorno) define una variable con valor no vacío?
# Spring y Vite aceptan la variable tanto en el archivo como exportada en la shell,
# así que revisamos ambos lugares.
has_var() {
  local file="$1" var="$2"
  [ -n "${!var}" ] && return 0                      # ${!var} = valor de la variable cuyo NOMBRE está en $var
  [ -f "$file" ] && grep -qE "^${var}=.+" "$file"   # línea "VAR=algo" (no vacía)
}

# Espera hasta que un servicio abra su puerto, o falla con un mensaje útil.
# Además vigila que el proceso siga vivo: si muere, no tiene sentido esperar el timeout completo.
wait_for_port() {
  local name="$1" port="$2" timeout="$3" pid="$4" log="$5"
  local waited=0
  while ! port_in_use "$port"; do
    # kill -0 no envía ninguna señal: sólo pregunta "¿existe este proceso?"
    if ! kill -0 "$pid" 2>/dev/null; then
      echo "  ✗ $name terminó antes de abrir el puerto $port. Últimas líneas de $log:"
      tail -n 20 "$log" | sed 's/^/      /'
      exit 1
    fi
    if [ "$waited" -ge "$timeout" ]; then
      echo "  ✗ $name no abrió el puerto $port en ${timeout}s. Revisa: tail -f $log"
      exit 1
    fi
    sleep 2
    waited=$((waited + 2))
  done
  echo "  ✓ $name escuchando en el puerto $port (${waited}s)"
}

# Arranca un comando en segundo plano dentro de su PROPIO grupo de procesos.
#   - setsid crea una sesión/grupo nuevo; su PID pasa a ser el ID del grupo (PGID).
#     Así, "npm → sh -c vite → node" quedan todos en el mismo grupo y stop-all.sh
#     puede matarlos juntos con `kill -- -PGID`. Antes se guardaba sólo el PID de npm
#     y matar ese PID dejaba vivo al hijo que realmente tenía el puerto.
#   - nohup + redirección: el proceso sigue vivo aunque cierres la terminal, y su salida va al log.
# Deja el PID en la variable global STARTED_PID (una función de bash no puede "devolver" un número
# grande con return, y usar $(...) correría la función en una subshell y perderíamos el proceso).
start_in_group() {
  local log="$1"; shift
  setsid nohup "$@" > "$log" 2>&1 &
  STARTED_PID=$!
}

# ---------------------------------------------------------------------------
# 0. Validación previa: revisamos TODO antes de arrancar NADA.
#    Juntamos todos los errores y los mostramos de una vez, en lugar de fallar en
#    el primero y obligarte a correr el script una y otra vez para descubrir el siguiente.
# ---------------------------------------------------------------------------
echo "== 0/3: Verificando configuración =="
errors=()

DB_ENV="$ROOT_DIR/helpdeskpro-db/.env"
if [ ! -f "$DB_ENV" ]; then
  errors+=("Falta helpdeskpro-db/.env (copia .env.example a .env y complétalo).")
fi

# El backend lee helpdeskpro-backend/.env gracias a spring.config.import en application.properties.
# SecurityConfig.java usa @Value("${COGNITO_...}") SIN valor por defecto: si falta, Spring no arranca.
BACKEND_ENV="$ROOT_DIR/helpdeskpro-backend/.env"
for var in COGNITO_JWK_SET_URI COGNITO_ISSUER_URI COGNITO_CLIENT_ID; do
  has_var "$BACKEND_ENV" "$var" || errors+=("Backend: falta $var en helpdeskpro-backend/.env")
done

# Vite sólo carga archivos .env* al ARRANCAR, y sólo expone al navegador variables que empiezan con VITE_.
# Una variable faltante no da error: queda como undefined y la app falla después, lejos de la causa
# (ej. "No authority or metadataUrl configured"). Por eso la revisamos aquí.
FRONTEND_ENV="$ROOT_DIR/helpdeskpro-frontend/.env.local"
for var in VITE_API_URL VITE_COGNITO_AUTHORITY VITE_COGNITO_CLIENT_ID VITE_COGNITO_REDIRECT_URI \
           VITE_COGNITO_DOMAIN VITE_COGNITO_LOGOUT_URI; do
  has_var "$FRONTEND_ENV" "$var" || errors+=("Frontend: falta $var en helpdeskpro-frontend/.env.local")
done

if [ ! -d "$ROOT_DIR/helpdeskpro-frontend/node_modules" ]; then
  errors+=("Frontend: no hay node_modules. Corre 'npm install' en helpdeskpro-frontend.")
fi

if [ ${#errors[@]} -gt 0 ]; then
  echo "No se arrancó nada. Corrige lo siguiente:"
  printf '  - %s\n' "${errors[@]}"
  exit 1
fi
echo "  ✓ Configuración completa"

# ---------------------------------------------------------------------------
# 1. Base de datos
# ---------------------------------------------------------------------------
echo "== 1/3: Base de datos =="
cd "$ROOT_DIR/helpdeskpro-db"
# Ya validamos .env arriba, así que ahora sí es seguro levantar el contenedor.
# (Antes se levantaba primero y se validaba después: el contenedor arrancaba sin contraseña.)
# docker compose up -d ya es idempotente: si el contenedor está corriendo, no hace nada.
docker compose up -d
set -a; source .env; set +a   # set -a: exporta automáticamente todo lo que se defina mientras está activo

echo "Esperando a que SQL Server esté listo..."
waited=0
until docker exec helpdeskpro-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "SELECT 1" > /dev/null 2>&1; do
  # Límite de tiempo: sin esto, una contraseña incorrecta dejaba el script esperando para siempre.
  if [ "$waited" -ge 90 ]; then
    echo "  ✗ SQL Server no respondió en 90s. Revisa: docker logs helpdeskpro-sqlserver"
    exit 1
  fi
  sleep 2
  waited=$((waited + 2))
  echo "  ...todavía iniciando"
done

echo "Verificando/creando base y usuario (seguro correrlo aunque ya exista)..."
./setup-db.sh

# ---------------------------------------------------------------------------
# 2. Backend
# ---------------------------------------------------------------------------
echo "== 2/3: Backend (Spring Boot) =="
if port_in_use "$BACKEND_PORT"; then
  # No arrancamos un segundo backend: no podría usar el puerto y moriría (y sólo te enterarías leyendo el log).
  echo "  • Ya hay algo escuchando en $BACKEND_PORT; no se arranca otro. (Para reiniciar: ./stop-all.sh)"
else
  cd "$ROOT_DIR/helpdeskpro-backend"
  start_in_group "$LOG_DIR/backend.log" ./mvnw spring-boot:run
  echo "$STARTED_PID" > "$LOG_DIR/backend.pid"
  echo "  Arrancando (grupo $STARTED_PID) — log: logs/backend.log"
  # Maven + Spring tardan bastante la primera vez (descarga dependencias), por eso 180s.
  wait_for_port "Backend" "$BACKEND_PORT" 180 "$STARTED_PID" "$LOG_DIR/backend.log"
fi

# ---------------------------------------------------------------------------
# 3. Frontend
# ---------------------------------------------------------------------------
echo "== 3/3: Frontend (React/Vite) =="
if port_in_use "$FRONTEND_PORT"; then
  # Sin esta revisión, Vite saltaba en silencio a 5174 y quedaban dos servidores.
  # (vite.config.js ahora también tiene strictPort: true como segunda protección.)
  echo "  • Ya hay algo escuchando en $FRONTEND_PORT; no se arranca otro. (Para reiniciar: ./stop-all.sh)"
else
  cd "$ROOT_DIR/helpdeskpro-frontend"
  start_in_group "$LOG_DIR/frontend.log" npm run dev
  echo "$STARTED_PID" > "$LOG_DIR/frontend.pid"
  echo "  Arrancando (grupo $STARTED_PID) — log: logs/frontend.log"
  wait_for_port "Frontend" "$FRONTEND_PORT" 60 "$STARTED_PID" "$LOG_DIR/frontend.log"
fi

echo ""
echo "Todo arriba:"
echo "  Frontend: http://localhost:$FRONTEND_PORT"
echo "  Backend:  http://localhost:$BACKEND_PORT"
echo "Logs en vivo:"
echo "  tail -f $LOG_DIR/backend.log"
echo "  tail -f $LOG_DIR/frontend.log"
echo "Para detener: ./stop-all.sh   (agrega --db para bajar también la base de datos)"
