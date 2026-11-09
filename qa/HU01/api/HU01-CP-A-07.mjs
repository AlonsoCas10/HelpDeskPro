// HU01-CP-A-07 · Permitir una lectura con sesión válida · HU01-CA-03
// Aquí la lectura es el caso mismo: un 401 con un token vigente es un fallo del producto.
import { pedir, leerToken, claims, vencido, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'

export default async function () {
  const token = leerToken(CORREO, 'ACCESS')
  if (claims(token)?.token_use !== 'access') bloquear('QA_ACCESS_MAIL_02_A01 no es un access token')
  if (vencido(token)) bloquear('QA_ACCESS_MAIL_02_A01 está vencido: copiar uno nuevo del navegador')

  const { status } = await pedir('/clientes', { token })
  return resultado(status === 200, `GET /clientes con sesión válida respondió ${status}`)
}
