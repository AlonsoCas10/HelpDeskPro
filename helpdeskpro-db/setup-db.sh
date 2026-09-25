#!/bin/bash
# Ejecutar UNA sola vez después de "docker compose up -d".
set -e

if [ ! -f .env ]; then
  echo "Falta el archivo .env. Copia .env.example a .env y coloca los valores reales."
  exit 1
fi

# Carga las variables de .env al entorno de este script
set -a
source .env
set +a

CONTAINER="helpdeskpro-sqlserver"
SQLCMD="/opt/mssql-tools18/bin/sqlcmd"

echo "Esperando a que SQL Server esté listo dentro del contenedor..."
until docker exec "$CONTAINER" $SQLCMD -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C -Q "SELECT 1" > /dev/null 2>&1; do
  sleep 2
  echo "  ...todavía iniciando"
done

echo "Generando init.sql a partir de la plantilla (sin dejar secretos en el repo)..."
envsubst < init/init.sql.template > init/init.generated.sql

echo "Ejecutando el script generado..."
docker exec -i "$CONTAINER" $SQLCMD -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -C < init/init.generated.sql

rm init/init.generated.sql   # nunca dejamos el archivo con la contraseña real en disco

echo "Listo. $DB_NAME y el usuario $DB_USER quedaron creados (o ya existían)."
