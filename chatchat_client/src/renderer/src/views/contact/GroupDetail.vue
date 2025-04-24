<template>
  <ContentPanel>
    <div class="group-info-item">
      <div class="group-title">群名称:</div>
      <div class="group-value">
        <Avatar :user-id="groupInfo.groupId"></Avatar>
      </div>
      <el-dropdown trigger="click" placement="bottom-end">
        <span class="el-dropdown-link">
          <div class="iconfont icon-more"></div>
        </span>
        <template #dropdown>
          <el-dropdown-menu v-if="groupInfo.groupOwnerId == userInfoStore.getUserInfo().userId">
            <el-dropdown-item @click="editGroupInfo">修改群消息</el-dropdown-item>
            <el-dropdown-item @click="dissolutionGroup">解散该群</el-dropdown-item>
          </el-dropdown-menu>
          <el-dropdown-menu v-else>
            <el-dropdown-item @click="leaveGroup">退出该群</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="group-info-item">
      <div class="group-title">群ID:</div>
      <div class="group-value">{{ groupInfo.groupId }}</div>
    </div>
    <div class="group-info-item">
      <div class="group-title">群名称:</div>
      <div class="group-value">{{ groupInfo.groupName }}</div>
    </div>
    <div class="group-info-item">
      <div class="group-title">群成员:</div>
      <div class="group-value">{{ groupInfo.memberCount }}</div>
    </div>
    <div class="group-info-item">
      <div class="group-title">加入权限:</div>
      <div class="group-value">{{ groupInfo.joinType == 0 ? '直接加入' : '管理员同意后加入' }}</div>
    </div>
    <div class="group-info-item notice">
      <div class="group-title">公告:</div>
      <div class="group-value">{{ groupInfo.groupNotice || '-' }}</div>
    </div>
    <div class="group-info-item">
      <div class="group-title"></div>
      <div class="group-value">
        <el-button type="primary" @click="sendMessage">发送消息</el-button>
      </div>
    </div>
  </ContentPanel>
  <GroupEditDailog ref="groupEditDailogRef" @reload-group-info="getGroupInfo"></GroupEditDailog>
</template>

<script setup>
import Avatar from '../../components/Avatar.vue'
import ContentPanel from '../../components/ContentPanel.vue'
import { ref, getCurrentInstance, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useContactStateStore } from '../../store/contactStateStore'
import { useUserInfoStore } from '../../store/userInfoStore'
import GroupEditDailog from './GroupEditDailog.vue'
import router from '../../router'
const contactStateStore = useContactStateStore()

const userInfoStore = useUserInfoStore()
const route = useRoute()
const { proxy } = getCurrentInstance()

const groupInfo = ref({})
const groupId = ref()
const groupEditDailogRef = ref()
//修改群权限框
const editGroupInfo = () => {
  groupEditDailogRef.value.show(groupInfo.value)
}
//点击解散群聊功能
const dissolutionGroup = () => {
  proxy.confirm({
    message: '是否解散该群聊?',
    okfun: async () => {
      contactStateStore.setContactReload(null)
      let result = await proxy.Request({
        url: proxy.api.dissolutionGroup,
        params: {
          groupId: groupInfo.value.groupId
        }
      })
      if (!result) {
        return
      }
      proxy.message.success('解散成功')
      contactStateStore.setContactReload('DISSOLUTION_GROUP')
    }
  })
}
//点击退出群聊功能
const leaveGroup = () => {
  proxy.confirm({
    message: '是否退出该群聊?',
    okfun: async () => {
      contactStateStore.setContactReload(null)
      let result = await proxy.Request({
        url: proxy.api.leaveGroup,
        params: {
          groupId: groupInfo.value.groupId
        }
      })
      if (!result) {
        return
      }
      proxy.message.success('退出成功')
      contactStateStore.setContactReload('LEAVE_GROUP')
    }
  })
}

//获取某个群聊具体消息
const getGroupInfo = async () => {
  let result = await proxy.Request({
    url: proxy.api.getGroupInfo,
    params: {
      groupId: groupId.value
    }
  })
  if (!result) {
    return
  }
  groupInfo.value = result
}
watch(
  () => route.query.contactId,
  (newVal) => {
    groupId.value = newVal
    getGroupInfo()
  },
  { immediate: true, deep: true }
)
//点击发送消息按钮跳转到聊天页面
const sendMessage = () => {
  router.push({
    path: '/chat',
    query: { chatId: groupInfo.value.groupId, timeStamp: new Date().getTime() }
  })
}
</script>

<style lang="scss" scoped>
.group-info-item {
  display: flex;
  margin: 15px 0;
  align-items: center;
  .group-title {
    width: 100px;
    text-align: right;
  }
  .group-value {
    flex: 1;
  }
}
.notice {
  align-items: flex-start;
}
</style>
