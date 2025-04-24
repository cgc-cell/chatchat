const Store = require('electron-store')

const store = new Store()

let userId = null
const initUserId = (id) => {
  userId = id
}
const getUserId = () => {
  return userId
}
const setStore = (key, value) => {
  store.set(key, value)
}
const getStore = (key) => {
  return store.get(key)
}
const setUserData = (key, value) => {
  store.set(userId + key, value)
}
const getUserData = (key) => {
  return store.get(userId + key)
}
const deleteUserData = (key) => {
  store.delete(userId + key)
}

export default {
  setStore,
  getStore,
  initUserId,
  getUserId,
  setUserData,
  getUserData,
  deleteUserData
}
