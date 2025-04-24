<template>
  <div class="send-panel">
    <div class="toolbar">
      <el-popover
        v-model="showEmojiPopover"
        trigger="click"
        placement="top"
        :teleported="false"
        :popper-style="{ width: '490px', padding: '0 10px 10px 10px' }"
        @show="openPopover"
      >
        <template #default>
          <el-tabs v-model="activeEmoji" @click.stop>
            <el-tab-pane
              v-for="emoji in emojiList"
              :key="emoji.name"
              :label="emoji.name"
              :name="emoji.name"
            >
              <div class="emoji-list">
                <div
                  v-for="item in emoji.list"
                  :key="item"
                  class="emoji-item"
                  @click="sendEmoji(item)"
                >
                  {{ item }}
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </template>
        <template #reference>
          <div class="iconfont icon-emoji" @click="showEmojiPopoverHandle"></div>
        </template>
      </el-popover>
      <el-upload
        ref="uploadRef"
        name="file"
        :show-file-list="false"
        :multiple="true"
        :limit="fileLimit"
        :http-request="uploadFile"
        :on-exceed="fileExceed"
        ><div class="iconfont icon-folder"></div
      ></el-upload>
    </div>
    <div class="input-area" @drop="dropHandle" @dragover="dragOverHandle">
      <el-input
        v-model="messageContent"
        rows="5"
        type="textarea"
        resize="none"
        maxlength="500"
        show-word-limit
        spellcheck="false"
        input-style="background:#f5f5f5;border:none;"
        @keydown.enter="sendMessage"
        @paste="pastFile"
      ></el-input>
    </div>
    <div class="send-btn-panel">
      <el-popover
        v-model="showSendMsgPopover"
        hide-after="1500"
        trigger="click"
        placement="top-end"
        :teleported="false"
        :popper-style="{ width: '120px', padding: '5px', 'min-width': '0' }"
        @show="openPopover"
        @hide="closePopover"
      >
        <template #default><span class="empty-msg">不能发送空白消息</span></template>
        <template #reference>
          <span class="send-btn" @click="sendMessage">发送(S)</span>
        </template>
      </el-popover>
    </div>
    <SearchAdd ref="searchAddRef"></SearchAdd>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted, onUnmounted } from 'vue'
import emojiList from '../../Utils/Emoji'
import { useUserInfoStore } from '../../store/userInfoStore'
import SearchAdd from '../contact/SearchAdd.vue'
import { getFileType } from '../../Utils/Constans'
import { useSysSettingStore } from '../../store/SysSettingStore'
const sysSettingStore = useSysSettingStore()
const userInfoStore = useUserInfoStore()
const activeEmoji = ref('人物')
const messageContent = ref('')
const showEmojiPopover = ref(false)
const showSendMsgPopover = ref(false)
const { proxy } = getCurrentInstance()

const props = defineProps({
  currentChatSession: {
    type: Object,
    default: () => {}
  }
})

//发送简单消息(执行)
const sendMessage = (e) => {
  if (e.shiftKey || e.keyCode === 13) {
    return
  }
  e.preventDefault()
  if (messageContent.value.trim() === '') {
    console.log('不能发送空白消息')
    showSendMsgPopover.value = true
    return
  }
  sendMessageDo({ messageContent: messageContent.value, messageType: 2 })
}
//发送所有类型的消息(调用)
const emit = defineEmits(['sendMessage4Local'])
const sendMessageDo = async (
  messageObj = {
    messageContent: undefined,
    messageType: undefined,
    localFilePath: undefined,
    fileSize: undefined,
    fileName: undefined,
    filePath: undefined,
    fileType: undefined
  },
  cleanMsgContent
) => {
  if (!checkMessage(messageObj.fileType, messageObj.fileSize, messageObj.fileName)) {
    console.log('文件大小超过限制')
    return
  }
  if (messageObj.fileSize == 0) {
    proxy.confirm({
      message: `${messageObj.fileName}文件为空请重新选择`,
      showCancelButton: false
    })
    return
  }
  messageObj.sendUserId = userInfoStore.getUserInfo().userId
  messageObj.sessionId = props.currentChatSession.sessionId

  let result = await proxy.Request({
    url: proxy.api.sendMessage,
    params: {
      messageContent: messageObj.messageContent,
      contactId: props.currentChatSession.contactId,
      messageType: messageObj.messageType,
      fileSize: messageObj.fileSize,
      fileName: messageObj.fileName,
      fileType: messageObj.fileType
    },
    showLoading: false,
    showError: false,
    errorCallback: (responceData) => {
      proxy.confirm({
        message: responceData.info,
        okfun: () => {
          addContact(props.currentChatSession.contactId, responceData.code)
        },
        okText: '重新申请'
      })
    }
  })
  cleanMsgContent = true
  if (!result) {
    console.log('发送消息失败')
    return
  }
  if (cleanMsgContent) {
    messageContent.value = ''
  }
  Object.assign(messageObj, result)
  console.log('messageObj', messageObj)
  console.log('result', result)
  emit('sendMessage4Local', messageObj)
  window.ipcRenderer.send('addLocalMessage', messageObj)
}

