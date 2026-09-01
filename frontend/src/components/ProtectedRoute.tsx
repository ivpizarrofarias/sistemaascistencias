// Ruta protegida: exige autenticación y, opcionalmente, un rol específico.
// Se usa como "layout route" (sin path) en react-router: renderiza <Outlet/> con
// la página hija una vez que se cumplen las condiciones de acceso.
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { Role } from '../types'

interface ProtectedRouteProps {
  roles?: Role[]
}

export function ProtectedRoute({ roles }: ProtectedRouteProps) {
  const { isAuthenticated, role } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    // Redirige al login conservando la ruta a la que se quería acceder.
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (roles && (!role || !roles.includes(role))) {
    // Usuario autenticado pero sin el rol requerido -> página de no autorizado.
    return <Navigate to="/no-autorizado" replace />
  }

  // Renderiza la ruta hija dentro del Outlet.
  return <Outlet />
}
