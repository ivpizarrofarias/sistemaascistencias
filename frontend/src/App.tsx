// Componente raíz de React. Configura el enrutamiento y los proveedores globales.
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
import { Layout } from './components/Layout'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { EmployeesPage } from './pages/EmployeesPage'
import { SchedulesPage } from './pages/SchedulesPage'
import { AttendancePage } from './pages/AttendancePage'
import { PermisosAdminPage } from './pages/PermisosAdminPage'
import { PayrollAdminPage } from './pages/PayrollAdminPage'
import { NoAutorizadoPage } from './pages/NoAutorizadoPage'

// Componente de rutas que depende del estado de autenticación.
function AppRoutes() {
  const { isAuthenticated } = useAuth()

  return (
    <Routes>
      {/* Login */}
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />

      {/*
        Rutas protegidas con UN solo Layout (sin duplicación).
        - El ProtectedRoute sin roles solo exige haber iniciado sesión.
        - El Layout se renderiza una única vez y dibuja el menú lateral según el rol.
        - Los ProtectedRoute internos (con roles) solo validan el rol de cada grupo de rutas,
          sin volver a envolver en otro Layout.
        Solo el ADMIN inicia sesión: los empleados marcan con su código en el kiosco/app móvil.
      */}
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/no-autorizado" element={<NoAutorizadoPage />} />

          {/* Solo administrador */}
          <Route element={<ProtectedRoute roles={['ADMIN']} />}>
            <Route path="/empleados" element={<EmployeesPage />} />
            <Route path="/horarios" element={<SchedulesPage />} />
            <Route path="/asistencias" element={<AttendancePage />} />
            <Route path="/permisos" element={<PermisosAdminPage />} />
            <Route path="/remuneraciones" element={<PayrollAdminPage />} />
          </Route>
        </Route>
      </Route>

      {/* Ruta no encontrada */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  )
}
