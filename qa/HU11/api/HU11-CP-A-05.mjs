// HU11-CP-A-05 · Listar todos los eventos con sus datos · HU11-CA-04
import { pedir, sesion, json, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'
const CAMPOS = ['codigo', 'remitente', 'asunto', 'fechaHora', 'riskScore', 'clasificacion', 'estado']

export default async function () {
  const token = await sesion(CORREO)
  const { status, texto } = await pedir('/eventos', { token })
  const lista = json(texto)
  if (status !== 200 || !Array.isArray(lista)) return resultado(false, `GET /eventos respondió ${status} sin una lista`)

  const vacio = (v) => v === null || v === undefined || (typeof v === 'string' && v.trim() === '')
  const faltantes = CAMPOS
    .map((campo) => [campo, lista.filter((e) => vacio(e[campo])).length])
    .filter(([, n]) => n > 0)
  return resultado(
    faltantes.length === 0,
    `200 con ${lista.length} eventos; ${faltantes.length ? 'sin valor: ' + faltantes.map(([c, n]) => `${c} en ${n}`).join(', ') : 'todos traen sus 7 datos'}`,
  )
}
