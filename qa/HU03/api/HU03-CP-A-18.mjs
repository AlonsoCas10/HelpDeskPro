// HU03-CP-A-18 · No duplicar al repetir una carga · HU03-CA-10
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, CABECERA_CLIENTES, rucQA, clienteValido, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const rucs = [rucQA(), rucQA()]
  await rucsLibres(token, CLIENTES, rucs)
  const config = await crearConfiguracion(token, 'A18')
  try {
    const filas = [CABECERA_CLIENTES, ...rucs.map((r) => clienteValido(r))]
    const primera = await cargarExcel(token, CLIENTES, filas, config)
    const segunda = await cargarExcel(token, CLIENTES, filas, config)
    const total = (await guardados(token, CLIENTES, rucs)).length
    return resultado(
      primera.status === 200 && segunda.status === 200 && total === 2,
      `cargas ${primera.status} y ${segunda.status}; ${total} clientes para 2 RUC`,
    )
  } finally {
    await limpiar(token, CLIENTES, rucs)
  }
}
