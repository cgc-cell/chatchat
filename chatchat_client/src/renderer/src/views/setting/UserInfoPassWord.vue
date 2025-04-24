<template>
  <div>
    <el-form ref="formDataRef" :model="formData" :rules="rules" label-width="80px" @submit.prevent>
      <el-form-item label="密码" prop="password">
        <!--input输入-->
        <el-input
          v-model.trim="formData.password"
          type="password"
          clearable
          placeholder="请输入新密码"
          show-password
        ></el-input>
      </el-form-item>
      <!--input输入-->
      <el-form-item label="确认密码" prop="rePassword">
        <el-input
          v-model.trim="formData.rePassword"
          type="password"
          show-password
          clearable
          placeholder="请再次输入新密码"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveUserInfo">修改密码</el-button>
        <el-button link @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()
const formDataRef = ref()
const formData = ref({})
const validateRePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== formData.value.password) {
    callback(new Error(rule.message))
  } else {
    callback()
  }
}
const rules = ref({
  password: [
    { required: true, message: '请输入新密码' },
    { validator: proxy.Verify.password, message: '密码只能是数字、字符、特殊字符' }
  ],
  rePassword: [
    { required: true, message: '请再次输入新密码' },
    {
      validator: validateRePassword,
      message: '两次输入密码不一致'
    }
  ]
})
const saveUserInfo = () => {
  formDataRef.value.validate((valid) => {
    if (!valid) {
      return
    }
    proxy.confirm({
      message: '确认下修改密码吗?修改后需重新登录',
      okfun: async () => {
        let params = Object.assign({}, formData.value)
        //todo 强制刷新头像
        let result = await proxy.Request({
          url: proxy.api.updatePassword,
          params
        })
        if (!result) {
          console.log('保存失败')
          return
        }
        proxy.message.success('修改密码成功请重新登录', () => {
          window.ipcRenderer.send('reLogin')
        })
      }
    })
  })
}
const emit = defineEmits(['callback'])
const cancel = () => {
  emit('callback')
}
</script>

<style lang="scss" scoped></style>
