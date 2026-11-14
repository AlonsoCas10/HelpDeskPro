// HU03-CP-A-17 · Controlar un fallo al guardar · HU03-CA-09
// La segunda fila lleva un RUC de 300 caracteres para provocar un fallo de la base.
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, CABECERA_CLIENTES, rucQA, clienteValido, crearConfiguracion, rucsLibres, cargarExcel, guardados, limpiar, revelaDetalleTecnico } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const [primero, tercero] = [rucQA(), rucQA()]
  const largo = '20999' + '9'.repeat(295)
  const rucs = [primero, largo, tercero]
  await rucsLibres(token, CLIENTES, rucs)
  const config = await crearConfiguracion(token, 'A17')
  try {
    const filas = [CABECERA_CLIENTES, clienteValido(primero), clienteValido(largo), clienteValido(tercero)]
    const { status, texto } = await cargarExcel(token, CLIENTES, filas, config)
    const quedaron = (await guardados(token, CLIENTES, rucs)).length
    const tecnico = revelaDetalleTecnico(texto)
    const coherente = quedaron === 0 || (/guardad|procesad/i.test(texto) && new RegExp(`\\b${quedaron}\\b`).test(texto))
    return resultado(
      status === 400 && !tecnico && coherente,
      `carga ${status}${tecnico ? ' con detalle técnico' : ''}; quedaron ${quedaron} de 3 filas ${coherente ? 'y la respuesta lo refleja' : 'sin que la respuesta lo informe'}`,
    )
  } finally {
    await limpiar(token, CLIENTES, rucs)
  }
}
