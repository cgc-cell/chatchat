import { defineStore } from 'pinia'

export const useGlobalInfoStore = defineStore('global', {
  state: () => {
    const storeGlobalInfo = localStorage.getItem('globalInfo')
    return {
      globalInfo: storeGlobalInfo ? JSON.parse(storeGlobalInfo) : {}
    }
  },
  actions: {
    setGlobalInfo(key, value) {
      this.globalInfo[key] = value
    },
    getGlobalInfo(key) {
      return this.globalInfo[key]
    }
  }
})
