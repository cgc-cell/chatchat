<template>
  <div class="win-op no-drag">
    <div
      v-if="showSetTop"
      :class="['iconfont icon-top', isTop ? 'win-top' : '']"
      :title="isTop ? '取消置顶' : '置顶'"
      @click="top"
    ></div>
    <div v-if="showMin" class="iconfont icon-min" title="最小化" @click="minimize"></div>
    <div
      v-if="showMax"
      :class="['iconfont', isMax ? 'icon-maximize' : 'icon-max']"
      :title="isMax ? '向下还原' : '最大化'"
      @click="maximize"
    ></div>
    <div v-if="showClose" class="iconfont icon-close" title="关闭" @click="close"></div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
const props = defineProps({
  showSetTop: {
    type: Boolean,
    default: true
  },
  showMin: {
    type: Boolean,
    default: true
  },
  showMax: {
    type: Boolean,
    default: true
  },
  showClose: {
    type: Boolean,
    default: true
  },
  //0关闭,1隐藏
  closeType: {
    type: Number,
    default: 1
  }
})
const isMax = ref(false)
const isTop = ref(false)
onMounted(() => {
  isMax.value = false
  isTop.value = false
})
const winOp = (action, data) => {
  window.ipcRenderer.send('winTitleOp', { action, data })
}
const emits = defineEmits(['closeCallback'])
//关闭
const close = () => {
  winOp('close', { closeType: props.closeType })
  emits('closeCallback')
}
//最小化
const minimize = () => {
  winOp('minimize')
}
//最大化
const maximize = () => {
  if (isMax.value) {
    winOp('unmaximize')
    isMax.value = false
  } else {
    winOp('maximize')
    isMax.value = true
  }
}
const top = () => {
  isTop.value = !isTop.value
  winOp('top', { top: isTop.value })
}
</script>

<style lang="scss" scoped>
.win-op {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 1;
  overflow: hidden;
  border-radius: 0 3px 0 0;
  .iconfont {
    float: left;
    font-size: 12px;
    color: #101010;
    text-align: center;
    display: flex;
    justify-content: center;
    cursor: pointer;
    height: 25px;
    align-items: center;
    padding: 0 10px;
    &:hover {
      background: #ddd;
    }
  }
  .icon-close {
    &:hover {
      background: #fb7373;
      color: #fff;
    }
  }
  .win-top {
    color: #07c160;
    background: #fff;
  }
}
</style>
