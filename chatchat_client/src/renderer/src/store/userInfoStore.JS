import { defineStore } from 'pinia'

export const useUserInfoStore = defineStore('userInfo', {
  state: () => {
    const storedUserInfo = localStorage.getItem('userInfo')
    return {
      userInfo: storedUserInfo ? JSON.parse(storedUserInfo) : {}
    }
  },
  actions: {
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    getUserInfo() {
      return this.userInfo
    }
  }
})
