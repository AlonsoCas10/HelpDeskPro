// HU01-CP-A-12 · Rechazar operaciones de administración sin rol · HU01-CA-08
// Antes de cada DELETE se comprueba que el id no existe, por si el backend no aplica el rol.
import { pedir, sesion, esAdministrador, idLibre, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'
const ID = 999999999
const RUTAS = ['/clientes', '/deudores', '/usuarios-criticos']

export default async function () {
  const token = await sesion(CORREO)
  if (esAdministrador(token)) bloquear(`${CORREO} tiene rol de administrador: no sirve para este caso`)

  const distintas = []
  for (const ruta of RUTAS) {
    await idLibre(ruta, ID, token)
    const { status } = await pedir(`${ruta}/${ID}`, { metodo: 'DELETE', token })
    if (status !== 403) distintas.push(`${ruta} ${status}`)
  }
  return resultado(
    distintas.length === 0,
    distintas.length ? `no respondieron 403: ${distintas.join(', ')}` : 'los 3 DELETE respondieron 403',
  )
}
