// Funciones comunes de los scripts de QA: backend, sesión, resultado y CSV.
// Regla del flujo: nada de lo que se imprime lleva tokens, contraseñas ni cuerpos de respuesta.

export const PASO = 'pasó'
export const FALLO = 'falló'
export const BLOQUEADO = 'bloqueado'

const ESPERA_MS = 10_000

// Un caso bloqueado no concluye nada sobre el producto.
export class Bloqueado extends Error {}

export function bloquear(motivo) {
  throw new Bloqueado(motivo)
}

export function resultado(cumple, detalle) {
  return { resultado: cumple ? PASO : FALLO, detalle }
}

// ---------- Backend ----------

function base() {
  const url = process.env.QA_API_URL
  if (!url) bloquear('falta QA_API_URL en qa/.env')
  return url.replace(/\/+$/, '')
}

// Devuelve el código y el texto de la respuesta. El texto es solo para
// comprobaciones internas del script: nunca se imprime.
export async function pedir(ruta, { metodo = 'GET', token, cuerpo } = {}) {
  const cabeceras = {}
  const formulario = cuerpo instanceof FormData
  if (token) cabeceras.Authorization = `Bearer ${token}`
  if (cuerpo !== undefined && !formulario) cabeceras['Content-Type'] = 'application/json'

  let respuesta
  try {
    respuesta = await fetch(base() + ruta, {
      method: metodo,
      headers: cabeceras,
      body: cuerpo === undefined ? undefined : formulario ? cuerpo : JSON.stringify(cuerpo),
      signal: AbortSignal.timeout(ESPERA_MS),
    })
  } catch (error) {
    if (error instanceof Bloqueado) throw error
    bloquear(`el backend no responde en QA_API_URL (${error.name})`)
  }
  const texto = await respuesta.text()
  return { status: respuesta.status, texto }
}

// Lee un JSON de una respuesta 200 sin imprimirlo.
export function json(texto) {
  try {
    return JSON.parse(texto)
  } catch {
    return null
  }
}

// ---------- Sesión ----------

// MAIL-02-A01 y 'ACCESS' -> QA_ACCESS_MAIL_02_A01
export function nombreVariable(correo, tipo = 'ACCESS') {
  return `QA_${tipo}_${correo.replace(/-/g, '_')}`
}

export function leerToken(correo, tipo = 'ACCESS') {
  const nombre = nombreVariable(correo, tipo)
  const valor = process.env[nombre]?.trim()
  if (!valor) bloquear(`falta ${nombre} en qa/.env`)
  return valor
}

export function claims(jwt) {
  const partes = jwt.split('.')
  if (partes.length !== 3) return null
  try {
    return JSON.parse(Buffer.from(partes[1], 'base64url').toString('utf8'))
  } catch {
    return null
  }
}

export function vencido(jwt) {
  const exp = claims(jwt)?.exp
  return typeof exp !== 'number' || exp * 1000 <= Date.now()
}

export function grupos(jwt) {
  const valor = claims(jwt)?.['cognito:groups']
  return Array.isArray(valor) ? valor : []
}

export function esAdministrador(jwt) {
  return grupos(jwt).some((g) => g.toUpperCase() === 'ADMINISTRADOR')
}

// Access token vigente de una cuenta de prueba, comprobado con una lectura de control.
// Si la sesión no sirve ni para leer, el caso queda bloqueado.
export async function sesion(correo) {
  const token = leerToken(correo, 'ACCESS')
  const datos = claims(token)
  if (!datos) bloquear(`${nombreVariable(correo)} no es un JWT`)
  if (datos.token_use !== 'access') bloquear(`${nombreVariable(correo)} no es un access token`)
  if (vencido(token)) bloquear(`${nombreVariable(correo)} está vencido: copiar uno nuevo del navegador`)

  const control = await pedir('/clientes', { token })
  if (control.status !== 200) {
    bloquear(`la lectura de control con ${correo} respondió ${control.status}`)
  }
  return token
}

