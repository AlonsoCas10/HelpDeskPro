// HU11-CP-A-01 · Rechazar el listado sin sesión · HU11-CA-01
import { pedir, resultado } from '../../comun.mjs'

export default async function () {
  const { status } = await pedir('/eventos')
  return resultado(status === 401, `GET /eventos sin sesión respondió ${status}`)
}
