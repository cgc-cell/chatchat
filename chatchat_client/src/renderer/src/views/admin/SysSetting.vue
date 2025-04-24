<template>
  <div class="form-panel">
    <el-form ref="formDataRef" :model="formData" :rules="rules" label-width="160px" @submit.prevent>
      <el-form-item label="最多可创建的群组数" prop="maxGroupCount">
        <el-input
          v-model.trim="formData.maxGroupCount"
          clearable
          placeholder="请输入最多可创建的群组数量 "
        ></el-input>
      </el-form-item>
      <el-form-item label="群组最大成员数" prop="maxGroupMemberCount">
        <el-input
          v-model.trim="formData.maxGroupMemberCount"
          clearable
          placeholder="请输入每个群组的最大成员数 "
        ></el-input>
      </el-form-item>
      <el-form-item label="图片大小" prop="maxImageSize">
        <el-input v-model.trim="formData.maxImageSize" clearable placeholder="请输入每个图片大小 ">
          <template #append>MB</template>
        </el-input>
      </el-form-item>
      <el-form-item label="视频大小" prop="maxVideoSize">
        <el-input
          v-model.trim="formData.maxVideoSize"
          clearable
          placeholder="请输入允许上传视频大小 "
        >
          <template #append>MB</template></el-input
        >
      </el-form-item>
      <el-form-item label="其他文件大小" prop="maxFileSize">
        <el-input
          v-model.trim="formData.maxFileSize"
          clearable
          placeholder="请输入允许上传其他文件大小 "
        >
          <template #append>MB</template></el-input
        >
      </el-form-item>
      <el-form-item label="机器人昵称" prop="robotNickName">
        <el-input
          v-model.trim="formData.robotNickName"
          clearable
          placeholder="请输入允许上传其他文件大小 "
          maxlength="20"
        >
        </el-input>
      </el-form-item>
      <el-form-item label="机器人头像" prop="robotFile">
        <AvatarUpload v-model="formData.robotFile" @cover-file="saveCover"></AvatarUpload>
      </el-form-item>
      <el-form-item label="欢迎消息" prop="robotWelcome">
        <el-input
          v-model.trim="formData.robotNickName"
          clearable
          placeholder="请输入新用户注册机器人欢迎消息 "
          maxlength="300"
          resize="none"
          rows="5"
          type="textarea"
          :show-word-limit="true"
        >
        </el-input>
      </el-form-item>
      <el-form-item label="">
        <el-button type="primary" @click="saveSysSetting">保存设置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import AvatarUpload from '../../components/AvatarUpload.vue'
import { getCurrentInstance, ref } from 'vue'
const { proxy } = getCurrentInstance()

const formData = ref({})
const formDataRef = ref(null)

const rules = {
  maxGroupCount: [
    { required: true, message: '请输入最多可创建的群组数量' },
    { validator: proxy.Verify.number, message: '只能是数字' }
  ],
  maxGroupMemberCount: [
    { required: true, message: '请输入每个群组的最大成员数' },
    { validator: proxy.Verify.number, message: '只能是数字' }
  ],
  maxImageSize: [
    { required: true, message: '请输入每个图片大小' },
    { validator: proxy.Verify.number, message: '只能是数字' }
  ],
  maxFileSize: [
    { required: true, message: '请输入允许上传其他文件大小' },
    { validator: proxy.Verify.number, message: '只能是数字' }
  ],
  maxVideoSize: [
    { required: true, message: '请输入允许上传视频大小' },
    { validator: proxy.Verify.number, message: '只能是数字' }
  ],
  robotNickName: [{ required: true, message: '请传入机器人昵称' }],
  robotFile: [{ required: true, message: '请传入机器人头像' }],
  robotWelcome: [{ required: true, message: '请传入欢迎消息' }]
}

const saveCover = ({ avatarFile, coverFile }) => {
  formData.value.robotFile = avatarFile
  formData.value.robotCover = coverFile
}

const getSysSetting = async () => {
  let result = await proxy.Request({
    url: proxy.api.getSysSetting4Admin
  })
  if (!result) {
    return
  }
  formData.value = result
  formData.value.robotFile = result.robotUid
}
getSysSetting()
</script>

<style lang="scss" scoped>
.form-panel {
  width: 500px;
}
</style>