// Antes de un DELETE de prueba: comprueba que el id no existe, para no borrar datos del equipo.
export async function idLibre(ruta, id, token) {
  const { status, texto } = await pedir(ruta, { token })
  const lista = json(texto)
  if (status !== 200 || !Array.isArray(lista)) bloquear(`no se pudo leer ${ruta} para comprobar el id`)
  if (lista.some((x) => String(x?.id) === String(id))) bloquear(`${ruta}/${id} existe: no se borra un registro real`)
}

// ---------- Excel ----------

// Arma un .xlsx mínimo (una hoja, sin estilos) a partir de filas de valores.
// Los números van como número y todo lo demás como texto. No usa dependencias.
export async function excel(filas) {
  const { crc32 } = await import('node:zlib')
  const esc = (v) => String(v).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  const columna = (i) => String.fromCharCode(65 + i)
  const celdas = filas.map((fila, r) => `<row r="${r + 1}">${fila.map((v, c) => {
    if (v === null || v === undefined || v === '') return ''
    const ref = `${columna(c)}${r + 1}`
    return typeof v === 'number'
      ? `<c r="${ref}"><v>${v}</v></c>`
      : `<c r="${ref}" t="inlineStr"><is><t xml:space="preserve">${esc(v)}</t></is></c>`
  }).join('')}</row>`).join('')

  const archivos = {
    '[Content_Types].xml': '<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>',
    '_rels/.rels': '<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>',
    'xl/workbook.xml': '<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Hoja1" sheetId="1" r:id="rId1"/></sheets></workbook>',
    'xl/_rels/workbook.xml.rels': '<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>',
    'xl/worksheets/sheet1.xml': `<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>${celdas}</sheetData></worksheet>`,
  }

  // ZIP sin compresión: cabecera local + datos por archivo, luego el directorio central.
  const locales = []
  const centrales = []
  let desplazamiento = 0
  for (const [nombre, contenido] of Object.entries(archivos)) {
    const n = Buffer.from(nombre, 'utf8')
    const d = Buffer.from(contenido, 'utf8')
    const crc = crc32(d) >>> 0
    const local = Buffer.alloc(30)
    local.writeUInt32LE(0x04034b50, 0); local.writeUInt16LE(20, 4); local.writeUInt16LE(0x0800, 6)
    local.writeUInt32LE(crc, 14); local.writeUInt32LE(d.length, 18); local.writeUInt32LE(d.length, 22)
    local.writeUInt16LE(n.length, 26)
    const central = Buffer.alloc(46)
    central.writeUInt32LE(0x02014b50, 0); central.writeUInt16LE(20, 4); central.writeUInt16LE(20, 6)
    central.writeUInt16LE(0x0800, 8); central.writeUInt32LE(crc, 16); central.writeUInt32LE(d.length, 20)
    central.writeUInt32LE(d.length, 24); central.writeUInt16LE(n.length, 28); central.writeUInt32LE(desplazamiento, 42)
    locales.push(local, n, d)
    centrales.push(central, n)
    desplazamiento += local.length + n.length + d.length
  }
  const dirCentral = Buffer.concat(centrales)
  const fin = Buffer.alloc(22)
  fin.writeUInt32LE(0x06054b50, 0); fin.writeUInt16LE(Object.keys(archivos).length, 8)
  fin.writeUInt16LE(Object.keys(archivos).length, 10); fin.writeUInt32LE(dirCentral.length, 12)
  fin.writeUInt32LE(desplazamiento, 16)
  return Buffer.concat([...locales, dirCentral, fin])
}

// Arma el formulario multipart de una carga de archivo.
export function formularioArchivo(contenido, nombre, tipo, campos = {}) {
  const formulario = new FormData()
  formulario.append('archivo', new Blob([contenido], { type: tipo }), nombre)
  for (const [k, v] of Object.entries(campos)) formulario.append(k, String(v))
  return formulario
}

export const TIPO_XLSX = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

// ---------- Evidencia ----------

