// HU01-CP-A-02 · Rechazar escrituras sin sesión · HU01-CA-01
// Usa la sesión de MAIL-02-A01 solo para leer el estado de Gmail antes y después.
// Si el estado cambiara, el script lo restaura con esa sesión.
import { pedir, sesion, json, bloquear, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'

async function gmailActivo(token) {
  const { status, texto } = await pedir('/integraciones/gmail/status', { token })
  const enabled = json(texto)?.enabled
  if (status !== 200 || typeof enabled !== 'boolean') bloquear(`no se pudo leer el estado de Gmail (${status})`)
  return enabled
}

export default async function () {
  const token = await sesion(CORREO)
  const antes = await gmailActivo(token)

  const llamadas = [
    ['POST', '/configuracion', { nombreEmpresa: 'QA-HU01-CP-A-02' }],
    ['DELETE', '/clientes/999999999'],
    ['POST', '/integraciones/gmail/activation?enabled=false'],
  ]
  const distintas = []
  for (const [metodo, ruta, cuerpo] of llamadas) {
    const { status } = await pedir(ruta, { metodo, cuerpo })
    if (status !== 401) distintas.push(`${metodo} ${ruta.split('?')[0]} ${status}`)
  }

  const despues = await gmailActivo(token)
  if (despues !== antes) {
    await pedir(`/integraciones/gmail/activation?enabled=${antes}`, { metodo: 'POST', token })
  }

  const cumple = distintas.length === 0 && despues === antes
  const detalle = [
    distintas.length ? `no respondieron 401: ${distintas.join(', ')}` : 'las 3 escrituras respondieron 401',
    despues === antes ? 'el estado de Gmail no cambió' : 'el estado de Gmail cambió y se restauró',
  ].join('; ')
  return resultado(cumple, detalle)
}
