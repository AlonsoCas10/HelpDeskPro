// HU11-CP-A-03 · No aceptar nivel ni estado elegidos por el cliente · HU11-CA-02
// Si el backend crea el evento, queda un evento de QA: no existe una ruta para borrar eventos.
import { pedir, sesion, json, resultado } from '../../comun.mjs'

const CORREO = 'MAIL-02-A01'

// Límites de la especificación (RSK-04).
function nivel(puntaje) {
  if (puntaje >= 75) return 'Crítico'
  if (puntaje >= 50) return 'Alto'
  if (puntaje >= 25) return 'Medio'
  return 'Bajo'
}

export default async function () {
  const token = await sesion(CORREO)
  const cuerpo = {
    asunto: 'QA-HU11-A03',
    remitente: 'qa@qa.invalid',
    destinatario: 'qa@qa.invalid',
    fechaHora: new Date().toISOString().slice(0, 19),
    riskScore: 99,
    clasificacion: 'Crítico',
    estado: 'Cerrado',
  }
  const { status, texto } = await pedir('/eventos', { metodo: 'POST', token, cuerpo })
  if ([403, 405].includes(status)) return resultado(true, `POST /eventos respondió ${status}: no se crean eventos a mano`)

  const evento = json(texto)
  if (status !== 200 && status !== 201) return resultado(false, `POST /eventos respondió ${status}`)
  if (!evento) return resultado(false, `POST /eventos respondió ${status} sin un evento legible`)

  const problemas = []
  if (evento.riskScore === 99) problemas.push('conservó el riskScore 99 enviado')
  if (evento.clasificacion !== nivel(evento.riskScore)) problemas.push(`nivel «${evento.clasificacion}» con Risk Score ${evento.riskScore}`)
  if (evento.estado !== 'Requiere revisión') problemas.push(`estado «${evento.estado}» en vez de Requiere revisión`)
  return resultado(
    problemas.length === 0,
    `POST /eventos respondió ${status}; ${problemas.length ? problemas.join('; ') : 'el servidor fijó puntaje, nivel y estado'}. Quedó el evento ${evento.codigo ?? 'sin código'}`,
  )
}
