// HU04-CP-A-03 · Rechazar connection-test sin sesión · HU04-CA-02
import { pedir, resultado } from '../../comun.mjs'

export default async function () {
  const { status } = await pedir('/integraciones/gmail/connection-test')
  return resultado(status === 401, `GET /integraciones/gmail/connection-test sin sesión respondió ${status}`)
}
