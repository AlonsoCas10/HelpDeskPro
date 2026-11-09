// HU04-CP-A-05 · Activar la recepción de correos · HU04-CA-08
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/integraciones/gmail/activation?enabled=true', { metodo: 'POST', token })
  if (status === 503) bloquear(`activar respondió 503: ${json(texto)?.message || 'falta la configuración de Gmail o Pub/Sub en el entorno'}`)
  const { texto: estadoTexto } = await pedir('/integraciones/gmail/status', { token })
  const habilitado = json(estadoTexto)?.enabled
  return resultado(status === 200 && habilitado === true, `activar respondió ${status}; /status devolvió enabled ${habilitado}`)
}
