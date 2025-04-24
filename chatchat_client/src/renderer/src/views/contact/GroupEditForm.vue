<template>
  <div>
    <el-form ref="formDataRef" :model="formData" :rules="rules" label-width="80px" @submit.prevent>
      <!--input输入-->
      <el-form-item label="群名称" prop="groupName">
        <el-input
          v-model.trim="formData.groupName"
          maxlength="150"
          placeholder="请输入分组名称"
          clearable
        ></el-input>
      </el-form-item>
      <el-form-item label="封面" prop="avatarFile">
        <AvatarUpload
          ref="avatarUploadRef"
          v-model="formData.avatarFile"
          @cover-file="saveCover"
        ></AvatarUpload>
      </el-form-item>
      <el-form-item label="加入权限" prop="joinType">
        <el-radio-group v-model="formData.joinType">
          <el-radio :label="1">管理员同意后加入</el-radio>
          <el-radio :label="0">直接加入</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="公告" prop="groupNotice">
        <!--input输入-->
        <el-input
          v-model.trim="formData.groupNotice"
          clearable
          placeholder="请输入群公告"
          type="textarea"
          rows="5"
          maxlength="300"
          :show-word-limit="true"
          resize="none"
        ></el-input>
      </el-form-item>

      <el-form-item
        ><el-button type="primary" @click="submit">{{
          formData.groupId ? '修改群聊' : '创建群聊'
        }}</el-button></el-form-item
      >
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getCurrentInstance } from 'vue'
import { useContactStateStore } from '../../store/contactStateStore'
import AvatarUpload from '../../components/AvatarUpload.vue'
import { useAvatarUpdateStore } from '../../store/AvatarUpdateStore'
const avatarUpdateStore = useAvatarUpdateStore()
const contactStateStore = useContactStateStore()
const { proxy } = getCurrentInstance()
const formDataRef = ref()
const formData = ref({})
//创建/修改群聊时的表单验证规则
const rules = ref({
  groupName: [{ required: true, message: '请输入群名称', trigger: 'blur' }],
  joinType: [{ required: true, message: '请选择加入权限', trigger: 'blur' }],
  avatarFile: [{ required: true, message: '请上传封面', trigger: 'blur' }]
})
//提交创建/修改群聊时的处理
const emits = defineEmits(['editBack'])
const submit = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) return
    let params = {}
    Object.assign(params, formData.value)
    if (params.groupId) {
      avatarUpdateStore.setForceReload(params.groupId, false)
    }
    let result = await proxy.Request({
      url: proxy.api.saveGroup,
      params
    })
    if (!result) {
      return
    }

    if (params.groupId) {
      proxy.message.success('群组修改成功')
      emits('editBack')
    } else {
      proxy.message.success('群组创建成功')
    }
    formDataRef.value.resetFields()
    contactStateStore.setContactReload('My')
    if (params.groupId) {
      avatarUpdateStore.setForceReload(params.groupId, true)
    }
  })
}
//保存封面
const saveCover = ({ avatarFile, coverFile }) => {
  formData.value.avatarFile = avatarFile
  formData.value.avatarCover = coverFile
}
const show = (data) => {
  formDataRef.value.resetFields()
  formData.value = Object.assign({}, data)
  formData.value.avatarFile = data.groupId
}

defineExpose({
  show
})
</script>

<style lang="scss" scoped></style>
