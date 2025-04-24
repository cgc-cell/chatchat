<template>
  <div>
    <Dailog
      :show="dialogConfig.show"
      :title="dialogConfig.title"
      :buttons="dialogConfig.buttons"
      width="400px"
      :show-cancel="false"
      @close="dialogConfig.show = false"
    >
      <el-form ref="formDataRef" :model="formData" :rules="rules" @submit.prevent>
        <!--input输入-->
        <el-form-item label="" prop="">
          <el-input
            v-model.trim="formData.applyInfo"
            type="textarea"
            :rows="5"
            clearable
            resize="none"
            show-word-limit
            maxlength="100"
            placeholder="请输入内容"
          ></el-input>
        </el-form-item>
      </el-form>
    </Dailog>
  </div>
</template>

<script setup>
import { nextTick, ref, getCurrentInstance } from 'vue'
import { useUserInfoStore } from '@/store/userInfoStore.js'
import { useContactStateStore } from '../../store/contactStateStore'
const contactStateStore = useContactStateStore()
const { proxy } = getCurrentInstance()
const userInfoStore = useUserInfoStore()
const formData = ref({})
const formDataRef = ref()
const dialogConfig = ref({
  show: false,
  title: '申请提示',
  buttons: [
    {
      type: 'primary',
      text: '确定',
      click: () => {
        submitApply()
      }
    }
  ]
})
const rules = {
  title: [{ required: true, message: '提示信息' }]
}
//点击确定后申请添加好友或群聊
const emit = defineEmits(['reload'])
const submitApply = async () => {
  const { contactId, contactType, applyInfo } = formData.value
  let result = await proxy.Request({
    url: proxy.api.applyAdd,
    params: {
      contactId,
      contactType,
      applyInfo
    }
  })
  if (result == 0) {
    proxy.message.success('添加成功')
  } else {
    proxy.message.success('申请成功，请等待审核')
  }

  dialogConfig.value.show = false
  emit('reload')
  if (result == 0) {
    contactStateStore.setContactReload(contactType)
  }
}
// 显示申请添加好友弹窗
const show = (data) => {
  //打开面板
  dialogConfig.value.show = true
  nextTick(() => {
    formDataRef.value.resetFields() //重置表单
    formData.value = Object.assign({}, data)
    formData.value.applyInfo =
      '我是' + userInfoStore.getUserInfo().nickName + '，请求添加您为联系人'
  })
}
defineExpose({ show })
</script>

<style lang="scss" scoped></style>
