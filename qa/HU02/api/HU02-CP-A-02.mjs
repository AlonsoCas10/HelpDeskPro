// HU02-CP-A-02 · No duplicar la configuración vigente · HU02-CA-01
// Dos guardados seguidos para la misma empresa deben dejar una sola configuración vigente.
// Si cada POST devuelve un id distinto, se creó un registro nuevo en lugar de reemplazar.
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

async function guardar(token, nombre) {
  const { status, texto } = await pedir('/configuracion', {
    metodo: 'POST', token,
    cuerpo: { nombreEmpresa: nombre, moneda: 'PEN', umbralMontoRelevante: 1000 },
  })
  const id = json(texto)?.id
  if (status !== 200 || !id) bloquear(`un guardado de la prueba respondió ${status}`)
  return id
}

export default async function () {
  const token = await sesion(CORREO)
  const nombre = `QA-HU02-A02-${Date.now()}`
  const id1 = await guardar(token, nombre)
  const id2 = await guardar(token, nombre)
  return resultado(id1 === id2, id1 === id2
    ? 'el segundo guardado reutilizó la configuración vigente'
    : `el segundo guardado creó un registro nuevo (ids ${id1} y ${id2}): no reemplaza la vigente`)
}
