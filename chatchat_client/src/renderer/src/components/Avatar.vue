<template>
  <div>
    <AvatarBase
      v-if="userId == 'Urobot'"
      :user-id="userId"
      :width="width"
      :show-detail="showDetail"
      :border-radius="borderRadius"
    ></AvatarBase>
    <el-popover
      v-else
      ref="popoverRef"
      :width="280"
      placement="right-start"
      :show-arrow="false"
      trigger="click"
      transition="none"
      :hide-after="0"
      @show="getContactInfo"
    >
      <template #reference>
        <AvatarBase
          :user-id="userId"
          :width="width"
          :show-detail="showDetail"
          :border-radius="borderRadius"
        ></AvatarBase>
      </template>
      <template #default>
        <div class="popover-user-panel">
          <UserBaseInfo :user-info="userInfo" :show-area="true"></UserBaseInfo>
          <div v-if="showDetail" class="op-btn">
            <el-button v-if="userInfo.contactStatus == 1" type="primary" @click="sendMessage"
              >发送消息</el-button
            >
            <el-button v-else type="primary" @click="addContact">添加好友</el-button>
          </div>
        </div>
      </template>
    </el-popover>
    <SearchAdd ref="searchRef"></SearchAdd>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import AvatarBase from './AvatarBase.vue'
import UserBaseInfo from './UserBaseInfo.vue'
import { useUserInfoStore } from '@/store/userInfoStore.js'
import router from '../router'
import SearchAdd from '../views/contact/SearchAdd.vue'
const userInfoStore = useUserInfoStore()
const { proxy } = getCurrentInstance()
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
  groupId: {
    type: Boolean,
    default: false
  },
  showDetail: {
    type: Boolean,
    default: true
  }
})
//点击人物头像获取相关用户信息
const userInfo = ref({})
const getContactInfo = async () => {
  if (userInfoStore.getUserInfo().userId === props.userId) {
    userInfo.value = userInfoStore.getUserInfo()
  } else {
    let result = await proxy.Request({
      url: proxy.api.getContactInfo,
      params: {
        contactId: props.userId
      },
      showLoading: false
    })
    if (!result) {
      return
    }
    userInfo.value = Object.assign({}, result)
  }
}
//发送消息
const emit = defineEmits(['closeDrawer'])
const popoverRef = ref()

//点击头像后进入发送消息
const sendMessage = () => {
  popoverRef.value.hide()
  emit('closeDrawer')
  router.push({
    path: '/chat',
    query: {
      chatId: props.userId,
      timestamp: new Date().getTime()
    }
  })
}
//点击头像后添加好友
const searchRef = ref()
const addContact = () => {
  popoverRef.value.hide()
  searchRef.value.show({
    contactId: props.userId,
    contactType: 'USER'
  })
}
</script>

<style lang="scss" scoped>
.op-btn {
  text-align: center;
  border-top: 1px solid #eaeaea;
  padding-top: 10px;
}
</style>
