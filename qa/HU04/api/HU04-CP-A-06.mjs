// HU04-CP-A-06 · Desactivar la recepción de correos · HU04-CA-08
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/integraciones/gmail/activation?enabled=false', { metodo: 'POST', token })
  if (status === 503) bloquear(`desactivar respondió 503: ${json(texto)?.message || 'falta la configuración de Gmail en el entorno'}`)
  const { texto: estadoTexto } = await pedir('/integraciones/gmail/status', { token })
  const habilitado = json(estadoTexto)?.enabled
  return resultado(status === 200 && habilitado === false, `desactivar respondió ${status}; /status devolvió enabled ${habilitado}`)
}
