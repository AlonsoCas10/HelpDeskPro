// Corre los casos de prueba de API de una historia.
//
//   node --env-file=qa/.env qa/correr.mjs HU01                 todos los casos de API de HU01
//   node --env-file=qa/.env qa/correr.mjs HU01 HU01-CP-A-03    solo ese caso
//   node --env-file=qa/.env qa/correr.mjs HU01 --anotar        además escribe en HU01_CasosPrueba.csv
//
// Con --anotar se escriben resultado, fecha, version_probada y quien. La columna hallazgo la llena QA.
// Cada ejecución se agrega a qa/HUnn/evidencia/HUnn_evidencia.txt.

import { readdir } from 'node:fs/promises'
import { dirname, join } from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { Bloqueado, BLOQUEADO, PASO, FALLO, anotarCasos, guardarEvidencia, validarQuien, versionProbada } from './comun.mjs'

const QA = dirname(fileURLToPath(import.meta.url))

const args = process.argv.slice(2)
const anotar = args.includes('--anotar')
const [historia, soloCaso] = args.filter((a) => !a.startsWith('--'))
const quien = validarQuien()

if (!/^HU\d{2}$/.test(historia ?? '')) {
  console.error('Indica la historia, por ejemplo: node --env-file=qa/.env qa/correr.mjs HU01')
  process.exit(2)
}
if (anotar && !quien) {
  console.error('Para --anotar, QA_QUIEN en qa/.env debe ser Alejandra o Judiht')
  process.exit(2)
}

const carpetaApi = join(QA, historia, 'api')
const scripts = (await readdir(carpetaApi).catch(() => []))
  .filter((f) => /^HU\d{2}-CP-A-\d{2}\.mjs$/.test(f))
  .filter((f) => !soloCaso || f === `${soloCaso}.mjs`)
  .sort()

if (scripts.length === 0) {
  console.error(`No hay scripts de API que correr en qa/${historia}/api`)
  process.exit(2)
}

const lineas = []
const imprimir = (texto) => { console.log(texto); lineas.push(texto) }

const resultados = []
for (const archivo of scripts) {
  const id = archivo.replace(/\.mjs$/, '')
  let salida
  try {
    const modulo = await import(pathToFileURL(join(carpetaApi, archivo)).href)
    salida = await modulo.default()
  } catch (error) {
    salida = error instanceof Bloqueado
      ? { resultado: BLOQUEADO, detalle: error.message }
      : { resultado: BLOQUEADO, detalle: `error del script: ${error.name}` }
  }
  resultados.push({ id, ...salida })
  imprimir(`${id.padEnd(14)} ${salida.resultado.padEnd(10)} ${salida.detalle}`)
}

const cuenta = (r) => resultados.filter((x) => x.resultado === r).length
imprimir('')
imprimir(`${resultados.length} casos: ${cuenta(PASO)} pasó, ${cuenta(FALLO)} falló, ${cuenta(BLOQUEADO)} bloqueado`)

const version = await versionProbada(QA)
if (anotar) {
  await anotarCasos(historia, resultados, version, quien)
  imprimir(`Anotado en qa/${historia}/${historia}_CasosPrueba.csv (versión ${version})`)
}

const comando = `node --env-file=qa/.env qa/correr.mjs ${args.join(' ')}`
const ruta = await guardarEvidencia(historia, { nivel: 'Pruebas de API', comando, version, quien, lineas })
console.log(`Evidencia agregada a qa/${historia}/evidencia/${ruta.split('/').pop()}`)

process.exitCode = resultados.every((r) => r.resultado === PASO) ? 0 : 1
