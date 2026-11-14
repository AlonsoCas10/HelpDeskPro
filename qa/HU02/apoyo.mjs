// Apoyo de los casos de API de HU02: configuración de empresa, archivos de carga y limpieza.
// Cada caso usa RUC únicos con prefijo 20999, comprueba que no existan antes de cargar
// y borra al final solo los registros con esos RUC. No existe ruta para borrar configuraciones:
// cada una queda en la base con nombre de QA.
import { randomInt } from 'node:crypto'
import { pedir, json, bloquear, excel, formularioArchivo, TIPO_XLSX } from '../comun.mjs'

export const CLIENTES = '/clientes'
export const DEUDORES = '/deudores'
export const CARGA = { [CLIENTES]: '/clientes/importar-vip', [DEUDORES]: '/deudores/importar' }

export const CABECERA_CLIENTES = ['RUC', 'Razón social', 'Dominio autorizado', 'Correo autorizado', 'Monto promedio habitual', 'Monto máximo habitual', 'VIP', 'Estado']
export const CABECERA_DEUDORES = ['RUC', 'Razón social', 'Dominio autorizado', 'Correo autorizado', 'Estado']

export function rucQA() {
  return `20999${String(randomInt(0, 1_000_000)).padStart(6, '0')}`
}

export function clienteValido(ruc, razon = `QA HU02 ${ruc}`) {
  return [ruc, razon, 'qa.invalid', 'pagos@qa.invalid', 1000, 5000, 'Sí', 'Activo']
}

export async function crearConfiguracion(token, sufijo) {
  const { status, texto } = await pedir('/configuracion', {
    metodo: 'POST', token,
    cuerpo: { nombreEmpresa: `QA-HU02-${sufijo}`, moneda: 'PEN', umbralMontoRelevante: 1000 },
  })
  const id = json(texto)?.id
  if (status !== 200 || !id) bloquear(`no se pudo crear la configuración de QA (${status})`)
  return id
}

export async function listar(token, ruta) {
  const { status, texto } = await pedir(ruta, { token })
  const lista = json(texto)
  if (status !== 200 || !Array.isArray(lista)) bloquear(`no se pudo leer ${ruta} (${status})`)
  return lista
}

export async function rucsLibres(token, ruta, rucs) {
  const existentes = (await listar(token, ruta)).filter((x) => rucs.includes(x.ruc))
  if (existentes.length) bloquear(`ya existe un registro con un RUC de la prueba en ${ruta}`)
}

export async function cargarExcel(token, ruta, filas, configuracionId, nombre = 'qa-hu02.xlsx') {
  const archivo = await excel(filas)
  return cargarArchivo(token, ruta, archivo, nombre, TIPO_XLSX, configuracionId)
}

export function cargarArchivo(token, ruta, contenido, nombre, tipo, configuracionId) {
  const cuerpo = formularioArchivo(contenido, nombre, tipo, { configuracionEmpresaId: configuracionId })
  return pedir(CARGA[ruta], { metodo: 'POST', token, cuerpo })
}

export async function guardados(token, ruta, rucs) {
  return (await listar(token, ruta)).filter((x) => rucs.includes(x.ruc))
}

export async function limpiar(token, ruta, rucs) {
  let borrados = 0
  for (const x of await guardados(token, ruta, rucs)) {
    const { status } = await pedir(`${ruta}/${x.id}`, { metodo: 'DELETE', token })
    if (status >= 200 && status < 300) borrados++
  }
  return borrados
}

export function revelaDetalleTecnico(texto) {
  return /Exception|java\.|org\.|jakarta\.|com\.helpdeskpro|SQL|JDBC|Hibernate|POI|Workbook|could not|constraint|truncat|\bat [a-z]+\.[a-z]/i.test(texto)
}

export const mencionaFormato = (texto) => /formato|columna|encabezado|plantilla/i.test(texto)
export const mencionaRechazo = (texto) => /rechaz|inválid|no se guard|omit/i.test(texto)
