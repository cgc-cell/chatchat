<template>
  <ContentPanel>
    <div class="user-info">
      <UserBaseInfo :user-info="userInfo"></UserBaseInfo>
      <div class="more-op">
        <el-dropdown trigger="click" placement="bottom-end">
          <span class="el-dropdown-link">
            <div class="iconfont icon-more"></div>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="addContact2BlackList">加入黑名单</el-dropdown-item>
              <el-dropdown-item @click="delContact">删除联系人</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="part-item">
      <div class="part-title">个性签名</div>
      <div class="part-content">{{ userInfo.personalSignature || '-' }}</div>
    </div>
    <div class="sendMessage" @click="sendMessage">
      <div class="iconfont icon-chat2"></div>
      <div class="test">发消息</div>
    </div>
  </ContentPanel>
</template>

<script setup>
import ContentPanel from '../../components/ContentPanel.vue'
import UserBaseInfo from '../../components/UserBaseInfo.vue'
import { useContactStateStore } from '../../store/contactStateStore'
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
const router = useRouter()
const contactStateStore = useContactStateStore()
const route = useRoute()
import { getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()
const userInfo = ref({})
//点击某人头像后进入聊天前的信息获取
const loadUserDetail = async (contactId) => {
  let result = await proxy.Request({
    url: proxy.api.getContactUserInfo,
    params: {
      contactId
    }
  })
  if (!result) {
    return
  }
  userInfo.value = result
}
//点击拉黑联系人函数
const addContact2BlackList = () => {
  proxy.confirm({
    message: '确定要将用户拉入黑名单吗？',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.addContact2BlackList,
        params: {
          contactId: userInfo.value.userId
        }
      })
      if (!result) {
        return
      }
      delContactData()
    }
  })
}
//点击删除联系人函数
const delContact = () => {
  proxy.confirm({
    message: '确定要删除用户吗？',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.delContact,
        params: {
          contactId: userInfo.value.userId
        }
      })
      if (!result) {
        return
      }
      delContactData()
    }
  })
}

//删除操作，使用过多次，提取出来
const delContactData = () => {
  contactStateStore.setContactReload('REMOVE_USER')
}
const sendMessage = () => {
  router.push({
    path: '/chat',
    query: {
      chatId: userInfo.value.userId,
      timestamp: new Date().getTime()
    }
  })
}
//实时监听路由参数变化，来确定打开谁的聊天框
watch(
  () => route.query.contactId,
  (newVal) => {
    // console.log(route.query)
    loadUserDetail(newVal)
  },
  {
    immediate: true
  }
)
</script>

<style lang="scss" scoped>
.user-info {
  position: relative;
  .more-op {
    position: absolute;
    right: 0;
    top: 20px;
    .icon-more {
      color: #9e9e9e;
      &:hover {
        background: #dddddd;
      }
    }
  }
}

.part-item {
  display: flex;
  border-bottom: 1px solid #eaeaea;
  padding: 20px 0;
  .part-title {
    width: 60px;
    color: #9e9e9e;
  }
  .part-content {
    flex: 1;
    margin-left: 15px;
    color: #161616;
  }
}

.sendMessage {
  width: 80px;
  margin: 0 auto;
  text-align: center;
  margin-top: 20px;
  color: #7d8cac;
  padding: 5px;
  .icon-chat2 {
    font-size: 23px;
  }
  .text {
    margin-top: 5px;
    font-size: 12px;
  }
  &:hover {
    cursor: pointer;
    background: #e9e9e9;
  }
}
</style>
