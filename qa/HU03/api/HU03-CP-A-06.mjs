// HU03-CP-A-06 · Rechazar un archivo vacío · HU03-CA-03
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado, TIPO_XLSX } from '../../comun.mjs'
import { CORREO, DEUDORES, crearConfiguracion, cargarArchivo, revelaDetalleTecnico } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const config = await crearConfiguracion(token, 'A06')
  const { status, texto } = await cargarArchivo(token, DEUDORES, new Uint8Array(0), 'qa-hu03-vacio.xlsx', TIPO_XLSX, config)
  const tecnico = revelaDetalleTecnico(texto)
  return resultado(status === 400 && !tecnico, `archivo de 0 bytes respondió ${status}${tecnico ? ' con detalle técnico' : ''}`)
}
