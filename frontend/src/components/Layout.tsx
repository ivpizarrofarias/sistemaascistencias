// Layout principal con barra lateral de navegación.
// Solo el ADMIN inicia sesión (los empleados marcan con su código en el kiosco/app),
// por lo que el menú es siempre el del panel de administración.
import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const [collapsed, setCollapsed] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const closeMenu = () => setMenuOpen(false)

  return (
    <div className="layout">
      <button
        className="hamburger"
        aria-label="Abrir menú"
        onClick={() => setMenuOpen(!menuOpen)}
      >
        {menuOpen ? '✕' : '☰'}
      </button>

      {menuOpen && <div className="sidebar-overlay" onClick={closeMenu} />}

      <aside className={'sidebar' + (menuOpen ? ' sidebar-open' : '') + (collapsed ? ' sidebar-collapsed' : '')}>
        <div className="sidebar-title">
          <span>{collapsed ? 'SA' : 'Sistema de Asistencias'}</span>
          <button
            className="sidebar-collapse-btn"
            onClick={() => setCollapsed(!collapsed)}
            title={collapsed ? 'Expandir menú' : 'Colapsar menú'}
          >
            {collapsed ? '»' : '«'}
          </button>
        </div>
        <nav className="sidebar-nav">
          <NavLink to="/empleados" className={({ isActive }) => 'sidebar-link' + (isActive ? ' active' : '')} onClick={closeMenu} title="Empleados">
            <span>{collapsed ? 'EMP' : 'Empleados'}</span>
          </NavLink>
          <NavLink to="/horarios" className={({ isActive }) => 'sidebar-link' + (isActive ? ' active' : '')} onClick={closeMenu} title="Horarios">
            <span>{collapsed ? 'HOR' : 'Horarios'}</span>
          </NavLink>
          <NavLink to="/asistencias" className={({ isActive }) => 'sidebar-link' + (isActive ? ' active' : '')} onClick={closeMenu} title="Asistencia">
            <span>{collapsed ? 'ASI' : 'Asistencia'}</span>
          </NavLink>
          <NavLink to="/permisos" className={({ isActive }) => 'sidebar-link' + (isActive ? ' active' : '')} onClick={closeMenu} title="Permisos">
            <span>{collapsed ? 'PER' : 'Permisos'}</span>
          </NavLink>
          <NavLink to="/remuneraciones" className={({ isActive }) => 'sidebar-link' + (isActive ? ' active' : '')} onClick={closeMenu} title="Remuneraciones">
            <span>{collapsed ? 'REM' : 'Remuneraciones'}</span>
          </NavLink>
        </nav>
        <div className="sidebar-footer">
          {user?.fullName ? <div>{user.fullName}</div> : user?.username ? <div>{user.username}</div> : null}
        </div>
      </aside>

      <div className="main">
        <header className="topbar">
          <div>Panel de administración</div>
          <div className="flex align-center gap-2">
            <span className="topbar-user">{user?.fullName ?? user?.username}</span>
            <button className="btn btn-sm btn-secondary" onClick={handleLogout}>
              Salir
            </button>
          </div>
        </header>
        <main className="container">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
