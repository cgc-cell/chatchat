import axios from 'axios'
import api from './Api'
import { ElLoading } from 'element-plus'
import message from './Message'

const contentTypeForm = 'application/x-www-form-urlencoded;charset=UTF-8' // 表单类型
const contentTypeJson = 'application/json' // json类型
const responseTypeJson = 'json'
let loading = null

const instance = axios.create({
  withCredentials: true, // 跨域请求时发送cookie
  baseURL: (import.meta.env.PROD ? api.proDomain : '') + '/api', // 根据环境变量设置请求地址
  timeout: 10 * 1000
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    config.showLoading = config.showLoading !== undefined ? config.showLoading : true

    if (config.showLoading) {
      loading = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0,0,0,0.7)'
      })
    }
    return config
  },
  (error) => {
    if (error.config && error.config.showLoading && loading) {
      loading.close()
    }
    message.error('请求超时')
    return Promise.reject('发送失败')
  }
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    const { showLoading, responseType, errorCallback, showError = true } = response.config

    if (showLoading && loading) {
      loading.close()
    }

    const responseData = response.data
    // 二进制流直接返回
    if (responseType === 'blob' || responseType === 'arraybuffer') {
      return responseData
    }
    if (responseData.code === 200) {
      // 判断 data 是否为 null，如果为 null 则返回整个 responseData，否则返回 responseData.data
      return responseData.data !== null ? responseData.data : responseData
    } else if (responseData.code === 901) {
      // 登录超时处理
      setTimeout(() => {
        window.ipcRenderer.send('relogin')
      }, 2000)
      return Promise.reject({ showError: true, msg: '登录超时，请重新登录' })
    } else {
      if (errorCallback) {
        errorCallback(responseData)
      }
      return Promise.reject({ showError: showError, msg: responseData.info })
    }
  },
  (error) => {
    if (error.config && error.config.showLoading && loading) {
      loading.close()
    }
    return Promise.reject({ showError: true, msg: '网络错误' })
  }
)

// 请求函数
const request = async (config = {}) => {
  try {
    // console.log('Attempting to send request with config:', config)

    const {
      url,
      params,
      dataType,
      showLoading = true,
      responseType = responseTypeJson,
      showError = true,
      errorCallback
    } = config

    let contentType = dataType === 'json' ? contentTypeJson : contentTypeForm
    let formData = new FormData()

    for (let key in params) {
      formData.append(key, params[key] == undefined ? '' : params[key])
    }

    const token = localStorage.getItem('token')
    let headers = {
      'Content-Type': contentType,
      'X-Requested-With': 'XMLHttpRequest',
      token: token
    }

    const response = await instance.post(url, formData, {
      headers,
      showLoading,
      responseType,
      showError,
      errorCallback
    })

    // console.log('Request sent successfully:', response)

    return response
  } catch (error) {
    // console.log('Request encountered an error:', error)
    if (error.showError) {
      message.error(error.msg)
    }
    return null
  }
}

export default request
