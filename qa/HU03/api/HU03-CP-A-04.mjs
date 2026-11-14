// HU03-CP-A-04 · Rechazar columnas desconocidas en la carga de deudores · HU03-CA-02
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, DEUDORES, rucQA, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar, mencionaFormato } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const rucs = [rucQA()]
  await rucsLibres(token, DEUDORES, rucs)
  const config = await crearConfiguracion(token, 'A04')
  try {
    const filas = [['Código', 'Nombre', 'Ciudad', 'Teléfono', 'Notas'], [rucs[0], 'QA HU03 A04', 'Lima', '999999999', 'sin uso']]
    const { status, texto } = await cargarExcel(token, DEUDORES, filas, config)
    const creados = (await guardados(token, DEUDORES, rucs)).length
    return resultado(
      status === 400 && mencionaFormato(texto) && creados === 0,
      `carga ${status}, ${mencionaFormato(texto) ? 'indica el formato esperado' : 'no indica el formato esperado'}, ${creados} deudores creados`,
    )
  } finally {
    await limpiar(token, DEUDORES, rucs)
  }
}
