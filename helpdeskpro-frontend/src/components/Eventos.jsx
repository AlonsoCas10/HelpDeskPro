import { useState, useEffect } from 'react'
import axios from 'axios'
import '../App.css'

function Eventos() {

  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [eventos, setEventos] = useState([])
  const [busqueda, setBusqueda] = useState("")

  const [remitente, setRemitente] = useState("")
  const [destinatario, setDestinatario] = useState("")
  const [adjunto, setAdjunto] = useState("")
  const [asunto, setAsunto] = useState("")
  const [clasificacion, setClasificacion] = useState("")
  const [riskScore, setRiskScore] = useState("")
  const [estado, setEstado] = useState("")
  const [fechaHora, setFechaHora] = useState("")


  // Obtener eventos cuando se abre la página
  useEffect(() => {

    obtenerEventos()

  }, [])


  // Obtener eventos desde Spring Boot
  async function obtenerEventos() {

    try {

      const respuesta = await axios.get(
        "http://localhost:8080/eventos"
      )

      setEventos(respuesta.data)

    } catch (error) {

      console.error("Error al obtener eventos:", error)

    }

  }


  // Guardar nuevo evento
  async function guardarEvento(e) {

    e.preventDefault()

    try {

      const nuevoEvento = {

        remitente: remitente,
        destinatario: destinatario,
        adjunto: adjunto,
        asunto: asunto,
        clasificacion: clasificacion,
        riskScore: Number(riskScore),
        estado: estado,
        fechaHora: fechaHora

      }


      // Enviar evento al backend
      const respuesta = await axios.post(
        "http://localhost:8080/eventos",
        nuevoEvento
      )


      // Agregar el evento guardado a la tabla
      setEventos((eventosActuales) => [
        ...eventosActuales,
        respuesta.data
      ])


      // Limpiar formulario
      setRemitente("")
      setDestinatario("")
      setAdjunto("")
      setAsunto("")
      setClasificacion("")
      setRiskScore("")
      setEstado("")
      setFechaHora("")


      // Cerrar formulario
      setMostrarFormulario(false)

    } catch (error) {

      console.error("Error al guardar evento:", error)

    }

  }


  // Filtrar eventos
  const eventosFiltrados = eventos.filter((evento) => {

    const texto = busqueda.toLowerCase()

    return (

      evento.codigo?.toLowerCase().includes(texto) ||

      evento.remitente?.toLowerCase().includes(texto) ||

      evento.destinatario?.toLowerCase().includes(texto) ||

      evento.asunto?.toLowerCase().includes(texto) ||

      evento.adjunto?.toLowerCase().includes(texto)

    )

  })


  return (

    <div className="eventos-container">


      {/* ENCABEZADO */}

      <div className="eventos-header">

        <div>

          <h1>
            Eventos
          </h1>

          <p>
            Registro de eventos de correo electrónico
          </p>

        </div>

        
        <button
          className="btn-nuevo-evento"
          onClick={() => setMostrarFormulario(true)}
        >
          + Nuevo evento
        </button>

      </div>


      {/* FILTRO */}

      <div className="filtro-eventos">

        <input
          type="text"
          placeholder="Buscar evento..."
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
        />

      </div>


      {/* FORMULARIO */}

      {mostrarFormulario && (

        <div className="formulario-evento">

          <h2>
            Registrar nuevo evento
          </h2>


          <div className="formulario-grid">


            {/* REMITENTE */}

            <div className="campo-evento">

              <label>
                Remitente
              </label>

              <input
                type="email"
                placeholder="ejemplo@empresa.com"
                value={remitente}
                onChange={(e) => setRemitente(e.target.value)}
              />

            </div>


            {/* DESTINATARIO */}

            <div className="campo-evento">

              <label>
                Destinatario
              </label>

              <input
                type="email"
                placeholder="destinatario@empresa.com"
                value={destinatario}
                onChange={(e) => setDestinatario(e.target.value)}
              />

            </div>


            {/* ADJUNTO */}

            <div className="campo-evento">

              <label>
                Adjunto
              </label>

              <input
                type="file"
                accept=".pdf,.doc,.docx,.xls,.xlsx,.odf,.odt,.ods,.exe"
                onChange={(e) => {

                  if (e.target.files.length > 0) {

                    setAdjunto(
                      e.target.files[0].name
                    )

                  }

                }}
              />

            </div>


            {/* ASUNTO */}

            <div className="campo-evento">

              <label>
                Asunto
              </label>

              <input
                type="text"
                placeholder="Asunto del correo"
                value={asunto}
                onChange={(e) => setAsunto(e.target.value)}
              />

            </div>


            {/* CLASIFICACIÓN */}

            <div className="campo-evento">

              <label>
                Clasificación
              </label>

              <select
                value={clasificacion}
                onChange={(e) => setClasificacion(e.target.value)}
              >

                <option value="">
                  Seleccionar clasificación
                </option>

                <option value="Seguro">
                  Seguro
                </option>

                <option value="Sospechoso">
                  Sospechoso
                </option>

                <option value="Crítico">
                  Crítico
                </option>

              </select>

            </div>


            {/* RISK SCORE */}

            <div className="campo-evento">

              <label>
                Risk Score
              </label>

              <input
                type="number"
                placeholder="0 - 100"
                min="0"
                max="100"
                value={riskScore}
                onChange={(e) => setRiskScore(e.target.value)}
              />

            </div>


            {/* ESTADO */}

            <div className="campo-evento">

              <label>
                Estado
              </label>

              <select
                value={estado}
                onChange={(e) => setEstado(e.target.value)}
              >

                <option value="">
                  Seleccionar estado
                </option>

                <option value="Procesado">
                  Procesado
                </option>

                <option value="Revisar">
                  Revisar
                </option>

                <option value="Bloqueado">
                  Bloqueado
                </option>

              </select>

            </div>


            {/* FECHA Y HORA */}

            <div className="campo-evento">

              <label>
                Fecha y hora
              </label>

              <input
                type="datetime-local"
                value={fechaHora}
                onChange={(e) => setFechaHora(e.target.value)}
              />

            </div>


          </div>


          {/* BOTONES */}

          <div className="formulario-botones">


            {/* CANCELAR */}

            <button
              className="btn-cancelar"
              onClick={() => setMostrarFormulario(false)}
            >
              Cancelar
            </button>


            {/* GUARDAR */}

            <button
              className="btn-guardar-evento"
              onClick={guardarEvento}
            >
              Guardar evento
            </button>


          </div>

        </div>

      )}


      {/* TABLA */}

      <div className="tabla-eventos-container">

        <table className="tabla-eventos">


          {/* CABECERA */}

          <thead>

            <tr>

              <th>
                Identificación
              </th>

              <th>
                Remitente
              </th>

              <th>
                Destinatario
              </th>

              <th>
                Adjunto
              </th>

              <th>
                Asunto
              </th>

              <th>
                Clasificación
              </th>

              <th>
                Risk Score
              </th>

              <th>
                Estado
              </th>

              <th>
                Fecha y hora
              </th>

              <th>
                Detalle
              </th>

            </tr>

          </thead>


          {/* DATOS */}

          <tbody>

            {eventosFiltrados.map((evento) => (

              <tr key={evento.id}>

                <td>
                  {evento.codigo}
                </td>

                <td>
                  {evento.remitente}
                </td>

                <td>
                  {evento.destinatario}
                </td>

                <td>
                  {evento.adjunto}
                </td>

                <td>
                  {evento.asunto}
                </td>

                <td>
                  {evento.clasificacion}
                </td>

                <td>
                  {evento.riskScore}
                </td>

                <td>
                  {evento.estado}
                </td>

                <td>
                  {evento.fechaHora}
                </td>

                <td>

                  <button
                    className="btn-detalle"
                  >
                    Ver
                  </button>

                </td>

              </tr>

            ))}

          </tbody>

        </table>

      </div>

    </div>

  )

}

export default Eventos