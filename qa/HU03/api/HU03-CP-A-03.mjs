// HU03-CP-A-03 · Rechazar un archivo de deudores en la carga de clientes · HU03-CA-02
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, CABECERA_DEUDORES, rucQA, deudorValido, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar, mencionaFormato } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const rucs = [rucQA()]
  await rucsLibres(token, CLIENTES, rucs)
  const config = await crearConfiguracion(token, 'A03')
  try {
    const { status, texto } = await cargarExcel(token, CLIENTES, [CABECERA_DEUDORES, deudorValido(rucs[0])], config)
    const creados = (await guardados(token, CLIENTES, rucs)).length
    return resultado(
      status === 400 && mencionaFormato(texto) && creados === 0,
      `carga ${status}, ${mencionaFormato(texto) ? 'indica el formato esperado' : 'no indica el formato esperado'}, ${creados} clientes creados`,
    )
  } finally {
    await limpiar(token, CLIENTES, rucs)
  }
}
