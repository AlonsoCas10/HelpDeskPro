// HU02-CP-A-07 · Importar clientes VIP desde un Excel válido · HU02-CA-07
import { pedir, sesion, json, resultado } from '../../comun.mjs'
import { crearConfiguracion, cargarExcel, guardados, limpiar, rucsLibres, rucQA, clienteValido, CLIENTES, CABECERA_CLIENTES } from '../apoyo.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const configuracionId = await crearConfiguracion(token, `A07-${Date.now()}`)
  const rucs = [rucQA(), rucQA()]
  await rucsLibres(token, CLIENTES, rucs)
  const filas = [CABECERA_CLIENTES, clienteValido(rucs[0]), clienteValido(rucs[1])]
  try {
    const { status, texto } = await cargarExcel(token, CLIENTES, filas, configuracionId)
    const guardadosRucs = (await guardados(token, CLIENTES, rucs)).map((x) => x.ruc)
    return resultado(
      status === 200 && guardadosRucs.length === 2,
      `carga de Excel válido respondió ${status}; ${guardadosRucs.length} de 2 clientes quedaron consultables${json(texto)?.procesados !== undefined ? `, informa ${json(texto).procesados} procesados` : ''}`,
    )
  } finally {
    await limpiar(token, CLIENTES, rucs)
  }
}
