import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import '@fontsource/be-vietnam-pro/400.css'
import '@fontsource/be-vietnam-pro/500.css'
import '@fontsource/be-vietnam-pro/600.css'
import '@fontsource/be-vietnam-pro/800.css'
import '@fontsource/jetbrains-mono/500.css'
import './index.css'
import App from './App.tsx'
import { warmUpServer } from './lib/api'
import { AuthProvider } from './lib/auth'
import { ToastProvider } from './lib/toast'

// Danh thuc backend som: ngay khi mo trang, va khi quay lai app (vd mo lai app da cai tren dien thoai).
warmUpServer()
document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'visible') warmUpServer()
})

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false },
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)

// PWA: chi dang ky service worker o ban build that (khi dev se gay rac cache).
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {
      // khong co service worker thi app van chay binh thuong, chi khong mo offline duoc
    })
  })
}
