<template>
  <div class="file-panel">
    <div class="file-info" :title="data.fileName">
      <div class="file-name">{{ data.fileName }}</div>
      <div class="file-size">大小:{{ proxy.Utils.size2Str(data.fileSize) }}</div>
      <div class="process">
        <span v-if="data.status == 1" class="iconfont icon-ok"></span>
        <div class="info">{{ data.status == 0 ? '文件处理中' : '处理完成' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()
const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})
</script>

<style lang="scss" scoped>
.file-panel {
  display: flex;
  padding: 10px 15px;
  background: #fff;
  align-items: center;
  .file-info {
    width: 150px;
    display: flex;
    flex-direction: column;
    max-width: 100%;
    .file-name {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .file-size {
      margin-top: 2px;
      font-size: 12px;
      color: #6e6e6e;
    }
    .process {
      margin-top: 2px;
      display: flex;
      align-items: center;
      .loading {
        width: 16px;
      }
      .info {
        margin-left: 5px;
        font-size: 12px;
        color: #545454;
      }
      .icon-ok {
        color: #25c25b;
        font-size: 16px;
        width: 16px;
      }
    }
  }
  .iconfont {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    color: #6e6e6e;
  }
}
</style>
