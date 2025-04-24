import { insertOrIgnore, queryAll, queryOne, run, update } from './ADB'
import store from '../store'

//添加或者更新会话(调用)
const addChatSession = (sessionInfo) => {
  sessionInfo.userId = store.getUserId()
  insertOrIgnore('chat_session_user', sessionInfo)
}
//查询自身和单个联系人的会话(调用)
const selectUserSessionByContactId = (contactId) => {
  let sql = 'select * from chat_session_user where user_id=? and contact_id = ?'
  return queryOne(sql, [store.getUserId(), contactId])
}
//更新会话状态(执行)
const updateChatSession = (sessionInfo) => {
  const paramData = {
    userId: store.getUserId(),
    contactId: sessionInfo.contactId
  }
  const updateInfo = Object.assign({}, sessionInfo)
  updateInfo.userId = undefined
  updateInfo.contactId = undefined
  return update('chat_session_user', updateInfo, paramData)
}
//查询自身和群组的会话(执行)
const saveOrUpdateChatSessionBatch4Init = (chatSessionList) => {
  return new Promise((resolve) => {
    async function startFunction() {
      try {
        for (let i = 0; i < chatSessionList.length; i++) {
          const sessionInfo = chatSessionList[i]
          sessionInfo.status = 1
          let sessionData = await selectUserSessionByContactId(sessionInfo.contactId)
          // console.log('sessionData:', sessionData)
          if (sessionData) {
            await updateChatSession(sessionInfo)
          } else {
            await addChatSession(sessionInfo)
          }
        }
        resolve()
      } catch {
        resolve()
      }
    }
    startFunction()
  })
}

//更新未读数量(执行)
const updateNoReadCount = ({ contactId, noReadCount }) => {
  const sql =
    'update chat_session_user set no_read_count = no_read_count + ? where user_id = ? and contact_id = ?'
  return run(sql, [noReadCount, store.getUserId(), contactId])
}
//查询此表的数据库内容(导出)
const selectUserSessionList = () => {
  let sql = 'select * from chat_session_user where user_id=? and status=1'
  return queryAll(sql, [store.getUserId()])
}
//删除聊天行列里的某一个会话
const delChatSession = (contactId) => {
  let paramData = {
    userId: store.getUserId(),
    contactId
  }
  let sessionInfo = {
    status: 0
  }
  return update('chat_session_user', sessionInfo, paramData)
}
const topChatSession = (contactId, topType) => {
  let paramData = {
    userId: store.getUserId(),
    contactId
  }
  let sessionInfo = {
    topType
  }
  return update('chat_session_user', sessionInfo, paramData)
}

//某人发送消息后更新的一系列操作
const updateSessionInfo4Message = (
  currentSessionId,
  { sessionId, contactName, lastMessage, lastReceiveTime, contactId, memberCount }
) => {
  const params = [lastMessage, lastReceiveTime]
  let sql = 'update chat_session_user set last_message = ? ,last_receive_time = ? ,status = 1'
  if (contactName) {
    sql += ',contact_name = ?'
    params.push(contactName)
  }
  if (memberCount != null) {
    sql += ',member_count = ?'
    params.push(memberCount)
  }
  if (sessionId != currentSessionId) {
    sql += ',no_read_count = no_read_count + 1'
  }
  sql += ' where user_id = ? and contact_id = ?'
  params.push(store.getUserId())
  params.push(contactId)
  return run(sql, params)
}

//设置会话选中后清空未读数量
const readAll = (contactId) => {
  let sql = 'update chat_session_user set no_read_count = 0 where user_id = ? and contact_id = ?'
  return run(sql, [store.getUserId(), contactId])
}

//发消息后更新被发送人的会话列表
const saveOrUpdate4Message = (currentSessionId, sessionInfo) => {
  return new Promise((resolve) => {
    async function startFunction() {
      let sessionData = await selectUserSessionByContactId(sessionInfo.contactId)
      if (sessionData) {
        updateSessionInfo4Message(currentSessionId, sessionInfo)
      } else {
        sessionInfo.noReadCount = 1
        await addChatSession(sessionInfo)
      }
      resolve()
    }
    startFunction()
  })
}

const updateGroupName = (contactId, groupName) => {
  const paramData = {
    userId: store.getUserId(),
    contactId
  }
  const sessionInfo = {
    contactName: groupName
  }
  return update('chat_session_user', sessionInfo, paramData)
}

const updateStatus = (contactId) => {
  const paramData = {
    userId: store.getUserId(),
    contactId
  }
  const sessionInfo = {
    status: 1
  }
  return update('chat_session_user', sessionInfo, paramData)
}
export {
  saveOrUpdateChatSessionBatch4Init,
  updateNoReadCount,
  selectUserSessionList,
  delChatSession,
  topChatSession,
  updateSessionInfo4Message,
  readAll,
  saveOrUpdate4Message,
  selectUserSessionByContactId,
  updateGroupName,
  updateStatus
}
