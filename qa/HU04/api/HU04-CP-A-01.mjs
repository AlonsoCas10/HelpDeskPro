// HU04-CP-A-01 · Verificar conexión con credenciales válidas · HU04-CA-03
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/integraciones/gmail/connection-test', { token })
  if (status === 503) bloquear('Gmail no está configurado en el entorno de QA: falta la configuración del servicio')
  if (status === 502) bloquear('Google rechazó el acceso con las credenciales del entorno de QA')
  const cuerpo = json(texto)
  return resultado(
    status === 200 && !!cuerpo?.emailAddress && cuerpo?.messagesTotal !== undefined,
    `connection-test respondió ${status}${status === 200 ? ' con la cuenta y los totales' : ''}`,
  )
}
