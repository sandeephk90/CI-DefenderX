import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import api from '../services/api'

interface AuthState {
  token: string | null
  username: string | null
  role: string | null
  fullName: string | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      username: null,
      role: null,
      fullName: null,

      login: async (username, password) => {
        const { data } = await api.post('/auth/login', { username, password })
        localStorage.setItem('token', data.token)
        set({
          token: data.token,
          username: data.username,
          role: data.role,
          fullName: data.fullName,
        })
      },

      logout: () => {
        localStorage.removeItem('token')
        set({ token: null, username: null, role: null, fullName: null })
      },

      isAuthenticated: () => !!get().token,
    }),
    { name: 'auth-storage' }
  )
)