// Agrega una ejecución al final de qa/HUnn/evidencia/HUnn_evidencia.txt.
// Lo que se guarda es lo mismo que se imprime: sin tokens, contraseñas ni cuerpos de respuesta.
export async function guardarEvidencia(historia, { nivel, comando, version, quien, lineas }) {
  const { mkdir, appendFile } = await import('node:fs/promises')
  const { dirname, join } = await import('node:path')
  const { fileURLToPath } = await import('node:url')

  const carpeta = join(dirname(fileURLToPath(import.meta.url)), historia, 'evidencia')
  await mkdir(carpeta, { recursive: true })
  const ahora = new Date().toLocaleString('sv-SE', { timeZone: 'America/Lima' }).slice(0, 16)
  const bloque = [
    '='.repeat(72),
    `${nivel} · ${ahora} (hora de Perú)`,
    `Comando: ${comando}`,
    `Versión probada: ${version} · Quién: ${quien || 'sin indicar'}`,
    '-'.repeat(72),
    ...lineas,
    '',
  ].join('\n')
  const ruta = join(carpeta, `${historia}_evidencia.txt`)
  await appendFile(ruta, bloque + '\n', 'utf8')
  return ruta
}

export function versionProbada(cwd) {
  return import('node:child_process').then(({ execFileSync }) =>
    execFileSync('git', ['rev-parse', '--short', 'HEAD'], { cwd }).toString().trim())
}

// Escribe resultado, fecha, version_probada y quien en HUnn_CasosPrueba.csv.
// La columna hallazgo no se toca: la llena QA.
export async function anotarCasos(historia, resultados, version, quien) {
  const { readFile, writeFile } = await import('node:fs/promises')
  const { dirname, join } = await import('node:path')
  const { fileURLToPath } = await import('node:url')

  const ruta = join(dirname(fileURLToPath(import.meta.url)), historia, `${historia}_CasosPrueba.csv`)
  const csv = leerCsv(await readFile(ruta, 'utf8'))
  for (const columna of ['resultado', 'fecha', 'hallazgo', 'version_probada', 'quien']) {
    if (!csv.cabecera.includes(columna)) csv.cabecera.push(columna)
  }
  const col = Object.fromEntries(csv.cabecera.map((c, i) => [c, i]))
  const fecha = new Date().toLocaleDateString('en-CA', { timeZone: 'America/Lima' })
  for (const fila of csv.filas) {
    while (fila.length < csv.cabecera.length) fila.push('')
    const r = resultados.find((x) => x.id === fila[col.id])
    if (!r) continue
    fila[col.resultado] = r.resultado
    fila[col.fecha] = fecha
    fila[col.version_probada] = version
    fila[col.quien] = quien
  }
  await writeFile(ruta, escribirCsv(csv), 'utf8')
}

export function validarQuien() {
  const quien = process.env.QA_QUIEN?.trim()
  return ['Alejandra', 'Judiht'].includes(quien) ? quien : null
}

// ---------- CSV ----------

export function leerCsv(texto) {
  const filas = []
  let fila = []
  let campo = ''
  let entreComillas = false
  const t = texto.replace(/^﻿/, '')

  for (let i = 0; i < t.length; i++) {
    const c = t[i]
    if (entreComillas) {
      if (c === '"' && t[i + 1] === '"') { campo += '"'; i++ }
      else if (c === '"') entreComillas = false
      else campo += c
    } else if (c === '"') entreComillas = true
    else if (c === ',') { fila.push(campo); campo = '' }
    else if (c === '\n' || c === '\r') {
      if (c === '\r' && t[i + 1] === '\n') i++
      fila.push(campo); filas.push(fila); fila = []; campo = ''
    } else campo += c
  }
  if (campo !== '' || fila.length) { fila.push(campo); filas.push(fila) }

  const [cabecera = [], ...resto] = filas
  return { cabecera, filas: resto.filter((f) => f.some((v) => v !== '')) }
}

export function escribirCsv({ cabecera, filas }) {
  const celda = (v = '') => (/[",\n\r]/.test(v) ? `"${v.replace(/"/g, '""')}"` : v)
  const lineas = [cabecera, ...filas].map((f) => f.map(celda).join(','))
  return '﻿' + lineas.join('\n') + '\n'
}
