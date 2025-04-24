<template>
  <Layout>
    <template #left-content>
      <div class="drag-panel drag"></div>
      <div class="top-search">
        <!--input输入-->
        <el-input v-model="searchKey" clearable placeholder="提示信息" size="small" @keyup="search">
          <template #suffix>
            <!--这个插槽的作用是在输入框后进行插入-->
            <span class="iconfont icon-search"></span>
          </template>
        </el-input>
      </div>
      <div v-if="!searchKey" class="chat-session-list">
        <template v-for="item in chatSessionList" :key="item.contactId">
          <ChatSession
            :data="item"
            :current-session="currentChatSession.contactId == item.contactId"
            @contextmenu.stop="onContextMenu(item, $event)"
            @click="chatSessionClickHandle(item)"
          ></ChatSession>
        </template>
      </div>
      <div v-show="searchKey" class="search-list">
        <SearchResult
          v-for="item in searchList"
          :key="item.contactId"
          :data="item"
          @click="searchClickHandle(item)"
        ></SearchResult>
      </div>
    </template>
    <template #right-content>
      <div v-if="Object.keys(currentChatSession).length > 0" class="title-panel drag">
        <div class="title">
          <span>{{ currentChatSession.contactName }}</span>
          <span v-if="currentChatSession.contactType == 1"
            >({{ currentChatSession.memberCount }})</span
          >
        </div>
      </div>
      <div
        v-if="currentChatSession.contactType == 1"
        class="iconfont icon-more no-drag"
        @click="showGroupDetail"
      ></div>
      <div v-show="Object.keys(currentChatSession).length > 0" class="chat-panel">
        <div id="message-panel" class="message-panel">
          <div
            v-for="(data, index) in messageList"
            :id="'message' + data.messageId"
            :key="data.sessionId"
            class="message-item"
          >
            <!-- 展示聊天时候的时间 -->
            <template
              v-if="
                index > 1 &&
                data.sendTime - messageList[index - 1].sendTime > 300000 &&
                (data.messageType == 2 || data.messageType == 5)
              "
              ><ChatMessageTime :data="data"></ChatMessageTime
            ></template>
            <!-- 展示系统消息 1:添加好友成功 3：群创建成功 9：好友加入群聊 11：退出群聊 12：被踢出群聊-->
            <template
              v-if="
                data.messageType == 1 ||
                data.messageType == 3 ||
                data.messageType == 9 ||
                data.messageType == 8 ||
                data.messageType == 11 ||
                data.messageType == 12
              "
              ><ChatMessageSys :data="data"></ChatMessageSys
            ></template>
            <template
              v-if="data.messageType === 1 || data.messageType === 2 || data.messageType === 5"
            >
              <ChatMessage
                :data="data"
                :current-chat-session="currentChatSession"
                @show-media-detail="showMediaDetailHandle"
              ></ChatMessage>
            </template>
          </div>
        </div>
        <MessageSend
          :current-chat-session="currentChatSession"
          @send-message4-local="sendMessage4LocalHandle"
        ></MessageSend>
      </div>
      <div v-show="Object.keys(currentChatSession).length == 0">
        <Blank></Blank>
      </div>
    </template>
  </Layout>
  <ChatGroupDetail
    ref="chatGroupDetailRef"
    @del-chat-session-callback="delChatSessionCallBack"
  ></ChatGroupDetail>
  <WinOp></WinOp>
</template>
<script>
export default {
  name: 'Chat'
}
</script>

