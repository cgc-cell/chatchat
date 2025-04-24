import { defineStore } from 'pinia'

export const useSysSettingStore = defineStore('sysSettingStore', {
  state: () => {
    return {
      sysSetting: {}
    }
  },
  actions: {
    setSetting(setting) {
      this.sysSetting = setting
    },
    getSetting() {
      return this.sysSetting
    }
  }
})
