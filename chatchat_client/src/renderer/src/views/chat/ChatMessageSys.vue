<template>
  <ChatMessageTime :data="data"></ChatMessageTime>
  <div class="sys-message">{{ dataMessage.messageContent }}</div>
</template>

<script setup>
import { computed } from 'vue'
import ChatMessageTime from './ChatMessageTime.vue'
const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const dataMessage = computed(() => {
  const data = { ...props.data }
  // console.log('data', data)

  if (data.messageType == 1) {
    data.messageContent = '成功添加好友，以下是打招呼的消息'
  }

  return data
})
</script>

<style lang="scss" scoped>
.sys-message {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-bottom: 20px;
}
</style>
