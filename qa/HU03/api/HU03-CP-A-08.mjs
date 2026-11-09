// HU03-CP-A-08 · Reportar filas con campos faltantes · HU03-CA-04
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, CABECERA_CLIENTES, rucQA, clienteValido, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar, mencionaRechazo } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const [sinRazon, valido] = [rucQA(), rucQA()]
  const rucs = [sinRazon, valido]
  await rucsLibres(token, CLIENTES, rucs)
  const config = await crearConfiguracion(token, 'A08')
  try {
    const filas = [
      CABECERA_CLIENTES,
      clienteValido('', 'QA HU03 A08 sin RUC'),
      clienteValido(sinRazon, ''),
      clienteValido(valido),
    ]
    const { status, texto } = await cargarExcel(token, CLIENTES, filas, config)
    const lista = (await guardados(token, CLIENTES, rucs)).map((x) => x.ruc)
    const soloValida = lista.length === 1 && lista[0] === valido
    const reporta = mencionaRechazo(texto) && /\b2\b/.test(texto)
    return resultado(
      soloValida && reporta,
      `carga ${status}, ${reporta ? 'reporta las 2 filas rechazadas' : 'no reporta las filas rechazadas'}, ${soloValida ? 'solo se guardó la fila válida' : `se guardaron ${lista.length} filas`}`,
    )
  } finally {
    await limpiar(token, CLIENTES, rucs)
  }
}
