// HU03-CP-A-10 · Rechazar RUC, correo y dominio inválidos · HU03-CA-05
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, CABECERA_CLIENTES, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar, mencionaRechazo } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const rucInvalido = `20999QA${Date.now() % 10000}`
  const rucs = [rucInvalido]
  await rucsLibres(token, CLIENTES, rucs)
  const config = await crearConfiguracion(token, 'A10')
  try {
    const fila = [rucInvalido, 'QA HU03 A10', 'no es un dominio', 'no-es-un-correo', 1000, 5000, 'Sí', 'Activo']
    const { status, texto } = await cargarExcel(token, CLIENTES, [CABECERA_CLIENTES, fila], config)
    const creados = (await guardados(token, CLIENTES, rucs)).length
    return resultado(
      creados === 0 && mencionaRechazo(texto),
      `carga ${status}, ${mencionaRechazo(texto) ? 'reporta la fila rechazada' : 'no reporta ningún rechazo'}, ${creados ? 'la fila se guardó tal cual' : 'la fila no se guardó'}`,
    )
  } finally {
    await limpiar(token, CLIENTES, rucs)
  }
}
