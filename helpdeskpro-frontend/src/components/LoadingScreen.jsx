import Logo from './Logo'

function LoadingScreen({ mensaje }) {
  return (
    <main className="loading-page" aria-live="polite" aria-busy="true">
      <div className="loading-container" role="status">
        <Logo />
        <div className="loading-indicator" aria-hidden="true">
          <span />
          <span />
          <span />
        </div>
        <p className="loading-message">{mensaje}</p>
      </div>
    </main>
  )
}

export default LoadingScreen
