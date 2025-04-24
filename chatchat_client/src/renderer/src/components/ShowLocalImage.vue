<template>
  <div class="image-panel" @click="showImagePanel">
    <el-image :src="serverUrl" fit="scale-down" :width="width">
      <template #error> <div class="iconfont icon-image-error"></div> </template>
    </el-image>
    <div v-if="showPlay" class="play-panel">
      <span class="iconfont icon-video-play"></span>
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useGlobalInfoStore } from '../store/GlobalInfoStore'
const globalInfoStore = useGlobalInfoStore()
//传进来参数表示显示那种类型的图片
const props = defineProps({
  width: {
    type: Number,
    default: 170 // 修改为函数形式
  },
  showPlay: {
    type: Boolean,
    default: false // 修改为函数形式
  },
  fileId: {
    type: [String, Number]
  },
  partType: {
    type: String,
    default: 'avatar'
  },
  fileType: {
    type: Number,
    default: 0
  },
  forceGet: {
    type: Boolean,
    default: false
  }
})
const serverUrl = computed(() => {
  if (!props.fileId) {
    return ''
  }
  let serverPort = globalInfoStore.getGlobalInfo('serverPort')
  watch(
    () => globalInfoStore.getGlobalInfo('serverPort'),
    (newVal) => {
      serverPort = newVal
    }
  )
  console.log(
    'serverUrl',
    `http://127.0.0.1:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&fileType=${props.fileType}&showCover=true&forceGet=${props.forceGet}&${new Date().getTime()}`
  )
  return `http://127.0.0.1:${serverPort}/file?fileId=${props.fileId}&partType=${props.partType}&fileType=${props.fileType}&showCover=true&forceGet=${props.forceGet}&${new Date().getTime()}`
})
</script>

<style lang="scss" scoped>
.image-panel {
  max-width: 170px;
  max-height: 170px;
  background: #ddd;
  position: relative;
  display: flex;
  overflow: hidden;
  cursor: pointer;
  .icon-image-error {
    margin: 0 auto;
    font-size: 30px;
    color: #838383;
  }
}

.play-panel {
  z-index: 2;
  position: absolute;
  top: 0;
  left: 0;
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  .icon-video-play {
    font-size: 35px;
    color: #fff;
  }
  &:hover {
    opacity: 0.8;
  }
}
</style>
