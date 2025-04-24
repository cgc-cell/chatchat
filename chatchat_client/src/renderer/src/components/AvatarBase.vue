<template>
  <div
    class="user-avatar"
    :style="{ width: width + 'px', height: width + 'px', 'border-radius': borderRadius + 'px' }"
    @click="showDetailHander"
  >
    <ShowLocalImage
      :width="width"
      :file-id="userId"
      :force-get="avatarUpdateStore.getForceReload(userId)"
      :file-type="0"
    ></ShowLocalImage>
  </div>
</template>

<script setup>
import ShowLocalImage from './ShowLocalImage.vue'
import { useAvatarUpdateStore } from '../store/AvatarUpdateStore'
import { onMounted } from 'vue'
const avatarUpdateStore = useAvatarUpdateStore()
const props = defineProps({
  width: {
    type: Number,
    default: 40
  },
  userId: {
    type: String
  },
  borderRadius: {
    type: Number,
    default: 0
  },
  showDetail: {
    type: Boolean,
    default: false
  }
})
// 点击头像查看详情
const showDetailHander = () => {
  if (!props.showDetail) {
    return
  }
  window.ipcRenderer.send('newWindow', {
    windowId: 'media',
    title: '图片查看',
    path: '/showMedia',
    data: {
      fileList: [
        {
          fileId: props.userId,
          fileType: 0,
          partType: 'avatar',
          status: 1,
          forceGet: true
        }
      ]
    }
  })
}
onMounted(() => {
  console.log('userId', props.userId)
})
</script>

<style lang="scss" scoped>
.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #d3d3d3;
  overflow: hidden;
  cursor: pointer;
}
</style>
