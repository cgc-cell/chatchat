<template>
  <div v-if="showUpdate">
    <div class="update-container drag"></div>
    <div class="update-panel no-drag">
      <div class="update-inner">
        <div class="update-content">
          <div class="update-content-title">更新内容</div>
          <div class="update-list">
            <div v-for="(item, index) in updateInfo.updateList" :key="item">
              {{ index + 1 }}、{{ item }}
            </div>
          </div>
        </div>
        <div v-if="downloading" class="download-progress">
          <div v-if="downloadPercent.progress != 100">
            <el-progress :percentage="downloadPercent.progress"></el-progress>
            <div class="download-tips">
              正在下载，请稍后:({{ proxy.Utils.size2Str(downloadPercent.loaded) }}/{{
                proxy.Utils.size2Str(downloadPercent.total)
              }})
            </div>
          </div>
          <div v-else>下载完成，准备安装</div>
        </div>
        <div v-else class="op-btn">
          <div class="cancel" @click="cancelUpdateHandle">残忍拒绝</div>
          <div class="update" @click="startDownload">现在更新</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted, onUnmounted } from 'vue'
const { proxy } = getCurrentInstance()
import config from '../../../../package.json'
import { useUserInfoStore } from '../store/userInfoStore'
const userInfoStore = useUserInfoStore()
const props = defineProps({
  autoUpdate: {
    type: Boolean,
    default: true
  }
})

const showUpdate = ref(false)
const updateInfo = ref({
  size: 0,
  updateList: []
})

//检查更新
const checkUpdateAuto = async (auto) => {
  let result = await proxy.Request({
    url: proxy.api.checkVersion,
    params: {
      appVersion: config.version,
      token: localStorage.getItem('token'),
      uid: userInfoStore.getUserInfo().userId
    }
  })
  if (!result) {
    return
  }
  if (result.data == null) {
    if (!auto) {
      proxy.confirm({
        message: '当前已经是最新版本！',
        showCancelBtn: false
      })
    }

    return
  }
  showUpdate.value = true
  updateInfo.value = result
}

const cancelUpdateHandle = () => {
  showUpdate.value = false
}

const downloading = ref(false)
const downloadPercent = ref({
  progress: 0,
  loaded: 0,
  total: updateInfo.value.size
})
const startDownload = () => {
  if (updateInfo.value.fileType == 0) {
    downloading.value = true
    window.ipcRenderer.send('downloadUpdate', {
      id: updateInfo.value.id,
      fileName: updateInfo.value.fileName
    })
  } else if (updateInfo.value.fileType == 1) {
    window.ipcRenderer.send('openUrl', { url: updateInfo.value.outerLink })
  }
}

onMounted(() => {
  if (props.autoUpdate) {
    checkUpdateAuto(true)
  }
  window.ipcRenderer.on('upateDownloadCallback', (event, loaded) => {
    downloadPercent.value.loaded = loaded
    downloadPercent.value.progress = Math.floor((loaded / updateInfo.value.size) * 100)
  })
})

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('upateDownloadCallback')
})

const checkUpdate = () => {
  checkUpdateAuto(false)
}
defineExpose({ checkUpdate })
</script>

<style lang="scss" scoped>
.update-container {
  opacity: 0.2;
  background: #000;
  z-index: 1;
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: calc(100vh);
}

.update-panel {
  left: 0;
  top: 100px;
  width: 100%;
  position: absolute;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  .update-inner {
    background-size: 100%;
    background-image: url(../assets/img/update_bg.png);
    background-position: top center;
    background-repeat: no-repeat;
    width: 350px;
    min-height: 400px;
    .update-content {
      margin-top: 230px;
      background: #fff;
      padding: 15px;
      .update-content-title {
        font-size: 18px;
        color: #000;
      }
      .update-list {
        margin-top: 5px;
        max-height: 150px;
        overflow: hidden;
      }
    }
    .download-progress {
      background: #fff;
      padding: 10px;
      border-radius: 0 0 10px 10px;
      .download-tips {
        margin-top: 5px;
        text-align: center;
        font-size: 14px;
        color: #6e6e6e;
      }
    }
    .op-btn {
      background: #fff;
      display: flex;
      align-items: center;
      line-height: 40px;
      overflow: hidden;
      border-top: 1px solid #ddd;
      border-radius: 0 0 10px 10px;
      .cancel {
        width: 50%;
        text-align: center;
        color: #989898;
        cursor: pointer;
      }
      .update {
        width: 50%;
        background: #07c160;
        text-align: center;
        color: #fff;
        cursor: pointer;
        border-left: 1px solid #ddd;
      }
    }
  }
}
</style>
