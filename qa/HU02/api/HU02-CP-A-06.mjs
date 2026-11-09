// HU02-CP-A-06 · Consultar la configuración vigente · HU02-CA-06
// Otros módulos deben poder consultar la configuración vigente de la empresa.
import { pedir, sesion, json, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/configuracion', { token })
  const cuerpo = json(texto)
  const tieneConfig = status === 200 && (cuerpo?.moneda !== undefined || Array.isArray(cuerpo))
  return resultado(
    tieneConfig,
    `GET /configuracion respondió ${status}${tieneConfig ? '' : ': no hay forma de consultar la configuración vigente'}`,
  )
}
