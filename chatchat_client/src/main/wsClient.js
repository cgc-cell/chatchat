const WebSocket = require('ws')
import store from './store'
import {
  saveOrUpdateChatSessionBatch4Init,
  selectUserSessionByContactId,
  updateGroupName
} from './db/ChatSessionUserModel'
import { saveMessage, saveMessageBatch, updateMessage } from './db/ChatMessageModel'
import { updateContactNoReadCount } from './db/UserSettingModel'
import { saveOrUpdate4Message } from './db/ChatSessionUserModel'
const NODE_ENV = process.env.NODE_ENV
let ws = null

let maxReconnectTimes = null
let wsUrl = null
let sender = null
let needReconnect = null
let lockReconnect = false
//进行ws的初始化包括设置基本信息和创建ws连接
const initWs = (config, _sender) => {
  wsUrl = `${NODE_ENV === 'development' ? store.getStore('proWsDomain') : store.getStore('devWsDomain')}?token=${config.token}`
  sender = _sender
  needReconnect = true
  maxReconnectTimes = 5
  createWs()
}
//正式创建ws连接函数
const createWs = () => {
  if (!wsUrl) {
    console.log('wsUrl is null')
    return
  }
  ws = new WebSocket(wsUrl)
  ws.onopen = function () {
    console.log('ws客户端连接成功')
    ws.send('heart beat')
    maxReconnectTimes = 5
  }
  ws.onmessage = async function (e) {
    // console.log('ws客户端收到消息:', e.data)
    const message = JSON.parse(e.data)
    const messageType = message.messageType
    const leaveGroupUserId = message.extendData
    console.log('messageType:', messageType)
    switch (messageType) {
      case 0: {
        //处理三个表的数据
        await saveOrUpdateChatSessionBatch4Init(message.extendData.chatSessionUserList)
        // console.log('message.extendData:', message.extendData)
        await saveMessageBatch(message.extendData.messageList)
        await updateContactNoReadCount({
          userId: store.getUserId(),
          noRead: message.extendData.applyCount
        })
        sender.send('receiveMessage', { messageType: message.messageType })
        break
      }
      case 4: {
        //接收好友申请
        // console.log('接收到好友申请消息:', message)
        await updateContactNoReadCount({ userId: store.getUserId(), noReadCount: 1 })
        sender.send('receiveMessage', { messageType: message.messageType })
        break
      }
      case 6: {
        //文件上传成功后通知渲染进程修改状态读取
        updateMessage({ status: 1 }, { messageId: message.messageId })
        sender.send('receiveMessage', message)
        break
      }
      case 10: {
        //修改群昵称
        await updateGroupName(message.contactId, message.extendData)
        sender.send('receiveMessage', message)
        break
      }
      case 7: {
        //进行强制下线
        sender.send('receiveMessage', message)
        closeWs()
        break
      }
      case 1: //添加好友成功
      case 3: //创建群成功
      case 8: //解散群聊
      case 9: //好友加入群聊
      case 11: //退出群聊
      case 12: //踢出群聊
      case 2: //普通消息
      case 5: { //图片/视频消息
        if (message.sendUserId === store.getUserId() && message.contactType === 1) {
          break
        }
        const sessionInfo = {}
        if (message.extendData && typeof message.extendData === 'object') {
          Object.assign(sessionInfo, message.extendData)
        } else {
          Object.assign(sessionInfo, message)
          if (message.contactType == 0 && messageType != 1) {
            sessionInfo.contactName = message.sendUserNickName
          }
          sessionInfo.lastReceiveTime = message.sendTime
        }
        //群聊处理
        if (messageType == 9 || messageType == 11 || messageType == 12) {
          sessionInfo.memberCount = message.memberCount
        }

        await saveOrUpdate4Message(store.getUserData('currentSessionId'), sessionInfo)
        await saveMessage(message)

        //更新数据库
        const dbSessionInfo = await selectUserSessionByContactId(message.contactId)
        message.extendData = dbSessionInfo
        if (messageType == 11 && leaveGroupUserId == store.getUserId()) {
          break
        }
        // console.log('接收到消息:', message)
        sender.send('receiveMessage', message)
        break
      }
    }
  }
  ws.onclose = function () {
    console.log('ws客户端关闭')
    reConnect()
  }
  ws.onerror = function (e) {
    console.log('ws客户端错误:', e)
    reConnect()
  }
  const reConnect = () => {
    if (!needReconnect) {
      console.log('变量显示不需要重连')
      return
    }
    if (ws != null) {
      console.log('ws不为空,先关闭ws')
      ws.close()
    }
    if (lockReconnect) {
      console.log('正在重连,请稍后')
      return
    }
    lockReconnect = true
    if (maxReconnectTimes > 0) {
      console.log('剩余重连次数:', maxReconnectTimes)
      maxReconnectTimes--
      setTimeout(() => {
        createWs()
        lockReconnect = false
      }, 5000)
    } else {
      console.log('重连次数已用完')
      ws.close()
    }
  }
  setInterval(() => {
    ws.send('heart beat')
    // console.log('发送心跳')
  }, 5000)
}

const closeWs = () => {
  needReconnect = false
  ws.close()
}
export { initWs, closeWs }
