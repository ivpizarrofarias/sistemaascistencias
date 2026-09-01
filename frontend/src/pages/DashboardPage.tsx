// Página de inicio: redirige automáticamente a la sección de empleados.
// Solo el ADMIN inicia sesión en el sistema, así que siempre se va a /empleados.
import { Navigate } from 'react-router-dom'

export function DashboardPage() {
  return <Navigate to="/empleados" replace />
}
