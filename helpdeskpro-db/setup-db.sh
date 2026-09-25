#!/bin/bash
# Ejecutar UNA sola vez después de "docker compose up -d".
# La imagen oficial de SQL Server no auto-ejecuta scripts de init (a diferencia de Postgres/MySQL),
# así que esperamos a que el motor esté listo y corremos init.sql manualmente.
set -e

CONTAINER="helpdeskpro-sqlserver"
SA_PASSWORD="Alonso10."
SQLCMD="/opt/mssql-tools18/bin/sqlcmd"

echo "Esperando a que SQL Server esté listo dentro del contenedor..."
until docker exec "$CONTAINER" $SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -C -Q "SELECT 1" > /dev/null 2>&1; do
  sleep 2
  echo "  ...todavía iniciando"
done

echo "Ejecutando init.sql (crea HelpDeskDB y el usuario helpdesk_user)..."
docker exec -i "$CONTAINER" $SQLCMD -S localhost -U sa -P "$SA_PASSWORD" -C < init/init.sql

echo "Listo. HelpDeskDB y helpdesk_user quedaron creados (o ya existían)."
