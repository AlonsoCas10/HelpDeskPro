// HU02-CP-A-01 · Guardar una configuración válida · HU02-CA-01
// Deja una configuración QA-HU02-… en la base: no existe ruta para borrar configuraciones.
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const sufijo = `A01-${Date.now()}`
  const { status, texto } = await pedir('/configuracion', {
    metodo: 'POST', token,
    cuerpo: { nombreEmpresa: `QA-HU02-${sufijo}`, moneda: 'PEN', umbralMontoRelevante: 1000 },
  })
  const id = json(texto)?.id
  return resultado(status === 200 && !!id, `POST /configuracion válido respondió ${status}${id ? ' y devolvió id' : ' sin id'}`)
}
