import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// for local dev (npm run dev) you can proxy /api to the backend
// in docker we won't use this-nginx handles it
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5173,
        proxy: {
            '/api': 'http://localhost:18081'
        }
    },
    build: {
        outDir: 'dist'
    }
})