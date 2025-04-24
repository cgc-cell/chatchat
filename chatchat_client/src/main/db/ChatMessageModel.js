import store from '../store'

import { insertOrReplace, queryAll, queryCount, queryOne, update } from './ADB'
import { updateNoReadCount } from './ChatSessionUserModel'
//插入单条数据(调用)
const saveMessage = (data) => {
  data.userId = store.getUserId()
  return insertOrReplace('chat_message', data)
}
//批量插入数据(执行)并更新未读数量
const saveMessageBatch = (chatMessageList) => {
  return new Promise((resolve) => {
    async function startFunction() {
      const chatSessionCountMap = {}
      chatMessageList.forEach((item) => {
        let contactId = item.contactType === 1 ? item.contactId : item.sendUserId
        let noReadCount = chatSessionCountMap[contactId]
        if (!noReadCount) {
          chatSessionCountMap[contactId] = 1
        } else {
          chatSessionCountMap[contactId] = noReadCount + 1
        }
      })
      //更新未读数量,在另一个表中进行更新
      for (let item in chatSessionCountMap) {
        await updateNoReadCount({ contactId: item, noReadCount: chatSessionCountMap[item] })
      }
      //批量插入此表格数据
      chatMessageList.forEach(async (item) => {
        await saveMessage(item)
      })
      resolve()
    }
    startFunction()
  })
}
//得到分页的信息(调用)
const getPageOffset = (pageNo = 1, totalCount) => {
  const pageSize = 20
  const offset = (pageNo - 1) * pageSize
  const pageTotal =
    totalCount % pageSize === 0 ? totalCount / pageSize : Number.parseInt(totalCount / pageSize) + 1
  pageNo < 1 ? 1 : pageNo
  pageNo > pageTotal ? pageTotal : pageNo
  return {
    pageTotal,
    offset,
    limit: pageSize
  }
}

//查询某个会话的聊天记录(调用)
const selectChatMessageList = ({ sessionId, pageNo, maxMessageId }) => {
  return new Promise((resolve) => {
    async function startFunction() {
      let sql = 'select count(1)  from chat_message where session_id=? and user_id=?'
      const totalCount = await queryCount(sql, [sessionId, store.getUserId()])
      // console.log('pageNo:', pageNo)
      const { offset, limit, pageTotal } = getPageOffset(pageNo, totalCount)
      // console.log('offset:', offset)
      // console.log('limit:', limit)
      // console.log('pageTotal:', pageTotal)
      const params = [sessionId, store.getUserId()]
      sql = 'select * from chat_message where session_id=? and user_id=?'
      if (maxMessageId) {
        sql += ' and message_id <= ?'
        params.push(maxMessageId)
      }
      params.push(offset)
      params.push(limit)
      sql += ' order by message_id desc limit ? , ?'
      const dataList = await queryAll(sql, params)
      // console.log('dataList:', dataList)
      resolve({ dataList, pageTotal, pageNo })
    }
    startFunction()
  })
}

const updateMessage = (data, paramData) => {
  paramData.userId = store.getUserId()
  return update('chat_message', data, paramData)
}
//通过id查询数据
const selectByMessageId = (messageId) => {
  const sql = 'select * from chat_message  where message_id=? and user_id=?'
  const params = [messageId, store.getUserId()]
  return queryOne(sql, params)
}

export { saveMessageBatch, selectChatMessageList, saveMessage, updateMessage, selectByMessageId }
