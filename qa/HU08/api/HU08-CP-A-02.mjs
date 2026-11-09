// HU08-CP-A-02 · El listado muestra el Risk Score y el nivel · HU08-CA-12
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/eventos', { token })
  const eventos = json(texto)
  if (status !== 200 || !Array.isArray(eventos)) bloquear(`no se pudo leer /eventos (${status})`)
  if (eventos.length === 0) bloquear('no hay eventos en el entorno para comprobar el nivel')
  const sinNivel = eventos.filter((e) => e.clasificacion === null || e.clasificacion === undefined || e.clasificacion === '').length
  return resultado(
    sinNivel === 0,
    `de ${eventos.length} eventos, ${sinNivel} no tienen nivel asignado`,
  )
}
