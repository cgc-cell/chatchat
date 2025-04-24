<template>
  <div class="avatar-upload">
    <div class="avatar-show">
      <template v-if="modelValue">
        <el-image v-if="preview" :src="localFile" fit="scale-down"></el-image>
        <ShowLocalImage
          v-else
          :file-id="props.modelValue"
          part-type="avatar"
          :width="40"
        ></ShowLocalImage>
      </template>
      <template v-else>
        <el-upload
          name="file"
          :show-file-list="false"
          accept=".png,.PNG,.jpg,.jpeg,.gif,.GIF,.bmp,.BMP"
          :multiple="false"
          :http-request="uploadImage"
        >
          <span class="iconfont icon-add"></span>
        </el-upload>
      </template>
    </div>
    <div class="select-btn">
      <el-upload
        name="file"
        :show-file-list="false"
        accept=".png,.PNG,.jpg,.jpeg,.gif,.GIF,.bmp,.BMP"
        :multiple="false"
        :http-request="uploadImage"
      >
        <el-button type="primary" size="small">选择</el-button>
      </el-upload>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import ShowLocalImage from './ShowLocalImage.vue'

const props = defineProps({
  modelValue: {
    type: [String, Object],
    default: null
  }
})
//头像预览功能
const preview = computed(() => {
  return props.modelValue instanceof File
})

const uploadImage = async (file) => {
  file = file.file
  window.ipcRenderer.send('createCover', file.path)
}
const emit = defineEmits(['coverFile'])
const localFile = ref(null)
onMounted(() => {
  window.ipcRenderer.on('createCoverCallback', (e, { avatarStream, coverStream }) => {
    const coverBlob = new Blob([coverStream], { type: 'image/png' })
    const coverFile = new File([coverBlob], 'cover.png')
    let img = new FileReader()
    img.readAsDataURL(coverFile)
    img.onload = ({ target }) => {
      localFile.value = target.result
    }
    const avatarBlob = new Blob([avatarStream], { type: 'image/png' })
    const avatarFile = new File([avatarBlob], 'avatar.png')
    emit('coverFile', { avatarFile, coverFile })
  })
})

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('createCoverCallback')
})
</script>

<style lang="scss" scoped>
.avatar-upload {
  display: flex;
  justify-content: center;
  align-items: end;
  line-height: normal; //指默认行高的1.5倍
  .avatar-show {
    background: #ededed;
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;
    .icon-add {
      font-size: 30px;
      color: #b9b9b9;
      width: 60px;
      height: 60px;
      line-height: 60px;
      text-align: center;
    }
    img {
      width: 100%;
      height: 100%;
    }
  }
  .select-btn {
    margin-left: 5px;
    vertical-align: bottom;
  }
}
</style>
