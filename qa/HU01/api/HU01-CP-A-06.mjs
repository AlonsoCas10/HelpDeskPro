// HU01-CP-A-06 · Rechazar un token vencido · HU01-CA-02
// La lectura de control usa el access token vigente de la misma cuenta.
import { pedir, sesion, leerToken, claims, vencido, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'

export default async function () {
  await sesion(CORREO)
  const viejo = leerToken(CORREO, 'ACCESS_VENCIDO')
  if (claims(viejo)?.token_use !== 'access') bloquear('QA_ACCESS_VENCIDO_MAIL_02_A01 no es un access token')
  if (!vencido(viejo)) bloquear('QA_ACCESS_VENCIDO_MAIL_02_A01 todavía no vence')
  // El backend tolera 60 s de diferencia de reloj al validar el vencimiento (valor por defecto
  // de Spring Security). Dentro de ese margen un 200 es lo esperado y no dice nada del producto.
  const segundosVencido = Math.floor(Date.now() / 1000) - claims(viejo).exp
  if (segundosVencido < 90) bloquear(`el token venció hace ${segundosVencido} s: esperar a que pasen 90 s`)

  const { status } = await pedir('/clientes', { token: viejo })
  return resultado(status === 401, `GET /clientes con el token vencido respondió ${status}`)
}
