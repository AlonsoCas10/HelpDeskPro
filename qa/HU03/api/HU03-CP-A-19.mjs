// HU03-CP-A-19 · No modificar datos de otra configuración · HU03-CA-10
// Deja dos configuraciones de QA: no existe una ruta para borrarlas.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, CABECERA_CLIENTES, rucQA, clienteValido, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const ruc = rucQA()
  await rucsLibres(token, CLIENTES, [ruc])
  const primera = await crearConfiguracion(token, 'A19-1')
  const segunda = await crearConfiguracion(token, 'A19-2')
  try {
    await cargarExcel(token, CLIENTES, [CABECERA_CLIENTES, clienteValido(ruc, 'QA HU03 A19 primera')], primera)
    await cargarExcel(token, CLIENTES, [CABECERA_CLIENTES, clienteValido(ruc, 'QA HU03 A19 segunda')], segunda)
    const lista = await guardados(token, CLIENTES, [ruc])
    const original = lista.find((x) => x.configuracionEmpresa?.id === primera)
    const intacto = original?.razonSocial === 'QA HU03 A19 primera'
    return resultado(
      intacto,
      intacto
        ? `el cliente de la primera configuración no cambió (${lista.length} registros con ese RUC)`
        : `el cliente de la primera configuración ${original ? 'cambió de razón social' : 'ya no existe: pasó a la segunda'} (${lista.length} registros con ese RUC)`,
    )
  } finally {
    await limpiar(token, CLIENTES, [ruc])
  }
}
