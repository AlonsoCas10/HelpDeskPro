#!/bin/bash
# Colocar en la raíz del repo (hermano de helpdeskpro-db, helpdeskpro-backend, helpdeskpro-frontend).
# Uso: chmod +x start-all.sh   luego   ./start-all.sh
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
mkdir -p "$ROOT_DIR/logs"

echo "== 1/3: Base de datos =="
cd "$ROOT_DIR/helpdeskpro-db"
docker compose up -d

if [ ! -f .env ]; then
  echo "Falta helpdeskpro-db/.env. Copia .env.example a .env y complétalo antes de continuar."
  exit 1
fi
set -a; source .env; set +a

echo "Esperando a que SQL Server esté listo..."
until docker exec helpdeskpro-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "SELECT 1" > /dev/null 2>&1; do
  sleep 2
  echo "  ...todavía iniciando"
done

echo "Verificando/creando base y usuario (seguro correrlo aunque ya exista)..."
./setup-db.sh

echo "== 2/3: Backend (Spring Boot) =="
cd "$ROOT_DIR/helpdeskpro-backend"
nohup ./mvnw spring-boot:run > "$ROOT_DIR/logs/backend.log" 2>&1 &
echo $! > "$ROOT_DIR/logs/backend.pid"
echo "Backend en segundo plano (PID $(cat "$ROOT_DIR/logs/backend.pid")) — log: logs/backend.log"

echo "== 3/3: Frontend (React/Vite) =="
cd "$ROOT_DIR/helpdeskpro-frontend"
nohup npm run dev > "$ROOT_DIR/logs/frontend.log" 2>&1 &
echo $! > "$ROOT_DIR/logs/frontend.pid"
echo "Frontend en segundo plano (PID $(cat "$ROOT_DIR/logs/frontend.pid")) — log: logs/frontend.log"

echo ""
echo "Todo arrancando. Para ver logs en vivo:"
echo "  tail -f $ROOT_DIR/logs/backend.log"
echo "  tail -f $ROOT_DIR/logs/frontend.log"
echo ""
echo "Para detener backend y frontend:"
echo "  kill \$(cat $ROOT_DIR/logs/backend.pid) \$(cat $ROOT_DIR/logs/frontend.pid)"
echo "Para detener la base de datos:"
echo "  cd $ROOT_DIR/helpdeskpro-db && docker compose down"
