// HU02-CP-A-10 · Aceptar un cliente VIP sin cuenta bancaria · HU02-CA-11
// El contexto mínimo es identidad, contacto, dominio y comportamiento: la cuenta bancaria no es obligatoria.
import { sesion, resultado } from '../../comun.mjs'
import { crearConfiguracion, cargarExcel, guardados, limpiar, rucsLibres, rucQA, clienteValido, CLIENTES, CABECERA_CLIENTES } from '../apoyo.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const configuracionId = await crearConfiguracion(token, `A10-${Date.now()}`)
  const ruc = rucQA()
  await rucsLibres(token, CLIENTES, [ruc])
  const filas = [CABECERA_CLIENTES, clienteValido(ruc)]
  try {
    const { status } = await cargarExcel(token, CLIENTES, filas, configuracionId)
    const guardado = (await guardados(token, CLIENTES, [ruc])).length === 1
    return resultado(status === 200 && guardado, `carga de un VIP sin cuenta bancaria respondió ${status}; quedó consultable: ${guardado}`)
  } finally {
    await limpiar(token, CLIENTES, [ruc])
  }
}
