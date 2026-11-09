// HU01-CP-A-09 · No aceptar registro ni login propios sin sesión · HU01-CA-05
// Si el registro llegara a crearse, deja una fila de QA: no existe una ruta para borrarla.
import { randomUUID } from 'node:crypto'
import { pedir, resultado } from '../../comun.mjs'

const RECHAZOS = [401, 404]

export default async function () {
  const correo = `qa-hu01-a09-${Date.now()}@qa.invalid`
  const clave = `QA-${randomUUID()}`
  const cuerpo = { nombre: 'QA HU01 A09', correo, contraseña: clave }

  const registro = await pedir('/usuarios/registro', { metodo: 'POST', cuerpo })
  const login = await pedir('/usuarios/login', { metodo: 'POST', cuerpo: { correo, contraseña: clave } })

  const devuelveClave = [registro, login].some((r) => r.texto.includes(clave))
  const loginDaDatos = login.texto.includes(correo)
  const cumple = RECHAZOS.includes(registro.status) && RECHAZOS.includes(login.status) && !devuelveClave && !loginDaDatos

  const detalle = [`registro ${registro.status}`, `login ${login.status}`]
  if (devuelveClave) detalle.push('una respuesta incluye la contraseña')
  if (loginDaDatos) detalle.push('el login devuelve datos del usuario')
  if (registro.status >= 200 && registro.status < 300) detalle.push('quedó un usuario de QA en la base')
  return resultado(cumple, detalle.join('; '))
}
