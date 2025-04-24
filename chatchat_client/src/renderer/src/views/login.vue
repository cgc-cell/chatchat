<template>
  <div class="login-panel">
    <div class="title drag">Wechat</div>
    <div v-if="showLoading" class="loding-panel">
      <img src="../assets/img/loading.gif" alt="" />
    </div>
    <div v-else class="login-form">
      <div class="error-msg">{{ errorMsg }}</div>
      <el-form ref="formDataRef" :model="formData" label-width="0px" @submit.prevent>
        <!--input输入-->
        <el-form-item prop="email">
          <div class="email-panel">
            <el-input
              v-model.trim="formData.email"
              clearable
              placeholder="请输入邮箱地址"
              max-length="30"
              @focus="clearInput"
            >
              <template #prefix>
                <span class="iconfont icon-email"></span>
              </template>
            </el-input>
            <el-dropdown v-if="IsLogin && localUserList.length > 0" trigger="click">
              <span class="iconfont icon-down"></span>
              <template #dropdown>
                <el-dropdown-item v-for="item in localUserList" :key="item">
                  <div class="email-select" @click="seleceEmail(item.email)">{{ item.email }}</div>
                </el-dropdown-item>
              </template>
            </el-dropdown>
          </div>
        </el-form-item>
        <el-form-item v-if="!IsLogin" prop="nickName">
          <el-input
            v-model.trim="formData.nickName"
            clearable
            placeholder="请输入昵称"
            max-length="15"
            @focus="clearInput"
          >
            <template #prefix>
              <span class="iconfont icon-user-nick"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model.trim="formData.password"
            clearable
            placeholder="请输入密码"
            show-password
            @focus="clearInput"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item v-if="!IsLogin" prop="rePassword">
          <el-input
            v-model.trim="formData.rePassword"
            clearable
            placeholder="请再次输入密码"
            @focus="clearInput"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="checkcode">
          <div class="check-code-panel">
            <el-input
              v-model.trim="formData.checkcode"
              clearable
              placeholder="请输入验证码"
              @focus="clearInput"
            >
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img
              :src="checkcodeUrl"
              alt="验证码"
              style="cursor: pointer; width: 70%"
              class="checkcode"
              @click="changeCheckcode"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" @click="submit">{{
            IsLogin ? '登陆' : '注册'
          }}</el-button>
        </el-form-item>
      </el-form>
      <div class="bottom-link">
        <span class="a-link" @click="changeOpType">{{ IsLogin ? '没有账号?' : '已有账号?' }}</span>
      </div>
    </div>
    <WinOp :show-set-top="false" :show-min="false" :show-max="false" :close-type="0"></WinOp>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance, nextTick, onMounted, onUnmounted } from 'vue'
