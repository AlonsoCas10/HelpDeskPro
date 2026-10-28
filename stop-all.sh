#!/bin/bash
# Detiene lo que arrancó start-all.sh.
# Uso: ./stop-all.sh          → detiene backend y frontend (la base sigue corriendo)
#      ./stop-all.sh --db     → además baja el contenedor de la base de datos
#
# Por qué la base queda corriendo por defecto: tarda en arrancar, no cambia entre
# reinicios del código y sus datos persisten en un volumen. Normalmente sólo quieres
# reiniciar backend/frontend.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"

BACKEND_PORT=8080
FRONTEND_PORT=5173

# Detiene un servicio usando el ID de grupo guardado por start-all.sh.
stop_service() {
  local name="$1" pidfile="$2" port="$3"

  if [ -f "$pidfile" ]; then
    local pgid
    pgid="$(cat "$pidfile")"

    # "kill -0 -- -PGID": el "-" delante del número significa "el GRUPO", no un proceso.
    # El "--" separa las opciones de kill de los argumentos, para que "-1234" no se lea como opción.
    if kill -0 -- "-$pgid" 2>/dev/null; then
      echo "Deteniendo $name (grupo $pgid)..."

      # Primero SIGTERM: le pedimos que se cierre ordenadamente
      # (Spring cierra conexiones a la base, Vite libera el puerto, etc.).
      kill -TERM -- "-$pgid"

      # Le damos hasta 15s para terminar por su cuenta.
      for _ in $(seq 1 15); do
        kill -0 -- "-$pgid" 2>/dev/null || break
        sleep 1
      done

      # Si sigue vivo, recién ahí SIGKILL (no se puede ignorar, pero tampoco permite limpiar nada).
      if kill -0 -- "-$pgid" 2>/dev/null; then
        echo "  No respondió a SIGTERM en 15s; forzando con SIGKILL."
        kill -KILL -- "-$pgid"
      fi
      echo "  ✓ $name detenido"
    else
      echo "$name: el grupo $pgid ya no existe (ya estaba detenido)."
    fi
    rm -f "$pidfile"
  else
    echo "$name: no hay $pidfile (no lo arrancó start-all.sh, o ya se detuvo)."
  fi

  # Verificación final: ¿quedó algo en el puerto?
  # NO lo matamos automáticamente: puede ser algo que arrancaste a mano en otra terminal
  # o un proceso que no tiene nada que ver. Te mostramos qué es y tú decides.
  if ss -ltn | grep -q ":$port "; then
    echo "  ⚠ Todavía hay algo escuchando en $port, que este script no arrancó:"
    ss -ltnp | grep ":$port " | sed 's/^/      /'
    echo "    Si es tuyo y quieres detenerlo: fuser -k -TERM $port/tcp"
    echo "    (sin sudo: tus procesos los puedes matar como tu usuario;"
    echo "     y -TERM porque fuser -k por defecto manda SIGKILL)"
  fi
}

stop_service "Backend"  "$LOG_DIR/backend.pid"  "$BACKEND_PORT"
stop_service "Frontend" "$LOG_DIR/frontend.pid" "$FRONTEND_PORT"

if [ "$1" = "--db" ]; then
  echo "Bajando la base de datos..."
  # "down" elimina el contenedor pero NO el volumen: los datos se conservan.
  # (Para borrar también los datos sería "down -v". No lo hacemos aquí a propósito.)
  (cd "$ROOT_DIR/helpdeskpro-db" && docker compose down)
fi
