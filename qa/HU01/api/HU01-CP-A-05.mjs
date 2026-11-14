// HU01-CP-A-05 · Rechazar un ID token · HU01-CA-02
import { pedir, sesion, leerToken, claims, vencido, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'

export default async function () {
  await sesion(CORREO)
  const idToken = leerToken(CORREO, 'ID')
  if (claims(idToken)?.token_use !== 'id') bloquear('QA_ID_MAIL_02_A01 no es un ID token')
  if (vencido(idToken)) bloquear('QA_ID_MAIL_02_A01 está vencido: copiar uno nuevo del navegador')

  const { status } = await pedir('/clientes', { token: idToken })
  return resultado(status === 401, `GET /clientes con el ID token respondió ${status}`)
}
