<template>
  <div class="group-panel">
    <el-drawer ref="drawerRef" v-model="showDrawer" modal-class="mask-style" :size="300">
      <div class="group-panel-body">
        <div class="member-list">
          <div v-for="item in memberList" :key="item.contactId" class="member-item">
            <Avatar :user-id="item.userId" :width="30" @close-drawer="closeDrawerHandle"></Avatar>
            <div class="nick-name">{{ item.contactName }}</div>
            <div v-if="item.userId == groupInfo.groupOwnerId" class="owner-tag">群主</div>
          </div>
          <template v-if="groupInfo.groupOwnerId == userInfoStore.getUserInfo().userId">
            <div class="member-item" @click="addUser">
              <div class="iconfont icon-add icon-op"></div>
              <div class="nick-name">添加</div>
            </div>
            <div class="member-item" @click="removeUser">
              <div class="iconfont icon-min icon-op"></div>
              <div class="nick-name">删除</div>
            </div>
          </template>
        </div>
        <div class="line"></div>
        <div class="part-content">
          <AvatarBase
            :user-id="groupInfo.groupId"
            :width="60"
            :border-radius="5"
            :show-detail="true"
          ></AvatarBase>
        </div>
        <div class="part-title">群号</div>
        <div class="part-content">{{ groupInfo.groupId }}</div>
        <div class="part-title">群聊名称</div>
        <div class="part-content">{{ groupInfo.groupName }}</div>
        <div class="part-title">群公告</div>
        <div class="part-content">{{ groupInfo.Notice || '' }}</div>
        <div class="line"></div>
        <a
          v-if="groupInfo.groupOwnerId == userInfoStore.getUserInfo().userId"
          href="javascript:void(0)"
          class="leave-btn"
          @click="dissolutionGtoup"
          >解散群聊</a
        >
        <a v-else href="javascript:void(0)" class="leave-btn" @click="leaveGroup">退出群聊</a>
      </div>
    </el-drawer>
    <UserSelect ref="userSelcetRef" @callback="addOrRemoveUserCallback"></UserSelect>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import Avatar from '../../components/Avatar.vue'
import { useUserInfoStore } from '../../store/userInfoStore'
import AvatarBase from '../../components/AvatarBase.vue'
import UserSelect from '../chat/UserSelect.vue'
import message from '../../Utils/Message'
const userInfoStore = useUserInfoStore()
const { proxy } = getCurrentInstance()
const showDrawer = ref(false)
const memberList = ref([])
const groupInfo = ref({})
const show = async (groupId) => {
  let result = await proxy.Request({
    url: proxy.api.getGroupInfo4Chat,
    params: {
      groupId
    },
    showError: false,
    errorCallback: (respnonce) => {
      proxy.confirm({ message: respnonce.info, showCancelButton: false })
    }
  })
  if (!result) {
    console.log('请求失败')
    return
  }
  showDrawer.value = true
  groupInfo.value = result.groupInfo
  memberList.value = result.userContactList
}

const drawerRef = ref(null)
const closeDrawerHandle = () => {
  drawerRef.value.close()
}
//添加/移除成员函数
const userSelcetRef = ref()
const addUser = async () => {
  let result = await proxy.Request({
    url: proxy.api.loadContact,
    params: {
      contactType: 'USER'
    }
  })
  if (!result) {
    return
  }
  const contactIds = memberList.value.map((item) => item['userId'])
  let contactList = result
  contactList.forEach((e) => {
    if (contactIds.includes(e.contactId)) {
      e.disabled = true
    }
  })
  userSelcetRef.value.show({ contactList, groupId: groupInfo.value.groupId, opType: 1 })
}

const removeUser = () => {
  console.log('memberList', memberList)
  let contactList = memberList.value
  contactList.forEach((item) => {
    item.contactId = item.userId
  })
  contactList.splice(0, 1)
  userSelcetRef.value.show({
    contactList,
    groupId: groupInfo.value.groupId,
    opType: 0
  })
}

const addOrRemoveUserCallback = () => {
  showDrawer.value = false
}
//退出群聊
const emit = defineEmits(['delChatSessionCallback'])
const leaveGroup = () => {
  proxy.confirm({
    message: '确定要退出群聊吗？',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.leaveGroup,
        params: {
          groupId: groupInfo.value.groupId
        }
      })
      if (!result) {
        console.log('退出群聊失败')
        return
      }
      showDrawer.value = false
      message.success('退出群聊成功')
      emit('delChatSessionCallback', groupInfo.value.groupId)
    }
  })
}

//解散群聊
const dissolutionGtoup = () => {
  proxy.confirm({
    message: '确定要解散群聊吗？',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.dissolutionGroup,
        params: {
          groupId: groupInfo.value.groupId
        }
      })
      if (!result) {
        console.log('解散群聊失败')
        return
      }
      showDrawer.value = false
      message.success('退出群聊成功')
    }
  })
}
defineExpose({
  show
})
</script>

<style lang="scss" scoped>
.group-panel {
  color: #000000;
  :deep(.mask-style) {
    top: 1px;
    right: 1px;
    height: calc(100vh - 2px);
  }
  :deep(.el-drawer) {
    -webkit-app-region: no-drag;
  }
  :deep(.el-drawer_header) {
    margin-bottom: 10px;
  }
  :deep(.el-drawer_body) {
    padding: 10px;
  }
  .group-panel-body {
    .member-list {
      display: flex;
      flex-wrap: wrap;
      .member-item {
        width: 20%;
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 5px;
        padding: 5px;
        position: relative;
        .owner-tag {
          position: absolute;
          left: 0;
          top: 0;
          font-size: 12px;
          background: #07c160;
          color: #fff;
          border-radius: 3px;
        }
        .nick-name {
          margin-top: 3px;
          width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          font-size: 13px;
          text-align: center;
        }
      }
      .icon-op {
        width: 30px;
        height: 30px;
        display: flex;
        cursor: pointer;
        justify-content: center;
        align-items: center;
        border: 1px solid #dbdbdb;
        color: #6e6e6e;
      }
    }
    .line {
      margin-top: 10px;
      height: 1px;
      border: 1px solid #ddd;
    }
    .part-content {
      color: #757575;
      margin-bottom: 10px;
    }
    .part-title {
      margin-top: 10px;
    }
    .leave-btn {
      color: #f45454;
      text-decoration: none;
      text-align: center;
      display: block;
      margin-top: 10px;
    }
  }
}
</style>
