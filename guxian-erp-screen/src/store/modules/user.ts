import { defineStore } from 'pinia'
import type { LoginUser } from '@/types/system'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null as LoginUser | null
  }),
  actions: {
    setUser(user: LoginUser) {
      this.userInfo = user
    },
    clearUser() {
      this.userInfo = null
    }
  }
})