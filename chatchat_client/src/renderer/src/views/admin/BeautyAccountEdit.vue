<template>
  <Dailog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="400px"
    @close="dialogConfig.show = false"
  >
    <el-form ref="formDataRef" :model="formData" label-width="60px" :rules="rules">
      <!--input输入-->
      <el-form-item prop="email" label="邮箱">
        <el-input
          v-model="formData.email"
          clearable
          placeholder="请输入邮箱"
          :max-length="50"
        ></el-input>
      </el-form-item>
      <el-form-item prop="userId" label="靓号">
        <el-input
          v-model="formData.userId"
          clearable
          placeholder="请输入靓号"
          :max-length="11"
        ></el-input>
      </el-form-item>
    </el-form>
  </Dailog>
</template>

<script setup>
import { ref, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
const dialogConfig = ref({
  show: false,
  title: '编辑靓号',
  buttons: [
    {
      type: 'primary',
      text: '确定',
      click: () => {
        submitForm()
      }
    }
  ]
})

const formData = ref({
  updateDescList: []
})
const formDataRef = ref(null)
const rules = {
  email: [
    { required: true, message: '请输入版本号' },
    { validator: proxy.Verify.email, message: '请输入正确的邮箱' }
  ],
  userId: [
    { required: true, message: '请输入靓号' },
    { min: 11, max: 11, message: '请输入11位靓号' },
    { validator: proxy.Verify.number, message: '靓号只能是数字' }
  ]
}

const emit = defineEmits(['reload'])
const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value)
    let result = await proxy.Request({
      url: proxy.api.saveBeautAccount,
      params
    })
    if (!result) {
      return
    }
    dialogConfig.value.show = false
    emit('reload')
  })
}

const showEdit = (data = {}) => {
  dialogConfig.value.show = true
  nextTick(() => {
    formDataRef.value.resetFields()
    formData.value = Object.assign({}, data)
  })
}

defineExpose({
  showEdit
})
</script>

<style lang="scss" scoped></style>
