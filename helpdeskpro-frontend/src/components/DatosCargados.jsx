import { useEffect, useState } from 'react'
import '../App.css'
import apiClient from '../api/apiClient'

const siNo = (valor) => (valor ? 'Sí' : 'No')
const estado = (valor) => (valor ? 'Activo' : 'Inactivo')
const monto = (valor) =>
  valor === null || valor === undefined
    ? ''
    : Number(valor).toLocaleString('es-PE')
const configuracion = (fila) => fila.configuracionEmpresa?.id ?? ''

const TABLAS = [
  {
    id: 'clientes',
    titulo: 'Clientes',
    ruta: '/clientes',
    columnas: [
      { titulo: 'RUC', valor: (fila) => fila.ruc },
      { titulo: 'Razón social', valor: (fila) => fila.razonSocial },
      { titulo: 'Dominio autorizado', valor: (fila) => fila.dominioAutorizado },
      { titulo: 'Correo autorizado', valor: (fila) => fila.correoAutorizado },
      { titulo: 'Monto promedio', valor: (fila) => monto(fila.montoPromedioHabitual) },
      { titulo: 'Monto máximo', valor: (fila) => monto(fila.montoMaximoHabitual) },
      { titulo: 'VIP', valor: (fila) => siNo(fila.vip) },
      { titulo: 'Estado', valor: (fila) => estado(fila.activo) },
      { titulo: 'Configuración', valor: configuracion },
    ],
  },
  {
    id: 'deudores',
    titulo: 'Deudores',
    ruta: '/deudores',
    columnas: [
      { titulo: 'RUC', valor: (fila) => fila.ruc },
      { titulo: 'Razón social', valor: (fila) => fila.razonSocial },
      { titulo: 'Dominio autorizado', valor: (fila) => fila.dominioAutorizado },
      { titulo: 'Correo autorizado', valor: (fila) => fila.correoAutorizado },
      { titulo: 'Estado', valor: (fila) => estado(fila.activo) },
      { titulo: 'Configuración', valor: configuracion },
    ],
  },
  {
    id: 'criticos',
    titulo: 'Usuarios críticos',
    ruta: '/usuarios-criticos',
    columnas: [
      { titulo: 'Nombre', valor: (fila) => fila.nombre },
      { titulo: 'Correo', valor: (fila) => fila.correo },
      { titulo: 'Área', valor: (fila) => fila.area },
      { titulo: 'Configuración', valor: configuracion },
    ],
  },
]

function DatosCargados() {

  const [resultados, setResultados] = useState(null)
  const [recarga, setRecarga] = useState(0)

  useEffect(() => {

    let componenteActivo = true

    // Cada tabla se consulta por separado: si una falla, las demás se muestran.
    Promise.allSettled(
      TABLAS.map((tabla) => apiClient.get(tabla.ruta))
    ).then((respuestas) => {

      if (!componenteActivo) {
        return
      }

      setResultados(
        Object.fromEntries(
          respuestas.map((respuesta, indice) => [
            TABLAS[indice].id,
            respuesta.status === 'fulfilled' && Array.isArray(respuesta.value.data)
              ? { filas: respuesta.value.data }
              : { error: true },
          ])
        )
      )

    })

    return () => {
      componenteActivo = false
    }

  }, [recarga])


  return (

    <div className="asistente-datos">

      <div className="asistente-header">

        <h2>
          Datos cargados
        </h2>

        <p>
          Consulta de los datos de contexto guardados.
          Esta vista no permite modificarlos.
        </p>

        <button
          type="button"
          onClick={() => {
            setResultados(null)
            setRecarga((actual) => actual + 1)
          }}
        >
          Actualizar
        </button>

      </div>


      {TABLAS.map((tabla) => {

        const resultado = resultados?.[tabla.id]

        return (

          <section key={tabla.id}>

            <h3>
              {tabla.titulo}
              {resultado?.filas ? ` (${resultado.filas.length})` : ''}
            </h3>

            <div className="tabla-eventos-container tabla-scroll">

              <table className="tabla-eventos">

                <thead>
                  <tr>
                    {tabla.columnas.map((columna) => (
                      <th key={columna.titulo}>
                        {columna.titulo}
                      </th>
                    ))}
                  </tr>
                </thead>

                <tbody>

                  {resultado?.filas?.map((fila) => (
                    <tr key={fila.id}>
                      {tabla.columnas.map((columna) => (
                        <td key={columna.titulo}>
                          {columna.valor(fila) || '—'}
                        </td>
                      ))}
                    </tr>
                  ))}

                  {(!resultado || resultado.error || resultado.filas.length === 0) && (
                    <tr>
                      <td
                        className="tabla-vacia"
                        colSpan={tabla.columnas.length}
                      >
                        {!resultado && 'Cargando...'}
                        {resultado?.error && 'No se pudieron consultar estos datos.'}
                        {resultado?.filas && 'No hay datos cargados.'}
                      </td>
                    </tr>
                  )}

                </tbody>

              </table>

            </div>

          </section>

        )

      })}

    </div>

  )

}

export default DatosCargados
