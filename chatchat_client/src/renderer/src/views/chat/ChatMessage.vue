<template>
  <div v-if="data.sendUserId == userInfoStore.getUserInfo().userId" class="message-content-my">
    <div :class="['content-panel', data.messageType == 5 ? 'content-panel-media' : '']">
      <div v-if="data.status == 0" class="sending">
        <el-skeleton :animated="true">
          <template #template>
            <el-skeleton-item variant="image" class="skeleton-item"></el-skeleton-item>
          </template>
        </el-skeleton>
      </div>
      <template v-else>
        <div v-if="data.messageType != 5" class="content" v-html="data.messageContent"></div>
        <div v-if="data.fileType == 0" class="content">
          <chatMessageImage :data="data" @click="showMediaDetail"></chatMessageImage>
        </div>
        <div v-if="data.fileType == 1" class="content">
          <chatMessageVedio :data="data" @click="showMediaDetail"></chatMessageVedio>
        </div>
        <div v-if="data.fileType == 2" class="content">
          <chatMessageFile :data="data" @click="showMediaDetail"></chatMessageFile>
        </div>
      </template>
    </div>
    <Avatar :user-id="userInfoStore.getUserInfo().userId"></Avatar>
  </div>
  <div v-else class="message-content-other">
    <div class="user-avatar"><Avatar :user-id="data.sendUserId"></Avatar></div>
    <div
      :class="[
        'content-panel',
        data.contactType == 1 ? 'group-content' : '',
        data.messageType == 5 ? 'content-panel-media' : ''
      ]"
    >
      <div v-if="data.contactType == 1" class="nick-name">{{ data.sendUserNickName }}</div>
      <div v-if="data.status == 0">
        <el-skeleton :animated="true" class="sending">
          <template #template>
            <el-skeleton-item variant="image" class="skeleton-item"></el-skeleton-item>
          </template>
        </el-skeleton>
      </div>
      <template v-else>
        <div v-if="data.messageType != 5" class="content" v-text="data.messageContent"></div>
        <div v-if="data.fileType == 0" class="content">
          <chatMessageImage :data="data" @click="showMediaDetail"></chatMessageImage>
        </div>
        <div v-if="data.fileType == 1" class="content">
          <chatMessageVedio :data="data" @click="showMediaDetail"></chatMessageVedio>
        </div>
        <div v-if="data.fileType == 2" class="content">
          <chatMessageFile :data="data" @click="showMediaDetail"></chatMessageFile>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import Avatar from '../../components/Avatar.vue'
import { useUserInfoStore } from '../../store/userInfoStore'
import chatMessageImage from './chatMessageImage.vue'
import chatMessageVedio from './chatMessageVedio.vue'
import chatMessageFile from './chatMessageFile.vue'

const userInfoStore = useUserInfoStore()
const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  },
  currentChatSession: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['showMediaDetail'])
const showMediaDetail = () => {
  if (props.data.status == 0) {
    return
  }
  emit('showMediaDetail', props.data.messageId)
}
</script>

<style lang="scss" scoped>
.message-content-my {
  display: flex;
  .content-panel {
    margin-right: 10px;
    text-align: right;
    padding-left: 32%;
    .content {
      background: #95ec96;
    }
    &::after {
      right: -4px;
    }
  }
}
.message-content-other {
  display: flex;
  padding-right: 32%;
  .user-avatar {
    margin-right: 10px;
    width: 35px;
    height: 35px;
  }
  .content-panel {
    flex: 1;
    position: relative;
    text-align: left;
    .nick-name {
      font-size: 12px;
      color: #b2b2b2;
    }
    .content {
      background: #fff;
    }
    .sending {
      float: left;
    }
    &::after {
      left: -4px;
      background: #fff;
    }
  }
  .content-panel-media {
    justify-content: flex-start;
  }
}
.sending {
  width: 170px;
  height: 170px;
  overflow: hidden;
  float: right;
  margin-right: 5px;
  border-radius: 5px;
  .skeleton-item {
    width: 170px;
    height: 170px;
  }
}
.content {
  display: inline-block;
  padding: 8px;
  color: #474747;
  border-radius: 5px;
  font-size: 14px;
  text-align: left;
  :deep(.emoji) {
    font-size: 20px;
  }
}
.content-panel {
  flex: 1;
  position: relative;
  &::after {
    content: '';
    position: absolute;
    display: block;
    width: 10px;
    height: 10px;
    top: 13px;
    background: #95ec96;
    border-radius: 2px;
    transform: rotate(45deg);
  }
}
.content-panel-media {
  .content {
    border-radius: 5px;
    background: none !important;
    overflow: hidden;
    padding: 0;
  }
  &::after {
    display: none;
  }
}
.group-content {
  margin-top: -6px;
  .content {
    margin-top: 6px;
  }
  &::after {
    left: -4px;
    top: 35px;
    background: #fff;
  }
}
</style>
