<template>
  <div class="message-time">
    {{ proxy.Utils.formatDate(data.sendTime) }}
  </div>
</template>

<script setup>
import { getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()
const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})
</script>

<style lang="scss" scoped>
.message-time {
  background: #dadada;
  display: inline-block;
  color: #fff;
  font-size: 12px;
  padding: 2px 5px;
  border-radius: 3px;
  margin: 10px 0;
}
</style>
