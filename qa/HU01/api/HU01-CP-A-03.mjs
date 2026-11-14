// HU01-CP-A-03 · Rechazar un token con la firma alterada · HU01-CA-02
import { pedir, sesion, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'

export default async function () {
  const token = await sesion(CORREO)
  const [cabecera, cuerpo, firma] = token.split('.')
  const medio = Math.floor(firma.length / 2)
  const otro = firma[medio] === 'A' ? 'B' : 'A'
  const alterado = `${cabecera}.${cuerpo}.${firma.slice(0, medio)}${otro}${firma.slice(medio + 1)}`

  const { status } = await pedir('/clientes', { token: alterado })
  return resultado(status === 401, `GET /clientes con la firma alterada respondió ${status}`)
}
