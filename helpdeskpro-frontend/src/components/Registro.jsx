import { useState } from 'react'
import '../App.css'
import Login from './Login'
import axios from 'axios'


function Registro() {

  const [nombre, setNombre] = useState("")
  const [correo, setCorreo] = useState("")
  const [password, setPassword] = useState("")
  const [mensaje, setMensaje] = useState("")
  const [volverLogin, setVolverLogin] = useState(false)

  if (volverLogin) {
    return <Login />
  }

async function registrarUsuario(e) {

  e.preventDefault()

  if (nombre === "" || correo === "" || password === "") {

    setMensaje("Complete todos los campos")

    return
  }

  try {

    const respuesta = await axios.post(
      "http://localhost:8080/usuarios/registro",
      {
        nombre: nombre,
        correo: correo,
        contraseña: password
      }
    )

    console.log(respuesta.data)

    setMensaje("Usuario registrado correctamente")

    setNombre("")
    setCorreo("")
    setPassword("")

  } catch (error) {

    console.log(error)

    if (error.response) {
      setMensaje(error.response.data)
    } else {
      setMensaje("Error al conectar con el servidor")
    }
  }
}


  return (
    <div className="login-page">

      <div className="login-container">

        <h1>Crear cuenta</h1>

        <p className="subtitle">
          Regístrate en HelpDeskPro
        </p>


        <form onSubmit={registrarUsuario}>

          <div className="input-group">

            <label>
              Nombre completo
            </label>

            <input
              type="text"
              placeholder="Ingrese su nombre"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
            />

          </div>


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


          <div className="input-group">

            <label>
              Contraseña
            </label>

            <input
              type="password"
              placeholder="Ingrese una contraseña"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />

          </div>


          <button type="submit">
            Registrarse
          </button>

          <button
            type="button"
            onClick={() => setVolverLogin(true)}
            >
            ¿Ya tienes una cuenta? Inicia sesión
          </button>

        </form>


        {mensaje && (
          <p className="mensaje">
            {mensaje}
          </p>
        )}

      </div>

    </div>
  )
}

export default Registro