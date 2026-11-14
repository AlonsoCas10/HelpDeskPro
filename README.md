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

3. En `.env`, pon esa dirección en estas tres variables. En la última, conserva `localhost` y añade la del túnel después de una coma:

   ```properties
   VITE_COGNITO_REDIRECT_URI=https://….trycloudflare.com
   VITE_COGNITO_LOGOUT_URI=https://….trycloudflare.com
   APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,https://….trycloudflare.com
   ```

4. Abre **una terminal nueva** (no cierres la del túnel) y aplica el cambio:

   ```bash
   docker compose up -d --force-recreate backend frontend
   ```

   Usa siempre una terminal nueva después de editar `.env`: una terminal abierta desde antes puede conservar los valores viejos, y esos tienen prioridad sobre `.env`.

5. Abre la dirección en una ventana de incógnito y compártela.

**La dirección cambia cada vez que se inicia el túnel.** Si lo cierras, repite todos los pasos. Mientras dure la demo, inicia sesión desde la dirección del túnel, no desde `localhost`.

**Al terminar:** cierra el túnel con `Ctrl+C`, devuelve las dos variables de Cognito a `http://localhost:5173`, deja `APP_CORS_ALLOWED_ORIGINS=http://localhost:5173`, repite el paso 4 y pide que se quite la dirección de Cognito.

**Si algo falla:**

| Problema | Solución |
|---|---|
| `No matching state found in storage` | El frontend sigue con la dirección anterior. Cierra la terminal (con la papelera, no solo `Ctrl+C`), abre una nueva y repite el paso 4. No hace falta reiniciar Docker. |
| No se guardan cambios o sale `403` al guardar o importar | Falta la dirección del túnel en `APP_CORS_ALLOWED_ORIGINS`. Corrígela y repite el paso 4. |
| `redirect_mismatch` en la página de Cognito | La dirección no está registrada en Cognito o la registraron con barra final. |
| Inicias sesión, pero la lista de eventos sale vacía | `VITE_API_URL` debe estar vacía o valer `/api`. Corrígela y repite el paso 4. |
| `network helpdeskpro-db_default not found` | La aplicación no está corriendo. Levántala con `docker compose up -d --build`. |

**Antes de compartir la dirección:** quien tenga una cuenta de Cognito podrá entrar y, por ahora, los permisos por rol no están completos. Compártela solo con personas de confianza, usa datos simulados y confirma que el registro libre esté desactivado.

## Pruebas de QA

Cada historia tiene sus pruebas en [`qa/`](qa/): criterios de aceptación, casos de prueba y hallazgos en CSV. Hay dos tipos de prueba automática:

| Tipo | Qué prueba | Historias | Necesita |
|---|---|---|---|
| Unitarias | Reglas del backend, sin base de datos | HU01, HU03, HU08, HU11 | Java 21 |
| API | Llamadas reales al backend: sesión, roles, validaciones | HU01, HU02, HU03, HU04, HU08, HU11 | La aplicación corriendo |

Requisitos: Node.js 20.6 o superior y, para las unitarias, Java 21.

1. Crea tu archivo de configuración:

   ```bash
   cp qa/.env.example qa/.env
   ```

   Pon `QA_API_URL=http://localhost:8080`. Con eso ya corren las unitarias y las pruebas de API que no necesitan sesión.

2. Corre las pruebas de una historia desde la raíz del repositorio, por ejemplo de HU01:

   ```bash
   node --env-file=qa/.env qa/unitarias.mjs HU01
   node --env-file=qa/.env qa/correr.mjs HU01
   ```

   Para un solo caso: `node --env-file=qa/.env qa/correr.mjs HU01 HU01-CP-A-04`.

Cada caso termina como `pasó`, `falló` o `bloqueado`. **Bloqueado** no es un error: indica qué falta, casi siempre un token.

**Para las pruebas con sesión** hacen falta tokens de las cuentas de prueba (pídelas al equipo). Inicia sesión en la aplicación, abre `F12` → **Application** → **Session Storage** → la clave que empieza con `oidc.user:`, y copia `access_token` e `id_token` en las variables `QA_ACCESS_…` y `QA_ID_…` de `qa/.env`. Los tokens vencen en una hora; si un caso dice "está vencido", copia uno nuevo.

**Para guardar los resultados** en el CSV de casos de prueba, añade `--anotar` al final y pon tu nombre en `QA_QUIEN`. Cada ejecución, con o sin `--anotar`, se agrega a `qa/HUnn/evidencia/`, que no se sube a git.

Algunos casos fallan a propósito: muestran hallazgos abiertos. El CSV de hallazgos de cada historia indica cuáles.

## Seguridad

- `.env` no se sube a git. `.env.example` solo lleva nombres, nunca valores.
- Las variables `VITE_` llegan al navegador: nunca pongas secretos en ellas.
