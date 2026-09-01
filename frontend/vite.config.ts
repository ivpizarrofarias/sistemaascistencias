import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Configuración de Vite para el frontend del sistema de asistencias.
// - plugin-react habilita el refresh rápido y el soporte JSX.
// - server.proxy redirige las peticiones /api al backend Spring Boot (puerto 8080),
//   de modo que el frontend en desarrollo no necesita CORS configurado manualmente.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
