// HU04-CP-A-02 · Verificar conexión con configuración incompleta · HU04-CA-03
// Comprueba que, sin la configuración de Gmail, la respuesta es un 503 controlado y no un error interno.
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/integraciones/gmail/connection-test', { token })
  if (status === 200) bloquear('Gmail está configurado en el entorno: este caso necesita el entorno sin la configuración de Gmail')
  const cuerpo = json(texto)
  return resultado(
    status === 503 && cuerpo?.status === 'error' && !!cuerpo?.message,
    `connection-test con configuración incompleta respondió ${status}${cuerpo?.message ? ' con mensaje controlado' : ''}`,
  )
}
