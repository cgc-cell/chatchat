import { defineStore } from 'pinia'

export const useMessageCountStore = defineStore('messageCount', {
  state: () => {
    return {
      messageCount: {
        chatCount: 0,
        contactAppltCount: 0
      }
    }
  },
  actions: {
    setCount(key, count, forceUpdate) {
      if (forceUpdate === true) {
        this.messageCount[key] = count
        return
      }

      this.messageCount[key] += count
      if (this.messageCount[key] < 0) {
        this.messageCount[key] = 0
      }
      console.log('count:', this.messageCount[key])
    },
    getCount(key) {
      return this.messageCount[key]
    }
  }
})
