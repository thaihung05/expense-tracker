import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Request /api/... chuyen sang backend Spring Boot (cong 8080): chung "dia chi" nen khong lo CORS khi dev.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