<script setup>
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import Layout from '../../components/Layout.vue'
import ChatSession from './ChatSession.vue'
import ContextMenu from '@imengyu/vue3-context-menu'
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import { getCurrentInstance } from 'vue'
import WinOp from '../../components/WinOp.vue'
import MessageSend from './MessageSend.vue'
import ChatMessage from './ChatMessage.vue'
import Blank from '../../components/Blank.vue'
import ChatMessageTime from './ChatMessageTime.vue'
import { useAvatarUpdateStore } from '../../store/AvatarUpdateStore'
import ChatGroupDetail from '../contact/ChatGroupDetail.vue'
import ChatMessageSys from './ChatMessageSys.vue'
import { useMessageCountStore } from '../../store/MessageCountStore'
import { useRoute } from 'vue-router'
import SearchResult from './SearchResult.vue'
const route = useRoute()
const messageCountStore = useMessageCountStore()
const avatarUpdateStore = useAvatarUpdateStore()
const { proxy } = getCurrentInstance()
const searchKey = ref('')
let searchList = ref([])
//搜索功能
const search = () => {
  if (!searchKey.value) {
    return
  }
  searchList.value = []
  const regx = new RegExp('(' + searchKey.value + ')', 'gi')
  chatSessionList.value.forEach((item) => {
    if (item.contactName.includes(searchKey.value) || item.lastMessage.includes(searchKey.value)) {
      let newData = Object.assign({}, item)
      newData.searchContactName = newData.contactName.replace(
        regx,
        '<span class="highlight">$1</span>'
      )
      newData.searchLastMessage = item.lastMessage.replace(
        regx,
        '<span class="highlight">$1</span>'
      )
      searchList.value.push(newData)
    }
  })
}
const searchClickHandle = (data) => {
  chatSessionClickHandle(data)
  searchKey.value = ''
}
//向主进程发送拿取聊天记录的信息(调用)
const loadChatSession = () => {
  window.ipcRenderer.send('loadSessionData')
}
//拿到主进程里的数据(执行)
const chatSessionList = ref([])
const onLoadSessionData = () => {
  window.ipcRenderer.on('loadSessionDataCallback', (e, dataList) => {
    let noReadCount = 0
    dataList.forEach((item) => {
      noReadCount += item.noReadCount
    })
    messageCountStore.setCount('chatCount', noReadCount, true)
    sortChatSessionList(dataList)
    // console.log('loadSessionDataCallback', dataList)
    chatSessionList.value = dataList
    
  })
}
const onReceiveMessage = () => {
  window.ipcRenderer.on('receiveMessage', (e, message) => {
    // console.log('receiveMessage', message)
    if (message.messageType == 4) {
      loadContactApply()
      return
    }
    if (message.messageType == 7) {
      proxy.confirm({
        message: '您已被管理员强制下线',
        okfun: () => {
          setTimeout(() => {
            window.ipcRenderer.send('reLogin')
          }, 200)
        },
        showCancelButton: false
      })
      return
    }
    if (message.messageType == 10) {
      let curSession = chatSessionList.value.find((item) => item.contactId == message.contactId)
      if (curSession != null) {
        curSession.contactName = message.extendData
      }
      return
    }
    if (message.messageType == 6) {
      const localMessage = messageList.value.find((item) => item.messageId == message.messageId)
      if (localMessage != null) {
        localMessage.status = 1
      }
      return
    }
    let curSession = chatSessionList.value.find((item) => item.sessionId == message.sessionId)
    if (curSession == null) {
      chatSessionList.value.push(message.extendData)
    } else {
      Object.assign(curSession, message.extendData)
    }
    sortChatSessionList(chatSessionList.value)
    if (message.sessionId != currentChatSession.value.sessionId) {
      messageCountStore.setCount('chatCount', 1, false)
    } else {
      Object.assign(currentChatSession.value, message.extendData)
      messageList.value.push(message)
      gotoBottom()
    }
  })
}
//会话排序规则函数
const sortChatSessionList = (dataList) => {
  dataList.sort((a, b) => {
    const topTypeResult = b['topType'] - a['topType']
    if (topTypeResult == 0) {
      return b['lastReceiveTime'] - a['lastReceiveTime']
    }
    return topTypeResult
  })
}
//会话删除函数(调用)
const delChatSessionList = (contactId) => {
  setTimeout(() => {
    chatSessionList.value = chatSessionList.value.filter((item) => item.contactId !== contactId)
  }, 100)
}
//置顶函数，与主进程进行交互
const setTop = (data) => {
  data.topType = data.topType === 0 ? 1 : 0
  sortChatSessionList(chatSessionList.value)
  chatSessionList.value = [...chatSessionList.value]
  window.ipcRenderer.send('topChatSession', { contactId: data.contactId, topType: data.topType })
}
const currentChatSession = ref({})
//分页相关信息
const messageInfo = {
  totalPage: 0,
  pageNo: 0,
  maxMessageId: null,
  noData: false
}
const messageList = ref([])
//去取到相关数据(调用)
const loadChatMessage = () => {
  if (messageInfo.noData) {
    console.log('没有数据了')
    return
  }
  messageInfo.pageNo++
  // console.log('messageInfo.pageNo++', messageInfo.pageNo)

  window.ipcRenderer.send('loadChatMessage', {
    sessionId: currentChatSession.value.sessionId,
    pageNo: messageInfo.pageNo,
    maxMessageId: messageInfo.maxMessageId
  })
}
//拿到相关数据(执行)
const onLoadChatMessage = () => {
  window.ipcRenderer.on('loadChatMessageCallback', (e, { dataList, totalPage, pageNo }) => {
    if (totalPage == pageNo) {
      messageInfo.noData = true
    }
    dataList.sort((a, b) => {
      return a.messageId - b.messageId
    })
    // console.log(dataList)
    // console.log(totalPage)
    // console.log(pageNo)
    const lastMessage = messageList.value[0]
    console.log(lastMessage)
    messageList.value = dataList.concat(messageList.value)
    messageInfo.totalPage = totalPage
    messageInfo.pageNo = pageNo
    if (pageNo == 1) {
      messageInfo.maxMessageId =
        dataList.length > 0 ? dataList[dataList.length - 1].messageId : null
      //滚动到底部
      gotoBottom()
    } else {
      nextTick(() => {
        console.log('转到最后一条')
        console.log(lastMessage.messageId)
        document.querySelector('#message' + lastMessage.messageId).scrollIntoView()
      })
    }
    // console.log(messageList.value)
  })
}

