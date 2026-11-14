// HU03-CP-A-16 · Rechazar cargas sin sesión · HU03-CA-08
import { pedir, excel, formularioArchivo, TIPO_XLSX, resultado } from '../../comun.mjs'
import { CARGA, CLIENTES, DEUDORES, CABECERA_CLIENTES, CABECERA_DEUDORES, rucQA, clienteValido, deudorValido } from '../apoyo.mjs'

export default async function () {
  const clientes = formularioArchivo(await excel([CABECERA_CLIENTES, clienteValido(rucQA())]), 'qa.xlsx', TIPO_XLSX, { configuracionEmpresaId: 1 })
  const deudores = formularioArchivo(await excel([CABECERA_DEUDORES, deudorValido(rucQA())]), 'qa.xlsx', TIPO_XLSX, { configuracionEmpresaId: 1 })
  const a = await pedir(CARGA[CLIENTES], { metodo: 'POST', cuerpo: clientes })
  const b = await pedir(CARGA[DEUDORES], { metodo: 'POST', cuerpo: deudores })
  return resultado(a.status === 401 && b.status === 401, `carga de clientes ${a.status}, carga de deudores ${b.status}`)
}
