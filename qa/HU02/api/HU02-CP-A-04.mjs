// HU02-CP-A-04 · Rechazar guardar configuración sin sesión · HU02-CA-03
import { pedir, resultado } from '../../comun.mjs'

export default async function () {
  const { status } = await pedir('/configuracion', {
    metodo: 'POST',
    cuerpo: { nombreEmpresa: 'QA sin sesión', moneda: 'PEN', umbralMontoRelevante: 1000 },
  })
  return resultado(status === 401, `POST /configuracion sin sesión respondió ${status}`)
}
