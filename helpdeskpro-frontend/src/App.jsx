import { useAuth } from 'react-oidc-context'
import './App.css'
import Dashboard from './components/Dashboard'
import Logo from './components/Logo'

const logoutUri = import.meta.env.VITE_COGNITO_LOGOUT_URI
const clientId = import.meta.env.VITE_COGNITO_CLIENT_ID
const cognitoDomain = import.meta.env.VITE_COGNITO_DOMAIN

function App() {
  const auth = useAuth()
  const profile = auth.user?.profile ?? {}
  const usuario = {
    nombre: profile.name ?? profile.preferred_username ?? profile.email ?? profile.sub ?? 'Usuario',
    correo: profile.email ?? profile.preferred_username ?? 'Correo no disponible',
  }

  async function cerrarSesion() {
    await auth.removeUser()

    const logoutUrl = new URL(`${cognitoDomain}/logout`)
    logoutUrl.searchParams.set('client_id', clientId)
    logoutUrl.searchParams.set('logout_uri', logoutUri)
    window.location.assign(logoutUrl.toString())
  }

  if (auth.isLoading) {
    return <p>Cargando sesión...</p>
  }

  if (auth.error) {
    return <p>No se pudo iniciar sesión: {auth.error.message}</p>
  }

  if (auth.isAuthenticated) {
    return <Dashboard usuario={usuario} cerrarSesion={cerrarSesion} />
  }

  return (
    <div className="login-page">
      <div className="login-container">
        <Logo />
        <h1>HelpDeskPro</h1>
        <p className="subtitle">Sistema de soporte TI Alonso</p>
        <button type="button" onClick={() => auth.signinRedirect()}>
          Iniciar sesión o registrarse
        </button>
      </div>
    </div>
  )
}

export default App