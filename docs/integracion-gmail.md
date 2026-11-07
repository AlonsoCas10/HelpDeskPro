# Integracion con Gmail

## Objetivo

La integracion con Gmail obtiene correos y sus metadatos para que FactSafe pueda procesarlos. El modulo trabaja exclusivamente con Gmail API y no modifica, elimina, mueve ni pone en cuarentena mensajes.

## Alcance implementado

La integracion permite:

- Verificar la conexion con Gmail API.
- Activar y desactivar la recepcion de nuevos correos.
- Configurar un `watch` de Gmail mediante Google Cloud Pub/Sub.
- Recibir notificaciones de cambios de Gmail.
- Consultar el historial de mensajes agregados.
- Obtener el mensaje completo mediante Gmail API.
- Extraer metadatos, contenido, enlaces y adjuntos.
- Persistir el correo normalizado como evento en SQL Server.
- Evitar duplicados cuando Pub/Sub reintenta una notificacion.
- Registrar errores de conexion, lectura y procesamiento.

## Configuracion

La configuracion se carga desde las variables de entorno del backend. Los nombres esperados son:

| Variable | Uso |
| --- | --- |
| `GOOGLE_CLIENT_ID` | Identificador del cliente OAuth de Google. |
| `GOOGLE_CLIENT_SECRET` | Secreto del cliente OAuth de Google. |
| `GOOGLE_REFRESH_TOKEN` | Token de refresco para acceder a Gmail API. |
| `GOOGLE_GMAIL_APPLICATION_NAME` | Nombre de la aplicacion enviada a Google API. |
| `GOOGLE_GMAIL_SCOPE` | Scope de acceso configurado para Gmail. |
| `GOOGLE_GMAIL_PUBSUB_TOPIC` | Nombre completo del topic de Google Pub/Sub. |
| `GOOGLE_GMAIL_PUBSUB_SUBSCRIPTION` | Nombre de la suscripcion pull de Pub/Sub. |
| `GOOGLE_GMAIL_EXPECTED_EMAIL` | Cuenta Gmail autorizada que se aceptara. |
| `GOOGLE_GMAIL_PUBSUB_ENABLED` | Estado inicial del consumidor al arrancar el backend. |
| `GOOGLE_GMAIL_WATCH_RENEWAL_MS` | Intervalo de renovacion del watch. |

Los valores reales de estas variables no forman parte de esta documentacion.

## Activacion de Gmail

La recepcion de nuevos correos solo esta activa cuando el consumidor Gmail esta habilitado.

### Consultar estado

```http
GET /integraciones/gmail/status
```

Respuesta:

```json
{
  "provider": "gmail",
  "enabled": true
}
```

### Activar o desactivar

```http
POST /integraciones/gmail/activation?enabled=true
```

Para desactivar:

```http
POST /integraciones/gmail/activation?enabled=false
```

Cuando se activa, el backend inicia el watch de Gmail y el consumidor pull de Pub/Sub. Cuando se desactiva, detiene el consumidor y deja de recibir nuevos mensajes.

Si falta la suscripcion de Pub/Sub, el topic o la configuracion necesaria, la activacion devuelve un error y el consumidor permanece deshabilitado.

## Verificacion de conexion

```http
GET /integraciones/gmail/connection-test
```

Una conexion exitosa devuelve el estado, la cuenta Gmail conectada y los totales informados por Gmail:

```json
{
  "status": "connected",
  "emailAddress": "cuenta@example.com",
  "messagesTotal": 100,
  "threadsTotal": 80
}
```

Los errores de configuracion devuelven un estado `503`. Los errores de acceso o comunicacion con Google devuelven un estado `502`.

## Flujo de recepcion

1. El usuario activa Gmail desde la pestaña de integraciones.
2. El backend inicia un `watch` sobre la etiqueta `INBOX`.
3. Gmail publica una notificacion en Google Cloud Pub/Sub.
4. El consumidor valida `emailAddress` y `historyId`.
5. El backend consulta el historial de Gmail usando `messageAdded`.
6. Cada mensaje se obtiene en formato completo.
7. El parser normaliza el mensaje.
8. El evento y sus relaciones de enlaces y archivos se guardan en SQL Server.
9. La notificacion se confirma con `ack`.
10. Si ocurre un error de lectura o persistencia, la notificacion se marca para reintento con `nack`.

El watch se renueva automaticamente con el intervalo configurado.

## Datos normalizados

Cada mensaje obtiene los siguientes datos:

- `idGmailMessage`: identificador unico del mensaje.
- `idGmailThread`: identificador del hilo.
- `nombreRemitente`: nombre visible del encabezado `From`, si existe.
- `correoRemitente`: direccion de correo extraida del encabezado `From`.
- `dominioRemitente`: dominio de la direccion del remitente.
- `asunto`: asunto del mensaje.
- `fechaHora`: fecha convertida a `LocalDateTime` cuando el formato es reconocible.
- `contenidoTexto`: cuerpo en texto plano.
- `contenidoHTML`: cuerpo HTML.
- `estadoConexion`: estado de la conexion Gmail utilizada para procesar el mensaje.

Cuando un dato no esta disponible, se conserva como ausente y no se reemplaza por un valor inventado.

## Enlaces

Los enlaces se detectan en cuerpos de texto plano y HTML. Se conservan sin duplicados.

Cada enlace se persiste como una entidad `Enlace` relacionada con su evento y contiene:

- `url`: URL detectada.
- `dominio`: dominio extraido de la URL cuando es valido.

Un mensaje sin enlaces se procesa normalmente y no crea registros de enlace.

## Archivos adjuntos

Los adjuntos se identifican recorriendo las partes MIME del mensaje. No se descarga ni se almacena el contenido binario.

Cada archivo se persiste como una entidad `Archivo` relacionada con su evento y contiene:

- `nombre`: nombre del archivo informado por Gmail.
- `tipo`: tipo MIME.
- `extension`: extension derivada del nombre.

Un mensaje sin adjuntos se procesa normalmente y no crea registros de archivo.

## Persistencia

La entidad `Evento` representa el mensaje Gmail normalizado. Sus relaciones son:

- Un evento puede tener muchos enlaces.
- Un evento puede tener muchos archivos adjuntos.
- Los enlaces y archivos se guardan junto con el evento mediante cascada JPA.
- `idGmailMessage` identifica de forma unica el mensaje.
- Si llega nuevamente el mismo `idGmailMessage`, se reutiliza el evento existente.

El esquema se actualiza mediante la configuracion JPA existente de SQL Server.

## Manejo de errores

- Gmail no disponible: se registra el error de conexion.
- Configuracion incompleta: la activacion permanece deshabilitada.
- Notificacion incompleta: se registra y se confirma para no procesarla como mensaje valido.
- `historyId` invalido: se registra y se confirma la notificacion.
- Error al leer Gmail API: se registra y se solicita reintento.
- Error al persistir un mensaje: se registra y se solicita reintento.
- Mensaje sin enlaces o adjuntos: continua su procesamiento con los datos disponibles.

## Limites de la integracion

- La unica fuente real de correo es Gmail.
- No se implementa Outlook, Microsoft 365 ni otros proveedores.
- No se eliminan ni mueven mensajes.
- No se ejecutan acciones automaticas sobre los correos.
- No se descargan archivos adjuntos.
- La integracion no decide si un mensaje es peligroso.