//点击后保存当前的会话Id
const setSessionSelect = ({ contactId, sessionId }) => {
  window.ipcRenderer.send('setSessionSelect', { contactId, sessionId })
}
let distanceBottom = 0
//渲染相关消息记录(执行)
const chatSessionClickHandle = (item) => {
  distanceBottom = 0
  currentChatSession.value = Object.assign({}, item)

  // console.log('currentChatSession', currentChatSession.value)
  messageCountStore.setCount('chatCount', -item.noReadCount, false)
  item.noReadCount = 0
  messageList.value = []
  messageInfo.maxMessageId = null
  messageInfo.noData = false
  messageInfo.pageNo = 0
  messageInfo.totalPage = 1
  loadChatMessage()
  setSessionSelect({ contactId: item.contactId, sessionId: item.sessionId })
}
//删除函数，与主进程进行交互
const delChatSession = (contactId) => {
  delChatSessionList(contactId)
  setSessionSelect({})
  currentChatSession.value = {}
  window.ipcRenderer.send('delChatSession', contactId)
}
//对人头像进行右击出现框的函数
const onContextMenu = (data, e) => {
  ContextMenu.showContextMenu({
    x: e.x,
    y: e.y,
    items: [
      {
        label: data.topType == 0 ? '置顶' : '取消置顶',
        onClick: () => {
          setTop(data)
        }
      },
      {
        label: '删除会话',
        onClick: () => {
          proxy.confirm({
            message: `确定删除${data.contactName}会话吗？`,
            okfun: () => {
              delChatSession(data.contactId)
            }
          })
        }
      }
    ]
  })
}
//点击发送添加消息并立即更新关于自己的消息页面

const sendMessage4LocalHandle = (messageObj) => {
  messageList.value.push(messageObj)
  const chatSession = chatSessionList.value.find((item) => item.sessionId == messageObj.sessionId)
  if (chatSession) {
    chatSession.lastMessage = messageObj.messageContent
    chatSession.lastReceiveTime = messageObj.sendTime
  }
  sortChatSessionList(chatSessionList.value)
  gotoBottom()
}
//发送消息后到达底部
const gotoBottom = () => {
  if (distanceBottom > 200) {
    console.log('不用滚动到底部了')
    return
  }
  nextTick(() => {
    const items = document.querySelectorAll('.message-item')
    if (items.length > 0) {
      setTimeout(() => {
        items[items.length - 1].scrollIntoView()
      }, 100)
    }
  })
}

//发送视频等媒体消息成功后的回调
const onAddLocalMessageCallback = () => {
  window.ipcRenderer.on('addLocalMessageCallback', (e, { messageId, status }) => {
    const findMessage = messageList.value.find((item) => item.messageId == messageId)
    if (findMessage != null) {
      findMessage.status = status
    }
  })
}

