// Cliente HTTP centralizado del frontend.
// Usa axios y añade automáticamente el token JWT (si existe) a cada petición.
// Todas las rutas /api se redirigen al backend gracias al proxy de Vite
// (ver vite.config.ts) en desarrollo.
import axios from 'axios'

const TOKEN_KEY = 'sistema_asistencias_token'
const USER_KEY = 'sistema_asistencias_user'

export const authStorage = {
  getToken: (): string | null => localStorage.getItem(TOKEN_KEY),
  setToken: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  getUser: () => {
    try {
      const raw = localStorage.getItem(USER_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  },
  setUser: (user: unknown) => localStorage.setItem(USER_KEY, JSON.stringify(user)),
  clear: () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  },
}

export const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Interceptor de petición: adjunta el Bearer token si existe.
api.interceptors.request.use((config) => {
  const token = authStorage.getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Interceptor de respuesta: si el servidor devuelve 401, cerramos la sesión.
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      authStorage.clear()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)
