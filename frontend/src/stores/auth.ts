import { defineStore } from 'pinia'
import api from '../api/client'

interface LoginResponse {
  token: string
  username: string
  role: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('assetpulse-token') || '',
    username: localStorage.getItem('assetpulse-username') || '',
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async login(username: string, password: string) {
      const { data } = await api.post<LoginResponse>('/api/auth/login', { username, password })
      this.token = data.token
      this.username = data.username
      localStorage.setItem('assetpulse-token', data.token)
      localStorage.setItem('assetpulse-username', data.username)
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('assetpulse-token')
      localStorage.removeItem('assetpulse-username')
    },
  },
})
