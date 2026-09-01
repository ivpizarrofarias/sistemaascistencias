// Servicios de autenticación.
import { api } from './client'
import type { AuthResponse, LoginRequest } from '../types'

export const authApi = {
  login: (payload: LoginRequest) =>
    api.post<AuthResponse>('/auth/login', payload).then((r) => r.data),
}