import md5 from 'js-md5'
import { useUserInfoStore } from '@/store/userInfoStore.js'
import { useRouter } from 'vue-router'
import WinOp from '../components/WinOp.vue'
const router = useRouter()
const { proxy } = getCurrentInstance()
const formData = ref({})
const formDataRef = ref()
const userInfoStore = useUserInfoStore()
const IsLogin = ref(true)
// 切换登录注册
const changeOpType = () => {
  window.ipcRenderer.send('LoginorRegister', !IsLogin.value)
  IsLogin.value = !IsLogin.value
  //nextTick用于在下一个 DOM 更新循环结束之后执行延迟回调.在 Vue 中，数据的更改（例如 IsLogin.value = !IsLogin.value）不会立即反映到 DOM 中，而是会在下一个事件循环中批量更新。这意味着如果您在数据更改后立即尝试操作依赖于最新 DOM 状态的元素，可能会遇到问题，因为 DOM 还未更新。
  nextTick(() => {
    formDataRef.value.resetFields() //resetFields 通常是表单组件（例如，Element Plus 的 el-form 组件）提供的方法，用于重置表单的所有字段到其初始状态。
    formData.value = {}
    errorMsg.value = null
  })
  changeCheckcode()
}
//验证码刷新模块
const checkcodeUrl = ref(null)
const changeCheckcode = async () => {
  let result = await proxy.Request({
    url: proxy.api.checkCode
  })
  if (!result) {
    return
  }
  // console.log(result)

  checkcodeUrl.value = result.verifyCode

  localStorage.setItem('checkcodeKey', result.verifyCodeKey)
}
changeCheckcode()
const errorMsg = ref(null)
//提交行为
const showLoading = ref(false)
const submit = async () => {
  //校验表单,校验表单思路：第一步在点击提交按钮时进行从上到下依次校验,校验函数传入类型，表单值和错误提示，在校验函数中首先判断空操作，，其中空操作挂在全局上
  //通过校验错误，将错误信息挂在上方盒子里，然后通过第二层正则校验，正则校验挂在全局上，传入校验类型和值，如果校验不通过，将错误信息挂在上方盒子里。
  clearInput()
  if (!checkValue('checkEmail', formData.value.email, '请输入正确的邮箱地址')) {
    return
  }
  if (!IsLogin.value && !checkValue(null, formData.value.nickName, '请输入昵称')) {
    return
  }
  if (!checkValue('checkPassword', formData.value.password, '请输入8-18位字母数字组合密码')) {
    return
  }
  if (!IsLogin.value && formData.value.password !== formData.value.rePassword) {
    errorMsg.value = '两次密码不一致'
    return
  }
  if (!checkValue(null, formData.value.checkcode, '请输入验证码')) {
    return
  }
  if (IsLogin.value) {
    showLoading.value = true
  }
  await nextTick()
  let result = await proxy.Request({
    url: IsLogin.value ? proxy.api.login : proxy.api.register,
    showLoading: IsLogin.value ? false : true,
    showError: false,
    params: {
      email: formData.value.email,
      password: IsLogin.value ? md5(formData.value.password) : formData.value.password,
      verifyCode: formData.value.checkcode,
      nickname: formData.value.nickName,
      verifyCodeKey: localStorage.getItem('checkcodeKey')
    },
    errorCallback: (responce) => {
      showLoading.value = false
      errorMsg.value = responce.info
      changeCheckcode()
    }
  })

  if (!result) {
    return
  }
  //此时的result是没有data的
  if (IsLogin.value) {
    // console.log(result)
    userInfoStore.setUserInfo(result)
    localStorage.setItem('token', result.token)
    const widowWidth = window.screen.width
    const widowHeight = window.screen.height
    router.push('/main')
    window.ipcRenderer.send('openChat', {
      email: formData.value.email,
      token: result.token,
      userId: result.userId,
      nickName: result.nickName,
      admin: result.admin,
      widowWidth,
      widowHeight
    })

    window.ipcRenderer.send('getLocalStore', 'devWsDomain')
  } else {
    changeOpType()
    proxy.message.success('注册成功')
  }
}
const localUserList = ref([])
const init = () => {
  window.ipcRenderer.send('setLocalStore', { key: 'proDomain', value: proxy.api.proDomain })
  window.ipcRenderer.send('setLocalStore', { key: 'devDomain', value: proxy.api.devDomain })
  window.ipcRenderer.send('setLocalStore', { key: 'proWsDomain', value: proxy.api.proWsDomain })
  window.ipcRenderer.send('setLocalStore', { key: 'devWsDomain', value: proxy.api.devWsDomain })
  window.ipcRenderer.send('loadlocalUser')
  window.ipcRenderer.on('loadlocalUserCallback', (event, user) => {
    if (user) {
      localUserList.value = user
    }
  })
}
onMounted(() => {
  init()
})
onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('loadlocalUserCallback')
})
//校验密码是否正确
const checkValue = (type, value, msg) => {
  if (proxy.Utils.Isempty(value)) {
    errorMsg.value = msg
    return false
  }
  if (type && !proxy.Verify[type](value)) {
    errorMsg.value = msg
    return false
  }
  return true
}
//将输入框清空
const clearInput = () => {
  errorMsg.value = null
}

const seleceEmail = (email) => {
  formData.value.email = email
}
</script>
<style lang="scss" scoped>
.email-select {
  cursor: pointer;
  width: 250px;
}
.loding-panel {
  height: calc(100vh - 30px); //calc() 函数用于动态计算长度值
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  img {
    width: 300px;
  }
}
.login-panel {
  background: #fff;
  border-radius: 3px;
  border: 1px solid #ddd;
  .title {
    height: 30px;
    padding: 5px 0 0 10px;
  }
}
.login-form {
  padding: 0 15px 29px 15px;
  // :deep() 是 Vue 中 scoped 样式的一种特殊选择器，用于深度选择子组件的样式,使用 :deep() 可以穿透当前组件的样式范围，对子组件或嵌套组件的样式进行覆盖。
  :deep(.el-input__wrapper) {
    box-shadow: none;
    border-radius: none;
  }
  .login-form-item {
    border-radius: 1px solid #ddd;
  }
  .email-panel {
    align-items: center;
    width: 100%;
    display: flex;
  }
}
.login-btn {
  margin-top: 20px;
  width: 100%;
  height: 36px;
  background: #07c160;
  font-size: 16px;
}
.bottom-link {
  text-align: right;
}
.error-msg {
  line-height: 30px;
  height: 30px;
  color: #fb7373;
}
.check-code-panel {
  display: flex;
  .checkcode {
    margin-left: 5px;
    width: 120px;
    cursor: pointer;
  }
}
</style>
