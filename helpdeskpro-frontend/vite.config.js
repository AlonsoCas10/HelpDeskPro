import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Si 5173 está ocupado, fallar en vez de saltar a 5174 en silencio.
    // Además, VITE_COGNITO_REDIRECT_URI apunta a este puerto: en otro puerto el login de Cognito no volvería aquí.
    strictPort: true,
    // Permite abrir la aplicación desde un túnel temporal de Cloudflare.
    allowedHosts: ['.trycloudflare.com'],
    // Reenvía /api al backend: así un solo túnel sirve la aplicación y la API,
    // y quien abra el enlace desde otro equipo también recibe los datos.
    proxy: {
      '/api': {
        target: process.env.API_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
