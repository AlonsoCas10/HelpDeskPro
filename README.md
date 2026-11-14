# FactSafe

Plataforma web de apoyo para empresas de factoring. Ayuda a detectar, clasificar e interpretar posibles amenazas en correos electrónicos y archivos adjuntos, usando el contexto del negocio para priorizar lo que debe revisarse primero.

> El repositorio y parte del código conservan el nombre anterior del proyecto, HelpDeskPro.

## Qué no hace

FactSafe apoya la revisión humana. No confirma por sí sola que un correo sea fraude, ni elimina, bloquea o pone correos en cuarentena. Tampoco usa inteligencia artificial: el análisis se basa en reglas predefinidas.

## Estado del proyecto

Es un MVP en desarrollo, pensado para demostración con datos simulados. **No está listo para producción.**

| Estado en el backlog | Historias |
|---|---|
| Terminadas | HU01 Acceso, HU02 Configuración, HU03 Carga de contexto, HU04 Integración con Gmail, HU08 Risk Score, HU11 Listado de eventos, HU15 Dashboard |
| En desarrollo | HU09 Factores del Risk Score, HU10 Impacto financiero, HU12 Detalle del evento, HU13 Estado del evento |
| No iniciadas | HU05 Remitente y dominio, HU06 Enlaces y adjuntos, HU07 Correlación, HU14 Alertas |

Una historia terminada en el backlog puede tener hallazgos abiertos. Los criterios, casos de prueba y hallazgos de QA están en [`qa/`](qa/).

## Tecnologías

| Parte | Tecnología |
|---|---|
| Frontend | React 19 y Vite |
| Backend | Spring Boot 4 y Java 21 |
| Base de datos | SQL Server 2022 |
| Autenticación | Amazon Cognito |
| Correo | Gmail API y Google Cloud Pub/Sub |
| Ejecución | Docker Compose |

## Requisitos

Solo necesitas Docker con Compose. Para iniciar sesión hacen falta los valores de Cognito, y para recibir correos, los de Gmail.

## Primera vez

1. Copia el archivo de ejemplo y completa los valores. Pide las credenciales al equipo:

   ```bash
   cp .env.example .env
   ```

   Deja `VITE_API_URL` vacía: el frontend usa `/api` y reenvía esas llamadas al backend.

2. Levanta todo:

   ```bash
   docker compose up -d --build
   ```

3. Abre http://localhost:5173

Si cambias `.env`, aplica los cambios con `docker compose up -d --force-recreate`.

## Comandos habituales

| Tarea | Comando |
|---|---|
| Levantar con actualización en vivo | `docker compose up --build --watch` |
| Levantar en segundo plano | `docker compose up -d --build` |
| Aplicar cambios de `.env` | `docker compose up -d --force-recreate` |
| Ver el estado | `docker compose ps -a` |
| Ver los logs del backend | `docker compose logs -f backend` |
| Detener y conservar los datos | `docker compose down` |
| Borrar también los datos | `docker compose down -v` |
| Ejecutar las pruebas del backend | `cd helpdeskpro-backend && ./mvnw test` |

`db-init` debe aparecer como `Exited (0)`: es un servicio de un solo uso.

## Gmail

Por defecto, el backend no lee correos. Para activarlo, cambia estas variables en `.env`:

- `GOOGLE_GMAIL_HISTORICAL_SYNC_ENABLED=true`: revisa la bandeja de entrada al iniciar y cada 30 segundos. Recupera los correos aunque se pierda una notificación.
- `GOOGLE_GMAIL_PUBSUB_ENABLED=true`: recibe avisos de correos nuevos para mostrarlos antes. Requiere credenciales de `gcloud` en un `compose.override.yaml` local.

Después ejecuta `docker compose up -d`.

## Compartir la demo con un túnel

Un túnel de Cloudflare publica la demo en internet para que cualquiera la abra desde su equipo. No necesitas cuenta ni instalar nada, y basta un solo túnel: el frontend reenvía las llamadas al backend.

1. Con la aplicación corriendo, abre el túnel y deja esa terminal abierta:

   ```bash
   docker run --rm --network helpdeskpro-db_default cloudflare/cloudflared:latest tunnel --no-autoupdate --url http://frontend:5173
   ```

   Copia la dirección `https://….trycloudflare.com` que aparece.

2. Pide a quien administre Cognito que añada esa dirección, sin barra final, en **Allowed callback URLs** y **Allowed sign-out URLs**.

3. En `.env`, pon esa dirección en estas dos variables:

   ```properties
   VITE_COGNITO_REDIRECT_URI=https://….trycloudflare.com
   VITE_COGNITO_LOGOUT_URI=https://….trycloudflare.com
   ```

4. Aplica el cambio en otra terminal:

   ```bash
   docker compose up -d --force-recreate frontend
   ```

5. Abre la dirección en una ventana de incógnito y compártela.

**La dirección cambia cada vez que se inicia el túnel.** Si lo cierras, repite todos los pasos. Mientras dure la demo, inicia sesión desde la dirección del túnel, no desde `localhost`.

**Al terminar:** cierra el túnel con `Ctrl+C`, devuelve las dos variables a `http://localhost:5173`, repite el paso 4 y pide que se quite la dirección de Cognito.

**Si algo falla:**

| Problema | Solución |
|---|---|
| `No matching state found in storage` | El frontend sigue con la dirección anterior. Cierra la terminal, abre una nueva y repite el paso 4. Las variables que haya cargado la terminal tienen prioridad sobre `.env`. |
| `redirect_mismatch` en la página de Cognito | La dirección no está registrada en Cognito o la registraron con barra final. |
| Inicias sesión, pero la lista de eventos sale vacía | `VITE_API_URL` debe estar vacía o valer `/api`. Corrígela y repite el paso 4. |
| `network helpdeskpro-db_default not found` | La aplicación no está corriendo. Levántala con `docker compose up -d --build`. |

**Antes de compartir la dirección:** quien tenga una cuenta de Cognito podrá entrar y, por ahora, los permisos por rol no están completos. Compártela solo con personas de confianza, usa datos simulados y confirma que el registro libre esté desactivado.

## Seguridad

- `.env` no se sube a git. `.env.example` solo lleva nombres, nunca valores.
- Las variables `VITE_` llegan al navegador: nunca pongas secretos en ellas.
