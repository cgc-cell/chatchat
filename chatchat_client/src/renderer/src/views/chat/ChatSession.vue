<template>
  <div :class="['chat-session-item', currentSession ? 'active' : '']">
    <div v-if="data.contactType == 1" class="contact-tag">群</div>
    <Badge :count="data.noReadCount" :top="2" :left="42"></Badge>
    <AvatarBase :user-id="data.contactId"></AvatarBase>
    <div class="user-info">
      <div class="user-name-panel">
        <div class="user-name">{{ data.contactName }}</div>
        <div class="message-time">{{ proxy.Utils.formatDate(data.lastReceiveTime) }}</div>
      </div>
      <div class="last-message" v-text="data.lastMessage"></div>
    </div>
    <div v-if="data.topType == 1" class="chat-top iconfont icon-top"></div>
  </div>
</template>

<script setup>
import AvatarBase from '../../components/AvatarBase.vue'
import Badge from '../../components/Badge.vue'
import { getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()
// eslint-disable-next-line no-unused-vars
const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  },
  currentSession: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
.chat-session-item {
  display: flex;
  padding: 10px;
  border-bottom: 1px solid #ddd;
  position: relative;
  .contact-tag {
    position: absolute;
    left: 0;
    top: 0;
    z-index: 1;
    background: #24acf2;
    font-size: 12px;
    color: #fff;
    padding: 1px 2px 1px 1px;
    border-radius: 0px 3px 3px 0;
    line-height: 12px;
  }
  &:hover {
    cursor: pointer;
    background: #d8d8d7;
    .message-time {
      color: #9a9898 !important;
    }
  }
  .user-info {
    flex: 1;
    margin-left: 10px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    .user-name-panel {
      display: flex;
      .user-name {
        width: 140px;
        color: #000000;
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .message-time {
        width: 55px;
        font-size: 12px;
        color: #b6b6b6;
        text-align: right;
      }
    }
    .last-message {
      width: 180px;
      height: 15px;
      overflow: hidden;
      font-size: 12px;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-top: 5px;
      color: #999999;
    }
  }
  .chat-top {
    position: absolute;
    right: 0;
    top: 0;
    font-size: 15px;
    color: #8f8f8f;
  }
}
.active {
  background: #d8d8d7;
}
</style>
