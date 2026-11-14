// HU01-CP-A-04 · Rechazar un token malformado y uno sin firma · HU01-CA-02
import { pedir, resultado } from '../../comun.mjs'

const b64 = (objeto) => Buffer.from(JSON.stringify(objeto)).toString('base64url')

export default async function () {
  const ahora = Math.floor(Date.now() / 1000)
  const sinFirma = `${b64({ alg: 'none', typ: 'JWT' })}.${b64({ sub: 'qa-hu01', token_use: 'access', iat: ahora, exp: ahora + 3600 })}.`

  const malformado = await pedir('/clientes', { token: 'esto-no-es-un-jwt' })
  const none = await pedir('/clientes', { token: sinFirma })
  return resultado(
    malformado.status === 401 && none.status === 401,
    `token malformado ${malformado.status}, token alg=none ${none.status}`,
  )
}
