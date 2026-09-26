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
  const [estado, setEstado] = useState("")
  const [fechaHora, setFechaHora] = useState("")

  // Controla si mostramos la lista o el detalle
  const [vista, setVista] = useState("lista")

  // Guarda el evento seleccionado
  const [eventoSeleccionado, setEventoSeleccionado] = useState(null)

  const [seccionDetalle, setSeccionDetalle] = useState("Resumen")
  const [analisis, setAnalisis] = useState("")
  const [analisisGuardados, setAnalisisGuardados] = useState([])
  
  const [resultado, setResultado] = useState("")
  const [resultadoGuardado, setResultadoGuardado] = useState("")

  const [dominioRegistrado, setDominioRegistrado] = useState("")

  // =====================================================
  // OBTENER EVENTOS
  // =====================================================

  useEffect(() => {

    obtenerEventos()

  }, [])


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


  // =====================================================
  // GUARDAR EVENTO
  // =====================================================

  async function guardarEvento(e) {

    e.preventDefault()

    try {

      const nuevoEvento = {

        remitente: remitente,
        dominioRegistrado: dominioRegistrado,
        destinatario: destinatario,
        adjunto: adjunto,
        asunto: asunto,
        clasificacion: clasificacion,
        estado: estado,
        fechaHora: fechaHora

      }


      const respuesta = await axios.post(
        "http://localhost:8080/eventos",
        nuevoEvento
      )


      setEventos((eventosActuales) => [

        ...eventosActuales,

        respuesta.data

      ])


      // Limpiar formulario

      setRemitente("")
      setDominioRegistrado("")
      setDestinatario("")
      setAdjunto("")
      setAsunto("")
      setClasificacion("")
      setEstado("")
      setFechaHora("")


      // Cerrar formulario

      setMostrarFormulario(false)

    } catch (error) {

      console.error("Error al guardar evento:", error)

    }

  }


  // =====================================================
  // FILTRAR EVENTOS
  // =====================================================

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


  // =====================================================
  // RETURN
  // =====================================================

  return (

    <div className="eventos-container">


      {/* =====================================================
          VISTA DE LISTA
          ===================================================== */}

      {vista === "lista" && (

        <>


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


          {/* =================================================
              FILTRO
              ================================================= */}

          <div className="filtro-eventos">

            <input
              type="text"
              placeholder="Buscar evento..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />

          </div>


          {/* =================================================
              FORMULARIO
              ================================================= */}

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

                {/* DOMINIO REGISTRADO */}

                <div className="campo-evento">

                  <label>
                    Dominio registrado
                  </label>

                  <input
                    type="text"
                    placeholder="empresa.com"
                    value={dominioRegistrado}
                    onChange={(e) => setDominioRegistrado(e.target.value)}
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


              {/* =================================================
                  BOTONES DEL FORMULARIO
                  ================================================= */}

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


          {/* =================================================
              TABLA
              ================================================= */}

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


                    {/* BOTÓN DETALLE */}

                    <td>

                      <button
                        className="btn-detalle"
                        onClick={() => {

                          setEventoSeleccionado(evento)

                          setVista("detalle")

                        }}
                      >
                        Ver Detalle
                      </button>

                    </td>


                  </tr>

                ))}

              </tbody>

            </table>

          </div>


        </>

      )}

      {/* =====================================================
          VISTA DE DETALLE
          ===================================================== */}

      {vista === "detalle" && eventoSeleccionado && (
        <div className="detalle-container">

          {/* BOTÓN VOLVER */}
          <button
            className="btn-volver"
            onClick={() => {
              setVista("lista")
              setEventoSeleccionado(null)
            }}
          >
            ← Volver a eventos
          </button>


          {/* INFORMACIÓN DEL EVENTO */}
          <div className="detalle-header">

            <p>
              <strong>Id del evento:</strong>{" "}
              {eventoSeleccionado.codigo}
            </p>

            <h1>
              Posible Business Email Compromise (BEC)
            </h1>

          </div>


          {/* 5 OPCIONES */}
          <div className="detalle-tabs">

            <button
              className={
                seccionDetalle === "Resumen"
                  ? "tab-activa"
                  : ""
              }
              onClick={() => setSeccionDetalle("Resumen")}
            >
              Resumen
            </button>

            <button
              className={
                seccionDetalle === "Análisis técnico"
                  ? "tab-activa"
                  : ""
              }
              onClick={() => setSeccionDetalle("Análisis técnico")}
            >
              Análisis técnico
            </button>

            <button
              className={
                seccionDetalle === "Contexto factoring"
                  ? "tab-activa"
                  : ""
              }
              onClick={() => setSeccionDetalle("Contexto factoring")}
            >
              Contexto factoring
            </button>

            <button
              className={
                seccionDetalle === "Impacto financiero"
                  ? "tab-activa"
                  : ""
              }
              onClick={() => setSeccionDetalle("Impacto financiero")}
            >
              Impacto financiero
            </button>

            <button
              className={
                seccionDetalle === "Pasos a seguir"
                  ? "tab-activa"
                  : ""
              }
              onClick={() => setSeccionDetalle("Pasos a seguir")}
            >
              Pasos a seguir
            </button>

          </div>


          {/* DOS TARJETAS */}
          <div className="detalle-cards">


            {/* =================================================
                TARJETA 1
                ================================================= */}

            <div className="detalle-card">

              <h2>
                Información del correo
              </h2>

              <div className="detalle-info">

                <p>
                  <strong>Remitente:</strong>
                  {eventoSeleccionado.remitente}
                </p>

                <p>
                  <strong>Asunto:</strong>
                  {eventoSeleccionado.asunto}
                </p>

                <p>
                  <strong>Fecha:</strong>
                  {eventoSeleccionado.fechaHora}
                </p>

                <p>
                  <strong>Para:</strong>
                  {eventoSeleccionado.destinatario}
                </p>

                <p>
                  <strong>Clasificación:</strong>
                  {eventoSeleccionado.clasificacion}
                </p>

                <p>
                  <strong>Estado:</strong>
                  {eventoSeleccionado.estado}
                </p>

                <p>
                  <strong>Adjunto:</strong>
                  {eventoSeleccionado.adjunto}
                </p>

              </div>


              {/* RISK SCORE */}

              <div className="risk-score">

                <h3>
                  Risk Score
                </h3>

                <div className="risk-circle">

                  <span>
                    {eventoSeleccionado.riskScore}
                  </span>

                  <small>
                    /100
                  </small>

                </div>

                <p>
                  Nivel de riesgo
                </p>

                <strong className="nivel-riesgo">
                  {eventoSeleccionado.clasificacion}
                </strong>

              </div>


              {/* MOTIVO */}

              <div className="motivo-container">

                <label htmlFor="motivo">
                  Motivo
                </label>

                <textarea
                  id="motivo"
                  placeholder="Escriba el motivo..."
                  rows="3"
                ></textarea>

                <button className="btn-guardar-motivo">
                  Guardar motivo
                </button>

              </div>

            </div>


            {/* TARJETA 2 */}

<div className="detalle-card">

  <h2>
    Resumen del análisis
  </h2>


  {/* ÁREA PARA ESCRIBIR ANÁLISIS */}

<div className="analisis-input">

  <div className="analisis-icono">
    ✉
  </div>

  <textarea
    placeholder="Escriba su análisis..."
    rows="3"
    value={analisis}
    onChange={(e) => setAnalisis(e.target.value)}
  />

  <button
    type="button"
    className="btn-guardar-analisis"
    onClick={() => {

      if (analisis.trim() === "") {
        return
      }

      setAnalisisGuardados((anteriores) => [
        ...anteriores,
        analisis
      ])

      setAnalisis("")

    }}
  >
    Guardar
  </button>

</div>


{/* ANÁLISIS GUARDADOS */}

<div className="analisis-lista">

  {analisisGuardados.map((texto, index) => (

    <div
      className="analisis-guardado"
      key={index}
    >

      <div className="analisis-icono">
        ✉
      </div>

      <div className="analisis-texto">

        <strong>
          Análisis {index + 1}
        </strong>

        <p>
          {texto}
        </p>

      </div>

    </div>

  ))}

</div>

{/* RESULTADO */}

<div className="resultado-analisis">

  <h3>
    Resultado
  </h3>

  {!resultadoGuardado ? (

    <>
      <textarea
        placeholder="Escriba el resultado del análisis..."
        rows="4"
        value={resultado}
        onChange={(e) => setResultado(e.target.value)}
      ></textarea>

      <button
        type="button"
        className="btn-guardar-resultado"
        onClick={() => {

          if (resultado.trim() === "") {
            return
          }

          setResultadoGuardado(resultado)

        }}
      >
        Guardar resultado
      </button>
    </>

  ) : (

    <>

      {/* MISMO CUADRO DE TEXTO CON EL RESULTADO */}

      <textarea
        rows="4"
        value={resultadoGuardado}
        readOnly
      ></textarea>


      {/* MENSAJE DE GUARDADO */}

      <p className="mensaje-guardado">
        ✓ Resultado guardado
      </p>


      {/* OPCIONES */}

      <div className="resultado-acciones">

        <button
          type="button"
          className="btn-editar-resultado"
          onClick={() => {

            setResultado(resultadoGuardado)
            setResultadoGuardado("")

          }}
        >
          Editar
        </button>


        <button
          type="button"
          className="btn-borrar-resultado"
          onClick={() => {

            setResultado("")
            setResultadoGuardado("")

          }}
        >
          Borrar
        </button>

      </div>

    </>

  )}

</div>

</div>

          </div>

        </div>
      )}

    </div>

  )

}

export default Eventos