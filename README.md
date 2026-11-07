# FactSafe 

## Requisitos

Solo necesitas Docker con Compose.

## Primera vez

1. Copia el archivo de ejemplo y completa los valores. Pide las credenciales al equipo:

   ```bash
   cp .env.example .env
   ```

2. Levanta todo:

   ```bash
   docker compose up --build --watch
   ```

3. Abre http://localhost:5173

## Comandos habituales

| Tarea | Comando |
|---|---|
| Levantar con actualización en vivo | `docker compose up --build --watch` |
| Levantar en segundo plano | `docker compose up -d --build` |
| Ver el estado | `docker compose ps -a` |
| Ver los logs del backend | `docker compose logs -f backend` |
| Detener y conservar los datos | `docker compose down` |
| Borrar también los datos | `docker compose down -v` |
| Ejecutar las pruebas del backend | `cd helpdeskpro-backend && ./mvnw test` |

`db-init` debe aparecer como `Exited (0)`: es un servicio de un solo uso.

## Gmail

Por defecto, el backend no lee correos al arrancar. Para activarlo, cambia estas variables en `.env`:

- `GOOGLE_GMAIL_HISTORICAL_SYNC_ENABLED=true`: sincroniza correos recientes al iniciar.
- `GOOGLE_GMAIL_PUBSUB_ENABLED=true`: recibe correos nuevos.

Después ejecuta `docker compose up -d`.

## Seguridad

- `.env` no se sube a git. `.env.example` solo lleva nombres, nunca valores.
- Las variables `VITE_` llegan al navegador: nunca pongas secretos en ellas.
