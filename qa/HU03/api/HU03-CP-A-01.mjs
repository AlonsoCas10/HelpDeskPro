// HU03-CP-A-01 · Cargar un Excel válido de clientes · HU03-CA-01
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, CABECERA_CLIENTES, rucQA, clienteValido, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const rucs = [rucQA(), rucQA()]
  await rucsLibres(token, CLIENTES, rucs)
  const config = await crearConfiguracion(token, 'A01')
  try {
    const { status, texto } = await cargarExcel(token, CLIENTES, [CABECERA_CLIENTES, ...rucs.map((r) => clienteValido(r))], config)
    const lista = await guardados(token, CLIENTES, rucs)
    const informaDos = /\b2\b/.test(texto)
    const enConfig = lista.filter((x) => x.configuracionEmpresa?.id === config).length
    return resultado(
      status === 200 && informaDos && enConfig === 2,
      `carga ${status}, ${informaDos ? 'informa 2 procesados' : 'no informa 2 procesados'}, ${enConfig} de 2 clientes consultables en la configuración`,
    )
  } finally {
    await limpiar(token, CLIENTES, rucs)
  }
}
