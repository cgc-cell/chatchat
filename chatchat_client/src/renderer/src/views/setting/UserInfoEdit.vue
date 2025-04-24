<template>
  <div>
    <el-form ref="formDataRef" :model="formData" :rules="rules" label-width="80px" @submit.prevent>
      <el-form-item label="头像" prop="avatarFile">
        <AvatarUpload v-model="formData.avatarFile" @cover-file="saveCover"></AvatarUpload>
      </el-form-item>
      <el-form-item label="昵称" prop="nickName">
        <el-input
          v-model.trim="formData.nickName"
          placeholder="请输入昵称"
          clearable
          maxlength="150"
        ></el-input>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="formData.sex">
          <el-radio :label="1">男</el-radio> <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item lable="朋友权限" prop="joinType">
        <el-switch v-model="formData.joinType" :active-value="1" :inactive-value="0"></el-switch>
        <div class="info">
          {{ formData.joinType == 1 ? '加我好友需要验证' : '加我好友不需要验证' }}
        </div>
      </el-form-item>
      <el-form-item label="地区" prop="area">
        <AreaSelect v-model="formData.area"></AreaSelect>
      </el-form-item>
      <el-form-item label="个性签名" prop="personalSignature">
        <!--input输入-->
        <el-input
          v-model.trim="formData.personalSignature"
          clearable
          placeholder="请输入个性签名"
          type="textarea"
          rows="5"
          maxlength="150"
          resize="none"
          :show-word-limit="true"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveUserInfo">保存</el-button>
        <el-button link @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, ref, getCurrentInstance, reactive } from 'vue'
import AvatarUpload from '../../components/AvatarUpload.vue'
import AreaSelect from './AreaSelect.vue'
import { useUserInfoStore } from '../../store/userInfoStore'
import { useAvatarUpdateStore } from '../../store/AvatarUpdateStore'
const avatarUpdateStore = useAvatarUpdateStore()
const userInfoStore = useUserInfoStore()
const { proxy } = getCurrentInstance()
const formDataRef = ref(null)
const rules = ref({
  avatarFile: [{ required: true, message: '请输入头像' }],
  nickName: [{ required: true, message: '请输入昵称' }]
})

const props = defineProps({
  data: {
    type: Object
  }
})
//拿到数据后进行处理后才符合表单的数据
const formData = computed(() => {
  const userInfo = reactive(Object.assign({}, props.data))
  userInfo.avatarFile = userInfo.userId
  userInfo.area = {
    areaCode: userInfo.areaCode ? userInfo.areaCode.split(',') : [],
    areaName: userInfo.areaName ? userInfo.areaName.split(',') : []
  }
  return userInfo
})
//保存头像
const saveCover = ({ avatarFile, coverFile }) => {
  formData.value.avatarFile = avatarFile
  formData.value.avatarCover = coverFile
}
//进行表单验证后提交个人信息
const emit = defineEmits(['callback'])
const saveUserInfo = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = Object.assign({}, formData.value)
    params.areaName = ''
    params.areaCode = ''
    if (params.area) {
      params.areaName = params.area.areaName.join(',')
      params.areaCode = params.area.areaCode.join(',')
      delete params.area
    }
    avatarUpdateStore.setForceReload(userInfoStore.getUserInfo().userId, false)
    //todo 强制刷新头像
    let result = await proxy.Request({
      url: proxy.api.saveUserInfo,
      params
    })
    if (!result) {
      console.log('保存失败')
      return
    }
    proxy.message.success('保存成功')
    userInfoStore.setUserInfo(result)
    avatarUpdateStore.setForceReload(userInfoStore.getUserInfo().userId, true)
    emit('callback')
  })
}
//取消编辑
const cancel = () => {
  emit('callback')
}
</script>

<style lang="scss" scoped>
.info {
  margin-left: 5px;
  color: #949494;
  font-size: 12px;
}
</style>
