// Corre las pruebas unitarias de QA de una historia (src/test/.../hu01, hu11, ...).
//
//   node --env-file=qa/.env qa/unitarias.mjs HU01             corre y muestra el resultado de cada caso
//   node --env-file=qa/.env qa/unitarias.mjs HU01 --anotar    además escribe en HU01_CasosPrueba.csv
//
// Equivale a ./mvnw test limitado al paquete de la historia, para no arrastrar pruebas
// que necesitan la base de datos o Cognito. Cada ejecución se agrega a la evidencia de la historia.

import { spawnSync } from 'node:child_process'
import { readdir, readFile, rm } from 'node:fs/promises'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { PASO, FALLO, BLOQUEADO, anotarCasos, guardarEvidencia, validarQuien, versionProbada } from './comun.mjs'

const QA = dirname(fileURLToPath(import.meta.url))
const BACKEND = join(QA, '..', 'helpdeskpro-backend')
const INFORMES = join(BACKEND, 'target', 'surefire-reports')

const args = process.argv.slice(2)
const anotar = args.includes('--anotar')
const [historia] = args.filter((a) => !a.startsWith('--'))
const quien = validarQuien()

if (!/^HU\d{2}$/.test(historia ?? '')) {
  console.error('Indica la historia, por ejemplo: node --env-file=qa/.env qa/unitarias.mjs HU01')
  process.exit(2)
}
if (anotar && !quien) {
  console.error('Para --anotar, QA_QUIEN en qa/.env debe ser Alejandra o Judiht')
  process.exit(2)
}

const paquete = historia.toLowerCase()
const lineas = []
const imprimir = (texto) => { console.log(texto); lineas.push(texto) }

// Se borran los informes anteriores de la historia para no leer resultados viejos.
for (const f of await readdir(INFORMES).catch(() => [])) {
  if (f.includes(`.${paquete}.`)) await rm(join(INFORMES, f))
}

const maven = spawnSync('./mvnw', ['-B', 'test', `-Dtest=**/${paquete}/**`, '-Dsurefire.failIfNoSpecifiedTests=false'], {
  cwd: BACKEND, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024,
})
const salidaMaven = (maven.stdout ?? '') + (maven.stderr ?? '')
const resumenMaven = salidaMaven.split('\n')
  .filter((l) => /Tests run: \d+, Failures: \d+, Errors: \d+, Skipped: \d+$|BUILD (SUCCESS|FAILURE)|COMPILATION ERROR|\.java:\[\d+/.test(l))

// Lee los informes XML de Surefire de la historia.
const resultados = []
for (const f of (await readdir(INFORMES).catch(() => [])).filter((f) => f.startsWith('TEST-') && f.includes(`.${paquete}.`))) {
  const xml = await readFile(join(INFORMES, f), 'utf8')
  for (const [, atributos, cuerpo = ''] of xml.matchAll(/<testcase\b([^>]*?)(?:\/>|>([\s\S]*?)<\/testcase>)/g)) {
    const nombre = /name="([^"]*)"/.exec(atributos)?.[1] ?? ''
    const id = /HU\d{2}[-_]CP[-_]U[-_]\d{2}/.exec(nombre)?.[0].replace(/_/g, '-')
    if (!id) continue
    const fallo = /<(failure|error)\b[^>]*message="([^"]*)"/.exec(cuerpo)
    const omitida = /<skipped/.test(cuerpo)
    const resultado = fallo ? FALLO : omitida ? BLOQUEADO : PASO
    const detalle = fallo
      ? fallo[2]
        .replace(/&#10;/g, '\n').replace(/&#9;/g, ' ').replace(/&quot;/g, '"')
        .replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&amp;/g, '&')
        .split('\n').map((l) => l.replace(/^\s*org\.opentest4j\.AssertionFailedError:\s*/, '').trim())
        .filter((l) => l && !/^Multiple Failures/.test(l))
        .join(' | ')
      : omitida ? 'prueba omitida' : 'todas las comprobaciones se cumplieron'
    resultados.push({ id, resultado, detalle })
  }
}

if (resultados.length === 0) {
  imprimir(`No se obtuvo ningún resultado de ${paquete}. Salida de Maven:`)
  for (const l of resumenMaven.length ? resumenMaven : salidaMaven.split('\n').slice(-20)) imprimir(l)
} else {
  resultados.sort((a, b) => a.id.localeCompare(b.id))
  for (const r of resultados) imprimir(`${r.id.padEnd(14)} ${r.resultado.padEnd(10)} ${r.detalle}`)
  const cuenta = (x) => resultados.filter((r) => r.resultado === x).length
  imprimir('')
  imprimir(`${resultados.length} casos: ${cuenta(PASO)} pasó, ${cuenta(FALLO)} falló, ${cuenta(BLOQUEADO)} bloqueado`)
  imprimir('Maven:')
  for (const l of resumenMaven) imprimir(`  ${l.trim()}`)
}

const version = await versionProbada(QA)
if (anotar && resultados.length) {
  await anotarCasos(historia, resultados, version, quien)
  imprimir(`Anotado en qa/${historia}/${historia}_CasosPrueba.csv (versión ${version})`)
}

const comando = `node --env-file=qa/.env qa/unitarias.mjs ${args.join(' ')}`
const ruta = await guardarEvidencia(historia, { nivel: 'Pruebas unitarias', comando, version, quien, lineas })
console.log(`Evidencia agregada a qa/${historia}/evidencia/${ruta.split('/').pop()}`)

process.exitCode = resultados.length && resultados.every((r) => r.resultado === PASO) ? 0 : 1
