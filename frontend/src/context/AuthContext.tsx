// Contexto de autenticación.
// Provee el usuario actual, su rol y las funciones login/logout a toda la app.
import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../api/auth'
import { authStorage } from '../api/client'
import type { AuthResponse, LoginRequest, Role } from '../types'

interface AuthContextValue {
  token: string | null
  user: AuthResponse | null
  role: Role | null
  employeeId: number | null
  isAuthenticated: boolean
  login: (payload: LoginRequest) => Promise<AuthResponse>
  logout: () => void
}

// Contexto con valor por defecto nulo; se resuelve con AuthProvider.
const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  // Estado inicial desde el almacenamiento local (persiste sesión al recargar).
  const [user, setUser] = useState<AuthResponse | null>(() => authStorage.getUser())

  const value = useMemo<AuthContextValue>(() => {
    const token = user?.token ?? null
    return {
      token,
      user,
      role: user?.role ?? null,
      employeeId: user?.employeeId ?? null,
      isAuthenticated: Boolean(token),
      login: async (payload) => {
        const res = await authApi.login(payload)
        authStorage.setToken(res.token)
        authStorage.setUser(res)
        setUser(res)
        return res
      },
      logout: () => {
        authStorage.clear()
        setUser(null)
      },
    }
  }, [user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// Hook de acceso al contexto.
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return ctx
}
