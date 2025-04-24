import { defineStore } from 'pinia'
export const useContactStateStore = defineStore('contactState', {
  state: () => {
    return {
      contactReload: null,
      delContactId: null
    }
  },
  actions: {
    setContactReload(contactReload) {
      this.contactReload = contactReload
    },
    setDelContactId(delContactId) {
      this.delContactId = delContactId
    }
  }
})
