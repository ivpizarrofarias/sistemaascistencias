// Página de inicio de sesión.
import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      // Se recortan espacios en blanco del usuario para evitar errores por
      // autocompletado que inserta espacios accidentales.
      // Solo el admin inicia sesión (los empleados marcan con su código).
      await login({ username: username.trim(), password })
      navigate('/empleados', { replace: true })
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const anyErr = err as any
      // Si no hay respuesta del servidor (red caída o backend apagado), avisamos claro.
      if (!anyErr?.response) {
        setError('No se pudo conectar con el servidor. Verifica que el backend esté corriendo en el puerto 8080.')
      } else {
        setError(anyErr?.response?.data?.message ?? 'Usuario o contraseña incorrectos')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={handleSubmit}>
        <h2 className="login-title">Iniciar sesión</h2>
        <p className="text-muted">Sistema de Asistencias y Liquidaciones</p>

        {error && <div className="alert alert-error">{error}</div>}

        <div className="form-group">
          <label htmlFor="username">Usuario</label>
          <input
            id="username"
            className="form-control"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Contraseña</label>
          <input
            id="password"
            type="password"
            className="form-control"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </div>

        <button className="btn" type="submit" disabled={loading} style={{ width: '100%' }}>
          {loading ? 'Ingresando...' : 'Ingresar'}
        </button>
      </form>
    </div>
  )
}
