import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  // Set base for GitHub Pages project site
  base: '/frontproject-development-serviceApi/',
  // Enable Web Workers support
  worker: {
    format: 'es',
  },
  // Development server proxy for API calls
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
