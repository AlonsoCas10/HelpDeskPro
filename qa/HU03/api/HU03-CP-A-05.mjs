// HU03-CP-A-05 · Rechazar un archivo que no es Excel · HU03-CA-03
// Deja una configuración de QA: no existe una ruta para borrarla.
import { sesion, resultado } from '../../comun.mjs'
import { CORREO, CLIENTES, crearConfiguracion, cargarArchivo, mencionaFormato, revelaDetalleTecnico } from '../apoyo.mjs'

export default async function () {
  const token = await sesion(CORREO)
  const config = await crearConfiguracion(token, 'A05')
  const csv = await cargarArchivo(token, CLIENTES, 'RUC,Razon social\n20999000000,QA HU03 A05\n', 'qa-hu03.csv', 'text/csv', config)
  const falso = await cargarArchivo(token, CLIENTES, 'esto no es un Excel', 'qa-hu03.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', config)

  const bien = (r) => r.status === 400 && mencionaFormato(r.texto) && !revelaDetalleTecnico(r.texto)
  const describir = (r) => `${r.status}${revelaDetalleTecnico(r.texto) ? ' con detalle técnico' : ''}${mencionaFormato(r.texto) ? '' : ' sin indicar el formato'}`
  return resultado(bien(csv) && bien(falso), `CSV ${describir(csv)}; texto renombrado a .xlsx ${describir(falso)}`)
}
