// HU01-CP-A-13 · Permitir operaciones de administración con rol · HU01-CA-08
import { pedir, sesion, esAdministrador, idLibre, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'
const ID = 999999999

export default async function () {
  const token = await sesion(CORREO)
  if (!esAdministrador(token)) bloquear(`${CORREO} no tiene rol de administrador en su token`)

  await idLibre('/clientes', ID, token)
  const { status } = await pedir(`/clientes/${ID}`, { metodo: 'DELETE', token })
  return resultado([200, 404].includes(status), `DELETE /clientes/${ID} con rol de administrador respondió ${status}`)
}
