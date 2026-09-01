// Página de acceso no autorizado.
import { Link } from 'react-router-dom'

export function NoAutorizadoPage() {
  return (
    <div className="container">
      <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
        <h2>Acceso no autorizado</h2>
        <p className="text-muted">No tiene permisos para ver esta sección.</p>
        <Link to="/" className="btn">
          Volver al inicio
        </Link>
      </div>
    </div>
  )
}
