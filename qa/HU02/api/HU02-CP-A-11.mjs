// HU02-CP-A-11 · Rechazar un registro de contexto inválido · HU02-CA-05
// Un registro con el monto mal formado debe detectarse y no usarse como contexto válido.
import { sesion, resultado } from '../../comun.mjs'
import { crearConfiguracion, cargarExcel, guardados, limpiar, rucsLibres, rucQA, CLIENTES, CABECERA_CLIENTES } from '../apoyo.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const configuracionId = await crearConfiguracion(token, `A11-${Date.now()}`)
  const ruc = rucQA()
  await rucsLibres(token, CLIENTES, [ruc])
  const filaInvalida = [ruc, `QA HU02 ${ruc}`, 'qa.invalid', 'pagos@qa.invalid', 'abc', 'xyz', 'Sí', 'Activo']
  const filas = [CABECERA_CLIENTES, filaInvalida]
  try {
    await cargarExcel(token, CLIENTES, filas, configuracionId)
    const registro = (await guardados(token, CLIENTES, [ruc]))[0]
    const aceptadoComoValido = registro && (registro.montoPromedioHabitual === 0 || registro.montoPromedioHabitual === null)
    return resultado(
      !registro || !aceptadoComoValido,
      registro
        ? `el registro inválido se guardó con monto ${registro.montoPromedioHabitual}: no se rechazó ni se informó`
        : 'el registro con monto inválido no se guardó como contexto válido',
    )
  } finally {
    await limpiar(token, CLIENTES, [ruc])
  }
}
