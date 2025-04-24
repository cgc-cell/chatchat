<template>
  <ContentPanel
    v-infinite-scroll="loadApply"
    :show-top-border="true"
    :infinite-scroll-immediate="false"
  >
    <div v-for="item in applyList" :key="item.applyId" class="apply-item">
      <div :class="['contact-type', item.contactType == 0 ? 'user-contact' : '']">
        {{ item.contactType == 0 ? '好友' : '群聊' }}
      </div>
      <Avatar :width="50" :user-id="item.applyUserId"></Avatar>
      <div class="contact-info">
        <div class="nick-name">{{ item.contactName }}</div>
        <div class="apply-info">{{ item.applyInfo }}</div>
      </div>
      <div class="op-btn">
        <div v-if="item.status === 0">
          <el-dropdown trigger="click" placement="bottom-end">
            <span class="el-dropdown-link">
              <el-button type="primary" size="small">接受</el-button>
            </span>
            <template #dropdown>
              <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 1)"
                >同意</el-dropdown-item
              >
              <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 2)"
                >拒绝</el-dropdown-item
              >
              <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 3)"
                >拉黑</el-dropdown-item
              >
            </template>
          </el-dropdown>
        </div>
        <div v-else class="result-name">{{ item.statusName }}</div>
      </div>
    </div>
    <div v-if="applyList.length == 0" class="no-data">暂无申请</div>
  </ContentPanel>
</template>

<script setup>
import Avatar from '../../components/Avatar.vue'
import ContentPanel from '../../components/ContentPanel.vue'
import { ref, getCurrentInstance, watch } from 'vue'
import message from '../../Utils/Message'
import { useContactStateStore } from '../../store/contactStateStore'
import { useMessageCountStore } from '../../store/MessageCountStore'
const messageCountStore = useMessageCountStore()
const contactStateStore = useContactStateStore()
const { proxy } = getCurrentInstance()
//申请添加好友列表的请求
const applyList = ref([])
let pageNo = 0
let pageTotal = 1
const loadApply = async () => {
  pageNo++
  if (pageNo > pageTotal) {
    return
  }
  let result = await proxy.Request({
    url: proxy.api.loadApply,
    params: {}
  })
  if (!result) {
    console.log('loadApply result is null')
    return
  }
  pageTotal = result.pageTotal
  if (result.pageNo === 1) {
    applyList.value = []
  }
  //动态加载申请添加好友列表
  applyList.value = applyList.value.concat(result.list)
  pageNo = result.pageNo
}
loadApply()
//处理好友申请
const dealWithApply = (applyId, contactType, status) => {
  proxy.confirm({
    message: '是否同意该好友申请?',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.dealWithApply,
        params: {
          applyId: applyId,
          contactType: contactType,
          status: status
        }
      })
      if (!result) {
        return
      }
      message.success('操作成功')
      pageNo = 0
      loadApply()
      if (contactType == 0 && status == 1) {
        contactStateStore.setContactReload('USER')
      } else if (contactType == 1 && status == 1) {
        contactStateStore.setContactReload('GROUP')
      }
    }
  })
}

watch(
  () => messageCountStore.messageCount.contactAppltCount,
  (newVal) => {
    if (newVal) {
      pageNo = 0
      loadApply()
    }
  },
  {
    immediate: true
  }
)
</script>

<style lang="scss" scoped>
.apply-item {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ddd;
  padding: 10px 0;
  .contact-type {
    display: flex;
    justify-content: center;
    writing-mode: vertical-rl;
    vertical-align: middle;
    background: #2cb6fe;
    color: #fff;
    border-radius: 5px 0 0 5px;
    height: 50px;
  }
  .user-contact {
    background: #08bf61;
  }
  .contact-info {
    width: 260px;
    margin-left: 10px;
    .nick-name {
      color: #000000;
    }
    .apply-info {
      color: #999999;
      font-size: 12px;
      margin-top: 5px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  .op-btn {
    width: 50px;
    text-align: center;
    .result-name {
      color: #999999;
      font-size: 12px;
    }
  }
}
</style>
