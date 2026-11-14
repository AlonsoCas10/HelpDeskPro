// HU02-CP-A-08 · Rechazar un archivo que no es Excel · HU02-CA-08
// Un archivo con estructura incorrecta debe dar un error controlado que indique el formato, no un 500 interno.
import { pedir, sesion, resultado } from '../../comun.mjs'
import { crearConfiguracion, cargarArchivo, CLIENTES, revelaDetalleTecnico, mencionaFormato } from '../apoyo.mjs'

const CORREO = 'MAIL-02-A09'

export default async function () {
  const token = await sesion(CORREO)
  const configuracionId = await crearConfiguracion(token, `A08-${Date.now()}`)
  const contenido = Buffer.from('esto no es un excel, es texto plano', 'utf8')
  const { status, texto } = await cargarArchivo(token, CLIENTES, contenido, 'qa.txt', 'text/plain', configuracionId)
  return resultado(
    status === 400 && mencionaFormato(texto) && !revelaDetalleTecnico(texto),
    `carga de un archivo no Excel respondió ${status}${revelaDetalleTecnico(texto) ? ' con detalle técnico interno' : ''}${mencionaFormato(texto) ? '' : ', sin indicar el formato esperado'}`,
  )
}
