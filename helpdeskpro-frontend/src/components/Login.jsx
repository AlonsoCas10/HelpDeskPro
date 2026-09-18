import { useState } from 'react'
import '../App.css'
import Logo from './Logo'
import Registro from './Registro'
import Dashboard from './Dashboard'
import axios from 'axios'

function Login() {

  // ==========================================
  // ESTADOS
  // ==========================================

  const [correo, setCorreo] = useState("")
  const [password, setPassword] = useState("")
  const [mensaje, setMensaje] = useState("")
  const [mostrarRegistro, setMostrarRegistro] = useState(false)

  const [usuario, setUsuario] = useState(null)

  if (mostrarRegistro) {
    return <Registro />
  }

  if (usuario) {
  return (
    <Dashboard
      usuario={usuario}
      cerrarSesion={() => setUsuario(null)}
    />
  )
}


  // ==========================================
  // FUNCIÓN PARA INICIAR SESIÓN
  // ==========================================
async function iniciarSesion(e) {

  e.preventDefault()

  if (correo === "" || password === "") {

    setMensaje("Complete todos los campos")

    return
  }

  try {

    const respuesta = await axios.post(
      "http://localhost:8080/usuarios/login",
      {
        correo: correo,
        contraseña: password
      }
    )

    console.log("Usuario autenticado:", respuesta.data)

    // Guardar los datos del usuario
    setUsuario(respuesta.data)

  } catch (error) {

    console.error(error)

    if (error.response) {

      setMensaje(error.response.data)

    } else {

      setMensaje("No se pudo conectar con el servidor")

    }
  }
}

  // ==========================================
  // INTERFAZ
  // ==========================================

  return (
    <div className="login-page">

      <div className="login-container">

        {/* LOGO */}

        <Logo />


        {/* TÍTULO */}

        <h1>HelpDeskPro</h1>

        <p className="subtitle">
          Sistema de soporte TI Alonso
        </p>


        {/* FORMULARIO */}

        <form onSubmit={iniciarSesion}>

          {/* CORREO */}

          <div className="input-group">

            <label>
              Correo electrónico
            </label>

            <input
              type="email"
              placeholder="ejemplo@empresa.com"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
            />

          </div>


          {/* CONTRASEÑA */}

          <div className="input-group">

            <label>
              Contraseña
            </label>

            <input
              type="password"
              placeholder="Ingrese su contraseña"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />

          </div>


          {/* BOTÓN */}

          <button type="submit">
            Iniciar sesión
          </button>

          <button
            type="button"
            onClick={() => setMostrarRegistro(true)}
            >
            ¿No tienes una cuenta? Regístrate
          </button>


        </form>


        {/* MENSAJE */}

        {mensaje && (
          <p className="mensaje">
            {mensaje}
          </p>
        )}


        {/* PIE */}

        <p className="footer-text">
          © 2026 HelpDeskPro
        </p>

      </div>

    </div>
  )
}

export default Login