//显示媒体详情
const showMediaDetailHandle = (messageId) => {
  let showFlieList = messageList.value.filter((item) => item.messageType == 5)
  showFlieList = showFlieList.map((item) => {
    console.log('item::', item)
    return {
      partType: 'chat',
      fileId: item.messageId,
      fileType: item.fileType,
      fileName: item.fileName,
      fileSize: item.fileSize,
      forceGet: avatarUpdateStore.getForceReload(item.userId)
    }
  })
  window.ipcRenderer.send('newWindow', {
    windowId: 'media',
    title: '图片查看',
    path: '/showMedia',
    data: {
      currentFileId: messageId,
      fileList: showFlieList
    }
  })
}

//显示群聊详情
const chatGroupDetailRef = ref()
const showGroupDetail = () => {
  chatGroupDetailRef.value.show(currentChatSession.value.contactId)
}

//当退出群聊后的回调删除该聊天会话
const delChatSessionCallBack = (contactId) => {
  delChatSession(contactId)
}

//加载未读消息数
const loadContactApply = () => {
  window.ipcRenderer.send('loadContactApply')
}
const onLoadContactApply = () => {
  window.ipcRenderer.on('loadContactApplyCallback', (e, contactNoRead) => {
    messageCountStore.setCount('contactApplyCount', contactNoRead, true)
  })
}

const sendMessage = (chatId) => {
  let curSession = chatSessionList.value.find((item) => item.contactId == chatId)
  if (!curSession) {
    window.ipcRenderer.send('reloadChatSession', { contactId: chatId })
  } else {
    chatSessionClickHandle(curSession)
  }
}

const onReloadChatSession = () => {
  window.ipcRenderer.on('reloadChatSessionCallback', (e, { contactId, chatSessionList }) => {
    sortChatSessionList(chatSessionList)
    sendMessage(contactId)
  })
}

//监听点击别人头像后跳过来的聊天
watch(
  () => route.query.timestamp,
  (newVal) => {
    if (newVal && route.query.chatId) {
      sendMessage(route.query.chatId)
    }
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  onLoadContactApply()
  onReceiveMessage()
  onLoadSessionData()
  loadChatSession()
  onLoadChatMessage()
  onAddLocalMessageCallback()
  loadContactApply()
  onReloadChatSession()
  nextTick(() => {
    const messagePanel = document.querySelector('#message-panel')
    messagePanel.addEventListener('scroll', (e) => {
      const scrollTop = e.target.scrollTop
      distanceBottom = e.target.scrollHeight - e.target.clientHeight - scrollTop
      if (scrollTop == 0 && messageList.value.length > 0) {
        loadChatMessage()
      }
    })
  })
  setSessionSelect({})
})

//页面结束删除监听
onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('loadSessionDataCallback')
  window.ipcRenderer.removeAllListeners('receiveMessage')
  window.ipcRenderer.removeAllListeners('loadChatMessageCallback')
  window.ipcRenderer.removeAllListeners('addLocalMessageCallback')
  window.ipcRenderer.removeAllListeners('loadContactApplyCallback')
  window.ipcRenderer.removeAllListeners('reloadChatSessionCallback')
})
</script>

<style lang="scss" scoped>
.drag-panel {
  height: 25px;
  background: #f7f7f7;
}
.top-search {
  padding: 0 10px 9px 10px;
  background: #f7f7f7;
  display: flex;
  align-items: center;
  .iconfont {
    font-size: 12px;
  }
}
.chat-session-list {
  height: calc(100vh - 62px);
  overflow: hidden;
  border-top: 1px solid #ddd;
  &:hover {
    overflow-y: auto;
  }
}
.chat-session-list::-webkit-scrollbar {
  display: none;
}
.title-panel {
  display: flex;
  align-items: center;
  .title {
    height: 60px;
    line-height: 60px;
    padding-left: 10px;
    font-size: 18px;
    color: #000000;
    flex: 1;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
.icon-more {
  position: absolute;
  top: 30px;
  right: 3px;
  z-index: 1;
  width: 20px;
  font-size: 20px;
  cursor: pointer;
  margin-right: 5px;
}
.chat-panel {
  background: #f5f5f5;
  border-top: 1px solid #ddd;
  .message-panel {
    padding: 10px 30px 0 30px;
    overflow-y: auto;
    height: calc(100vh - 200px - 62px);
    .message-item {
      margin-bottom: 15px;
      text-align: center;
    }
  }
}
</style>
