// HU02-CP-A-05 · Rechazar carga de Excel sin sesión · HU02-CA-03
import { pedir, excel, formularioArchivo, TIPO_XLSX, resultado } from '../../comun.mjs'

export default async function () {
  const archivo = await excel([['RUC'], ['20999000001']])
  const cuerpo = formularioArchivo(archivo, 'qa.xlsx', TIPO_XLSX, { configuracionEmpresaId: 1 })
  const { status } = await pedir('/clientes/importar-vip', { metodo: 'POST', cuerpo })
  return resultado(status === 401, `POST /clientes/importar-vip sin sesión respondió ${status}`)
}
