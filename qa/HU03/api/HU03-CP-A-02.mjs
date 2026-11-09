// HU03-CP-A-02 · Cargar un Excel válido de deudores · HU03-CA-01
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, DEUDORES, CABECERA_DEUDORES, rucQA, deudorValido, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const rucs = [rucQA(), rucQA()]
  await rucsLibres(token, DEUDORES, rucs)
  const config = await crearConfiguracion(token, 'A02')
  try {
    const { status, texto } = await cargarExcel(token, DEUDORES, [CABECERA_DEUDORES, ...rucs.map((r) => deudorValido(r))], config)
    const lista = await guardados(token, DEUDORES, rucs)
    const informaDos = /\b2\b/.test(texto)
    const enConfig = lista.filter((x) => x.configuracionEmpresa?.id === config).length
    return resultado(
      status === 200 && informaDos && enConfig === 2,
      `carga ${status}, ${informaDos ? 'informa 2 procesados' : 'no informa 2 procesados'}, ${enConfig} de 2 deudores consultables en la configuración`,
    )
  } finally {
    await limpiar(token, DEUDORES, rucs)
  }
}