//发送消息失败，需要添加好友
const searchAddRef = ref()
const addContact = (constactId, code) => {
  searchAddRef.value.show({ contactId: constactId, contactType: code == 902 ? 'USER' : 'GROUP' })
}

//得到文件类型(调用)
const getFileTypeByName = (fileName) => {
  const fileSuffix = fileName.slice(fileName.lastIndexOf('.') + 1)
  return getFileType(fileSuffix)
}

//文件上传(调用)
const uploadFileDo = (file) => {
  const fileType = getFileTypeByName(file.name)
  sendMessageDo(
    {
      messageContent: '[' + getFileType(fileType) + ']',
      messageType: 5,
      fileSize: file.size,
      fileName: file.name,
      fileType: fileType,
      filePath: file.path
    },
    false
  )
}
const uploadRef = ref()
//上传文件(执行)
const uploadFile = (file) => {
  uploadFileDo(file.file)
  uploadRef.value.clearFiles()
}

//管理文件大小限制(调用)
const checkMessage = (fileType, fileSize, fileName) => {
  const SIZE_MB = 1024 * 1024
  const settingAarry = Object.values(sysSettingStore.getSetting())
  // console.log('settingAarry', settingAarry)
  const fileSizeNumber = settingAarry[fileType]
  if (fileSize > (fileSizeNumber + 10) * SIZE_MB) {
    proxy.confirm({
      message: `${fileName}文件大小超过限制${fileSizeNumber}MB`,
      showCancelButton: false
    })
    return false
  }
  return true
}
//管理文件数量
const fileLimits = 10
const checkFileLimit = (files) => {
  if (files.length > fileLimits) {
    proxy.confirm({
      message: `文件数量超过限制${fileLimits}个`,
      showCancelButton: false
    })
    return false
  }
  return true
}
const fileExceed = (files) => {
  checkFileLimit(files)
}
//拖入文件上传
const dragOverHandle = (e) => {
  e.preventDefault()
}

const dropHandle = (e) => {
  e.preventDefault()
  const files = e.dataTransfer.files
  if (!checkFileLimit(files)) {
    return
  }
  for (let i = 0; i < files.length; i++) {
    uploadFileDo(files[i])
  }
}
//粘贴文件上传
const pastFile = (e) => {
  let items = e.clipboardData && e.clipboardData.items //安全嵌套
  for (let item of items) {
    if (item.kind != 'file') {
      console.log('出现非文件类型')
      return
    }
    const fileData = {}
    const file = item.getAsFile()
    // console.log('file', file)
    if (file.path != '') {
      uploadFileDo(file)
    } else {
      const imageFile = new File([file], 'temp.png')
      let fileReader = new FileReader()
      fileReader.onload = function () {
        const byteArray = new Uint8Array(this.result)
        fileData.byteArray = byteArray
        fileData.name = imageFile.name
        window.ipcRenderer.send('saveClipboardFile', fileData)
      }
      fileReader.readAsArrayBuffer(imageFile)
    }
  }
}
//管理表情包
const sendEmoji = (emoji) => {
  messageContent.value += emoji
  showEmojiPopover.value = false
}
const openPopover = () => {}
const closePopover = () => {}
const showEmojiPopoverHandle = () => {
  showEmojiPopover.value = true
}

onMounted(() => {
  window.ipcRenderer.on('saveClipboardFileCallback', (e, file) => {
    const fileType = 0
    sendMessageDo(
      {
        messageContent: '[' + getFileType(fileType) + ']',
        messageType: 5,
        fileSize: file.size,
        fileName: file.name,
        fileType: fileType,
        filePath: file.path
      },
      false
    )
  })
})

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('saveClipboardFileCallback')
})
</script>

<style lang="scss" scoped>
.send-panel {
  height: 200px;
  border-top: 1px solid #ddd;
  .toolbar {
    height: 40px;
    display: flex;
    align-items: center;
  }
  .iconfont {
    color: #494949;
    font-size: 20px;
    margin-left: 10px;
    cursor: pointer;
    :deep(.el-tabs_header) {
      margin-bottom: 0px;
    }
  }
  .input-area {
    padding: 0 10px;
    outline: none;
    width: 100%;
    height: 115px;
    overflow: auto;
    word-wrap: break-word;
    word-break: break-all;
    :deep(.el-textarea__inner) {
      box-shadow: none;
    }
    :deep(.el-input__count) {
      background: none;
      right: 12px;
    }
  }
  .send-btn-panel {
    text-align: right;
    padding-top: 10px;
    margin-right: 22px;
    .send-btn {
      cursor: pointer;
      color: #07c160;
      background: #e9e9e9;
      padding: 8px 25px;
      border-radius: 5px;
      &:hover {
        background: #d2d2d2;
      }
    }
    .empty-msg {
      font-size: 13px;
    }
  }
}
.emoji-list {
  .emoji-item {
    cursor: pointer;
    float: left;
    font-size: 23px;
  }
}
</style>
