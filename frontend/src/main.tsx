import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Layout } from './layouts/Layout'
import { AdminLayout } from './layouts/AdminLayout'
import { Home } from './pages/Home'
import { ConcertDetail } from './pages/ConcertDetail'
import { AdminDashboard } from './pages/admin/AdminDashboard'
import { CreateConcert } from './pages/admin/CreateConcert'
import { EditConcert } from './pages/admin/EditConcert'
import { Venues } from './pages/admin/Venues'
import { Vouchers } from './pages/admin/Vouchers'
import { Settings } from './pages/admin/Settings'
import { Login } from './pages/auth/Login'
import { Register } from './pages/auth/Register'
import { Checkout } from './pages/Checkout'
import { MyTickets } from './pages/MyTickets'
import './index.css'
import { Toaster } from 'react-hot-toast'

const queryClient = new QueryClient()

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <Toaster position="top-right" toastOptions={{
        className: '!bg-surface !text-white !border !border-border !shadow-2xl',
        success: { iconTheme: { primary: '#EAB308', secondary: '#1A1A1A' } },
      }} />
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="concerts/:id" element={<ConcertDetail />} />
            <Route path="checkout/:id" element={<Checkout />} />
            <Route path="my-tickets" element={<MyTickets />} />
          </Route>
          
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminDashboard />} />
            <Route path="concerts/new" element={<CreateConcert />} />
            <Route path="concerts/:id/edit" element={<EditConcert />} />
            <Route path="venues" element={<Venues />} />
            <Route path="vouchers" element={<Vouchers />} />
            <Route path="settings" element={<Settings />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>,
)
