
import '../App.css'
import Eventos from './Eventos'
import { useState } from 'react'
import Configuracion from './Configuracion'

function Dashboard({ usuario, cerrarSesion }) {

  const [pagina, setPagina] = useState("panel")
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

            <button>
              Dashboard
            </button>

            <button onClick={() => setPagina("eventos")}>
              Eventos
            </button>

            <button>
              Alertas
            </button>

            <button>
              Reportes
            </button>

            <button onClick={() => setPagina("configuracion")}>
              Configuracion
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
          <h3>Tickets</h3>
          <p>0</p>
        </div>

        <div className="dashboard-card">
          <h3>Alertas</h3>
          <p>0</p>
        </div>

        <div className="dashboard-card">
          <h3>Eventos</h3>
          <p>0</p>
        </div>

      </div>
    </>

  )}

  {pagina === "eventos" && (
    <Eventos />
  )}

  {pagina === "configuracion" && (
    <Configuracion />
  )}

</main>

    </div>

  )
}

export default Dashboard