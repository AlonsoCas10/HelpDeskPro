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
  },
})
