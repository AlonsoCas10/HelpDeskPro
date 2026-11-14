// HU01-CP-A-01 · Rechazar lecturas sin sesión · HU01-CA-01
import { pedir, resultado } from '../../comun.mjs'

const RUTAS = ['/clientes', '/deudores', '/facturas', '/usuarios-criticos', '/eventos', '/tickets', '/integraciones/gmail/status']

export default async function () {
  const distintas = []
  for (const ruta of RUTAS) {
    const { status } = await pedir(ruta)
    if (status !== 401) distintas.push(`${ruta} ${status}`)
  }
  return resultado(
    distintas.length === 0,
    distintas.length ? `no respondieron 401: ${distintas.join(', ')}` : `las ${RUTAS.length} rutas respondieron 401`,
  )
}
