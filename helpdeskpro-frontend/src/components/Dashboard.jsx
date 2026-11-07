
import '../App.css'
import Eventos from './Eventos'
import { useState, useEffect } from 'react'
import Configuracion from './Configuracion'
import apiClient from '../api/apiClient'

function Dashboard({ usuario, cerrarSesion, esAdministrador }) {

  const [pagina, setPagina] = useState("panel")
  const [eventos, setEventos] = useState([])
  const eventosAltoRiesgo = eventos.filter((evento) => {
  const score = Number(evento.riskScore)

  return evento.riskScore != null &&
    Number.isFinite(score) &&
    score >= 51 &&
    score <= 100
}).length

const eventosCriticos = eventos.filter((evento) => {
  const score = Number(evento.riskScore)

  return evento.riskScore != null &&
    Number.isFinite(score) &&
    score >= 76 &&
    score <= 100
}).length

const eventosPendientes = eventos.filter((evento) => {
  return evento.estado?.trim().toLowerCase() === "revisar"
}).length

const scoresValidos = eventos
  .filter((evento) => {
    const score = Number(evento.riskScore)

    return evento.riskScore != null &&
      evento.riskScore !== "" &&
      Number.isFinite(score) &&
      score >= 0 &&
      score <= 100
  })
  .map((evento) => Number(evento.riskScore))

const riskScoreGeneral = scoresValidos.length > 0
  ? Math.round(
      scoresValidos.reduce((total, score) => total + score, 0) /
      scoresValidos.length
    )
  : null

  const eventosPorClasificacion = {
  bajo: 0,
  medio: 0,
  alto: 0,
  critico: 0
}

eventos.forEach((evento) => {
  if (evento.riskScore == null || evento.riskScore === "") {
    return
  }

  const score = Number(evento.riskScore)

  if (!Number.isFinite(score) || score < 0 || score > 100) {
    return
  }

  if (score <= 25) {
    eventosPorClasificacion.bajo++
  } else if (score <= 50) {
    eventosPorClasificacion.medio++
  } else if (score <= 75) {
    eventosPorClasificacion.alto++
  } else {
    eventosPorClasificacion.critico++
  }
})


const totalEventosClasificados = Object.values(
  eventosPorClasificacion
).reduce((total, cantidad) => total + cantidad, 0)

const porcentajesRiesgo = {
  bajo: totalEventosClasificados > 0
    ? (eventosPorClasificacion.bajo / totalEventosClasificados) * 100
    : 0,

  medio: totalEventosClasificados > 0
    ? (eventosPorClasificacion.medio / totalEventosClasificados) * 100
    : 0,

  alto: totalEventosClasificados > 0
    ? (eventosPorClasificacion.alto / totalEventosClasificados) * 100
    : 0,

  critico: totalEventosClasificados > 0
    ? (eventosPorClasificacion.critico / totalEventosClasificados) * 100
    : 0
}


const eventosRecientes = [...eventos]
  .sort((a, b) => {
    const fechaA = a.fechaHora
      ? new Date(a.fechaHora).getTime()
      : 0

    const fechaB = b.fechaHora
      ? new Date(b.fechaHora).getTime()
      : 0

    const tiempoA = Number.isFinite(fechaA) ? fechaA : 0
    const tiempoB = Number.isFinite(fechaB) ? fechaB : 0

    return tiempoB - tiempoA
  })
  .slice(0, 5)

useEffect(() => {
  let activo = true

  async function cargarEventos() {
    try {
      const respuesta = await apiClient.get('/eventos')

      if (activo) {
        setEventos(Array.isArray(respuesta.data) ? respuesta.data : [])
      }
    } catch (error) {
      console.error('Error al cargar eventos del Dashboard:', error)
    }
  }

  cargarEventos()

  const intervalo = setInterval(cargarEventos, 5000)

  return () => {
    activo = false
    clearInterval(intervalo)
  }
}, [])

  return (

    <div className="dashboard">

      {/* BARRA LATERAL */}

      <aside className="sidebar">

        <div>

          {/* LOGO */}

          <div className="logo-dashboard">
              <img src="/src/assets/logo.png" alt="Logo HelpDeskPro" />
          </div>


          {/* TÍTULO */}

          <h2>Plataforma Principal</h2>


          {/* MENÚ */}

          <nav className="menu-dashboard">

            <button
            className={pagina === "panel" ? "menu-activo" : ""}
            onClick={() => setPagina("panel")}
          >
            Dashboard
          </button>

            <button
            className={pagina === "eventos" ? "menu-activo" : ""}
            onClick={() => setPagina("eventos")}
          >
            Eventos
          </button>

            <button>
              Alertas
            </button>

            <button>
              Reportes
            </button>

            <button
            className={pagina === "configuracion" ? "menu-activo" : ""}
            onClick={() => setPagina("configuracion")}
          >
            Configuración
          </button>

          </nav>

        </div>


        {/* PARTE INFERIOR */}

        <div className="sidebar-bottom">

          <p className="usuario-nombre">
            {usuario.nombre}
          </p>

          <p className="usuario-correo">
            {usuario.correo}
          </p>

          <p className="usuario-correo">
            {esAdministrador ? "Administrador" : "Usuario"}
          </p>

          <button
            className="cerrar-sesion"
            onClick={cerrarSesion}
          >
            CERRAR SESIÓN
          </button>

        </div>

      </aside>


      {/* CONTENIDO DEL DASHBOARD */}

      <main className="dashboard-content">

  {pagina === "panel" && (

    <>
      <h1>Dashboard General</h1>

      <p className="bienvenida">
        Bienvenido a HelpDeskPro
      </p>

      <div className="dashboard-cards">

        <div className="dashboard-card">
        <h3>Eventos críticos</h3>
        <p>{eventosCriticos}</p>
      </div>

        <div className="dashboard-card">
        <h3>Eventos de alto riesgo</h3>
        <p>{eventosAltoRiesgo}</p>
      </div>

        <div className="dashboard-card">
        <h3>Eventos analizados</h3>
        <p>{eventos.length}</p>
      </div>

        
        <div className="dashboard-card">
          <h3>Pérdida estimada</h3>
          <p className="perdida-estimada-valor">
            Sin datos
          </p>
        </div>


        <div className="dashboard-card">
        <h3>Risk Score general</h3>
        <p>
          {riskScoreGeneral !== null
            ? `${riskScoreGeneral}/100`
            : "Sin datos"}
        </p>
      </div>


      </div>

<div className="dashboard-graficos">

 <div className="dashboard-grafico">
  <h2>Eventos por clasificación</h2>

  <div className="grafico-barras">
    {[
      { nombre: "Bajo", cantidad: eventosPorClasificacion.bajo, color: "#22c55e" },
      { nombre: "Medio", cantidad: eventosPorClasificacion.medio, color: "#eab308" },
      { nombre: "Alto", cantidad: eventosPorClasificacion.alto, color: "#f97316" },
      { nombre: "Crítico", cantidad: eventosPorClasificacion.critico, color: "#ef4444" }
    ].map((item) => {
      const maximo = Math.max(
        1,
        ...Object.values(eventosPorClasificacion)
      )

      return (
        <div className="grafico-columna" key={item.nombre}>
          <span>{item.cantidad}</span>

          <div className="grafico-barra-contenedor">
            <div
              className="grafico-barra"
              style={{
                height: `${(item.cantidad / maximo) * 100}%`,
                backgroundColor: item.color
              }}
            />
          </div>

          <span>{item.nombre}</span>
        </div>
      )
    })}
  </div>

 </div>

<div className="dashboard-grafico">
  <h2>Distribución de riesgos</h2>

  {totalEventosClasificados > 0 ? (
    <>
      <div
        className="grafico-circular"
        style={{
          background: `conic-gradient(
            #22c55e 0% ${porcentajesRiesgo.bajo}%,
            #eab308 ${porcentajesRiesgo.bajo}% ${porcentajesRiesgo.bajo + porcentajesRiesgo.medio}%,
            #f97316 ${porcentajesRiesgo.bajo + porcentajesRiesgo.medio}% ${porcentajesRiesgo.bajo + porcentajesRiesgo.medio + porcentajesRiesgo.alto}%,
            #ef4444 ${porcentajesRiesgo.bajo + porcentajesRiesgo.medio + porcentajesRiesgo.alto}% 100%
          )`
        }}
      >
        <div className="grafico-circular-centro">
          <strong>{totalEventosClasificados}</strong>
          <span>Eventos</span>
        </div>
      </div>

      <div className="grafico-leyenda">
        <span>Bajo: {eventosPorClasificacion.bajo}</span>
        <span>Medio: {eventosPorClasificacion.medio}</span>
        <span>Alto: {eventosPorClasificacion.alto}</span>
        <span>Crítico: {eventosPorClasificacion.critico}</span>
      </div>
    </>
  ) : (
    <p>Sin datos de riesgo disponibles</p>
  )}
</div>


<div className="dashboard-grafico">
  <h2>Pérdida potencial estimada</h2>

  <div className="grafico-sin-datos">
    <p>Información financiera no disponible</p>
    <span>
      El impacto financiero se mostrará cuando
      los eventos estén vinculados con las
      facturas y sus montos.
    </span>
  </div>
</div>


<div className="dashboard-eventos-recientes">

  <div className="eventos-recientes-header">
    <h2>Eventos recientes</h2>

    <button
      type="button"
      onClick={() => setPagina("eventos")}
    >
      Ver todos los eventos →
    </button>
  </div>

  <div className="eventos-recientes-tabla-contenedor">
    <table className="eventos-recientes-tabla">
      <thead>
        <tr>
          <th>ID</th>
          <th>Remitente</th>
          <th>Asunto</th>
          <th>Clasificación</th>
          <th>Risk Score</th>
          <th>Importancia</th>
        </tr>
      </thead>

      <tbody>
        {eventosRecientes.map((evento) => {
          const score = Number(evento.riskScore)

          const scoreValido =
            evento.riskScore != null &&
            evento.riskScore !== "" &&
            Number.isFinite(score) &&
            score >= 0 &&
            score <= 100

          const importancia = !scoreValido
            ? "Sin evaluar"
            : score <= 25
              ? "Bajo"
              : score <= 50
                ? "Medio"
                : score <= 75
                  ? "Alto"
                  : "Crítico"

          return (
            <tr key={evento.id}>
              <td>{evento.codigo || "—"}</td>
              <td>{evento.remitente || "—"}</td>
              <td>{evento.asunto || "—"}</td>
              <td>{evento.clasificacion || "—"}</td>
              <td>
                {scoreValido ? `${score}/100` : "—"}
              </td>
              <td>
                <span className={`importancia-${importancia.toLowerCase().replace("í", "i").replace(" ", "-")}`}>
                  {importancia}
                </span>
              </td>
            </tr>
          )
        })}

        {eventosRecientes.length === 0 && (
          <tr>
            <td colSpan={6}>
              No hay eventos registrados.
            </td>
          </tr>
        )}
      </tbody>
    </table>
  </div>

</div>


</div>           

    </>

  )}

  {pagina === "eventos" && (
    <Eventos />
  )}

  {pagina === "configuracion" && (
    <Configuracion esAdministrador={esAdministrador} />
  )}

</main>

    </div>

  )
}

export default Dashboard