// HU03-CP-A-07 · Rechazar una configuración inexistente · HU03-CA-03
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, CABECERA_CLIENTES, rucQA, clienteValido, rucsLibres, cargarExcel, guardados, limpiar, revelaDetalleTecnico } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const rucs = [rucQA()]
  await rucsLibres(token, CLIENTES, rucs)
  try {
    const { status, texto } = await cargarExcel(token, CLIENTES, [CABECERA_CLIENTES, clienteValido(rucs[0])], 999999999)
    const creados = (await guardados(token, CLIENTES, rucs)).length
    const tecnico = revelaDetalleTecnico(texto)
    return resultado(
      [400, 404].includes(status) && !tecnico && creados === 0,
      `configuración 999999999 respondió ${status}${tecnico ? ' con detalle técnico' : ''}, ${creados} clientes creados`,
    )
  } finally {
    await limpiar(token, CLIENTES, rucs)
  }
}
