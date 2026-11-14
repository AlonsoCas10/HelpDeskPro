// HU02-CP-A-09 · Identificar una fila con campos obligatorios faltantes · HU02-CA-09
// Una fila sin RUC ni razón social debe reportarse como inválida y no guardarse como contexto válido.
import { pedir, sesion, json, resultado } from '../../comun.mjs'
import { crearConfiguracion, cargarExcel, guardados, limpiar, rucsLibres, rucQA, clienteValido, CLIENTES, CABECERA_CLIENTES, mencionaRechazo } from '../apoyo.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const configuracionId = await crearConfiguracion(token, `A09-${Date.now()}`)
  const rucValido = rucQA()
  await rucsLibres(token, CLIENTES, [rucValido])
  const filaInvalida = ['', '', 'qa.invalid', 'pagos@qa.invalid', 1000, 5000, 'Sí', 'Activo']
  const filas = [CABECERA_CLIENTES, clienteValido(rucValido), filaInvalida]
  try {
    const { status, texto } = await cargarExcel(token, CLIENTES, filas, configuracionId)
    const invalidaGuardada = (await guardados(token, CLIENTES, ['', rucValido])).some((x) => !x.ruc || !x.razonSocial)
    return resultado(
      status === 200 && mencionaRechazo(texto) && !invalidaGuardada,
      `carga respondió ${status}${mencionaRechazo(texto) ? ' y reporta el rechazo' : ' sin reportar las filas rechazadas'}`,
    )
  } finally {
    await limpiar(token, CLIENTES, [rucValido])
  }
}
