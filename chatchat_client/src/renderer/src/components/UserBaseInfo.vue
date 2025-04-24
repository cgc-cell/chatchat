<template>
  <div class="user-panel">
    <AvatarBase
      :width="60"
      :user-id="userInfo.userId || userInfo.contactId"
      :show-detail="true"
      :border-radius="5"
    >
    </AvatarBase>
    <div class="user-info">
      <div class="nick-name">
        {{ userInfo.nickName }}
        <span v-if="userInfo.sex == 0" class="iconfont icon-woman"></span>
        <span v-if="userInfo.sex == 1" class="iconfont icon-man"></span>
      </div>
      <div class="info">ID:{{ userInfo.userId || userInfo.contactId }}</div>
      <div v-if="showArea" class="info">地区:{{ proxy.Utils.getAreaInfo(userInfo.areaName) }}</div>
    </div>
  </div>
</template>

<script setup>
import AvatarBase from './AvatarBase.vue'
import { getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()
const props = defineProps({
  userInfo: {
    type: Object,
    default: () => ({})
  },
  showArea: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
.user-panel {
  display: flex;
  padding-bottom: 20px;
  .user-info {
    flex: 1;
    margin-left: 10px;
    .nick-name {
      font-size: 16px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #000000;
    }
  }
  .info {
    font-size: 12px;
    color: #9e9e9e;
    margin-top: 3px;
  }
}
</style>
