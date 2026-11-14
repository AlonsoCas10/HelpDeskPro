// HU02-CP-A-03 · Rechazar configuración con campos obligatorios vacíos · HU02-CA-02
import { pedir, sesion, json, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/configuracion', {
    metodo: 'POST', token,
    cuerpo: { nombreEmpresa: '', moneda: '', umbralMontoRelevante: null },
  })
  const id = json(texto)?.id
  return resultado(
    status === 400 && !id,
    `POST /configuracion con campos vacíos respondió ${status}${id ? ' y guardó el registro' : ''}`,
  )
}
