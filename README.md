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

Por defecto, el backend no lee correos. Para activarlo, cambia estas variables en `.env`:

- `GOOGLE_GMAIL_HISTORICAL_SYNC_ENABLED=true`: revisa la bandeja de entrada al iniciar y cada 30 segundos. Recupera los correos aunque se pierda una notificación.
- `GOOGLE_GMAIL_PUBSUB_ENABLED=true`: recibe avisos de correos nuevos para mostrarlos antes. Requiere credenciales de `gcloud` en un `compose.override.yaml` local.

Después ejecuta `docker compose up -d`.

## Compartir una demo con un túnel

Un túnel rápido de Cloudflare permite abrir la demo desde otros equipos, sin cuenta ni instalación. Requiere dos direcciones, una para el frontend y otra para el backend.

**Cada vez que se inicia un túnel, su dirección cambia.** Si se cierra, hay que repetir los pasos 2 a 4. Es solo para demos temporales: depende de tu computadora y de tu conexión.

1. Con la aplicación corriendo, abre un túnel en cada terminal y deja ambas abiertas:

   ```bash
   docker run --rm --network helpdeskpro-db_default cloudflare/cloudflared:latest tunnel --no-autoupdate --url http://frontend:5173
   docker run --rm --network helpdeskpro-db_default cloudflare/cloudflared:latest tunnel --no-autoupdate --url http://backend:8080
   ```

2. Pide a quien administre Cognito que añada la dirección del frontend en **Allowed callback URLs** y **Allowed sign-out URLs**, sin barra final.

3. Actualiza `.env` con las direcciones que te dieron los túneles:

   ```properties
   VITE_API_URL=<dirección del backend>
   VITE_COGNITO_REDIRECT_URI=<dirección del frontend>
   VITE_COGNITO_LOGOUT_URI=<dirección del frontend>
   APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,<dirección del frontend>
   ```

4. Aplica los cambios y comprueba que el frontend tome la dirección nueva:

   ```bash
   docker compose up -d --no-deps backend frontend
   docker compose exec -T frontend sh -c 'echo "$VITE_API_URL"'
   ```

5. Abre la dirección del frontend en una ventana de incógnito. No inicies sesión desde `localhost` mientras dure la demo.

**Al terminar:** cierra los túneles con `Ctrl+C`, devuelve esas variables a `localhost`, repite el paso 4 y pide que se quite la dirección de Cognito.

**Antes de compartir la dirección:** quien tenga una cuenta de Cognito podrá entrar y, por ahora, los permisos por rol no están completos. Compártela solo con personas de confianza, usa datos simulados y confirma que el registro libre esté desactivado.

## Seguridad

- `.env` no se sube a git. `.env.example` solo lleva nombres, nunca valores.
- Las variables `VITE_` llegan al navegador: nunca pongas secretos en ellas.
