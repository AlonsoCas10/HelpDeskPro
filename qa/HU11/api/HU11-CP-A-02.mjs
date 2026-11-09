// HU11-CP-A-02 · Rechazar el registro de un evento sin sesión · HU11-CA-01
// Si el backend aceptara el POST, quedaría un evento de QA: no existe una ruta para borrar eventos.
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'

async function totalEventos(token) {
  const { status, texto } = await pedir('/eventos', { token })
  const lista = json(texto)
  if (status !== 200 || !Array.isArray(lista)) bloquear(`no se pudo contar los eventos (${status})`)
  return lista.length
}

export default async function () {
  const token = await sesion(CORREO)
  const antes = await totalEventos(token)
  const { status } = await pedir('/eventos', { metodo: 'POST', cuerpo: { asunto: 'QA-HU11-A02', remitente: 'qa@qa.invalid' } })
  const despues = await totalEventos(token)
  return resultado(
    status === 401 && despues === antes,
    `POST /eventos sin sesión respondió ${status}; eventos antes ${antes}, después ${despues}`,
  )
}
