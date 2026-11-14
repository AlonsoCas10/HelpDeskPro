// HU04-CP-A-04 · Rechazar activación sin sesión · HU04-CA-02
import { pedir, sesion, json, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

async function estado(token) {
  const { texto } = await pedir('/integraciones/gmail/status', { token })
  return json(texto)?.enabled
}

export default async function () {
  const token = await sesion(CORREO)
  const antes = await estado(token)
  const { status } = await pedir('/integraciones/gmail/activation?enabled=true', { metodo: 'POST' })
  const despues = await estado(token)
  return resultado(
    status === 401 && antes === despues,
    `POST /integraciones/gmail/activation sin sesión respondió ${status}; estado antes ${antes}, después ${despues}`,
  )
}
