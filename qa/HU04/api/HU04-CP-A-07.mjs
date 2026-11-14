// HU04-CP-A-07 · Fallo de activación por configuración incompleta · HU04-CA-09
// Con la configuración de Pub/Sub incompleta, activar debe responder con error y el consumidor seguir deshabilitado.
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/integraciones/gmail/activation?enabled=true', { metodo: 'POST', token })
  if (status === 200) bloquear('activar respondió 200: el entorno tiene la configuración completa; este caso necesita Pub/Sub incompleto')
  const { texto: estadoTexto } = await pedir('/integraciones/gmail/status', { token })
  const habilitado = json(estadoTexto)?.enabled
  return resultado(
    status === 503 && json(texto)?.status === 'error' && habilitado === false,
    `activar con configuración incompleta respondió ${status}; /status devolvió enabled ${habilitado}`,
  )
}
