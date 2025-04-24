<template>
  <div class="media-window">
    <div class="win-title drag"></div>
    <div class="media-op no-drag">
      <div
        :class="['iconfont icon-left', currentIndex == 0 ? 'not-allow' : '']"
        title="上一张"
        @dblclick.stop
        @click="next(-1)"
      ></div>
      <div
        :class="['iconfont icon-right', currentIndex >= allFileList.length - 1 ? 'not-allow' : '']"
        title="下一张"
        @dblclick.stop
        @click="next(1)"
      ></div>
      <template v-if="fileList[0].fileType == 0">
        <el-divider direction="vertical"></el-divider>
        <div
          class="iconfont icon-enlarge"
          title="放大"
          @click.stop="changeSize(0.1)"
          @dblclick.stop
        ></div>
        <div
          class="iconfont icon-narrow"
          title="缩小"
          @click.stop="changeSize(-0.1)"
          @dblclick.stop
        ></div>
        <div
          :class="['iconfont', isOne2One ? 'icon-resize' : 'icon-source-size']"
          :title="isOne2One ? '图片适应窗口大小' : '图片原始大小'"
          @dblclick.stop
          @click="resize"
        ></div>
        <div class="iconfont icon-rotate" title="旋转" @click="rotate" @dblclick.stop></div>
      </template>
      <el-divider direction="vertical"></el-divider>
      <div class="iconfont icon-download" title="另存为..." @click="saveAs" @dblclick.stop></div>
    </div>
    <div class="media-panel">
      <viewer
        v-if="fileList[0].fileType == 0 && fileList[0].status == 1"
        :options="options"
        :images="fileList"
        @inited="inited"
      >
        <img :src="fileList[0].url" />
      </viewer>
      <div
        v-show="fileList[0].fileType == 1 && fileList[0].status == 1"
        id="player"
        ref="player"
        style="width: 100%; height: 100%"
      ></div>
      <div v-if="fileList[0].fileType == 2" class="file-panel">
        <div class="file-item">文件名：{{ fileList[0].fileName }}</div>
        <div class="file-item">文件大小:{{ Utils.size2Str(fileList[0].fileSize) }}</div>
        <div class="file-item download">
          <el-button type="primary" @click="saveAs">下载文件</el-button>
        </div>
      </div>
      <div v-if="fileList[0].status != 1" class="loading">加载中....</div>
    </div>
    <WinOp @close-callback="closeWin"></WinOp>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import WinOp from '../../components/WinOp.vue'
import 'viewerjs/dist/viewer.css'
import { component as Viewer } from 'v-viewer'
import DPlayer from 'dplayer'
import Utils from '../../Utils/Utils'
const currentIndex = ref(0)
const allFileList = ref([])
const fileList = ref([{ fileType: 0, status: 0 }])
const closeWin = () => {
  if (dplayer.value) {
    dplayer.value.pause()
  }
}
const options = {
  toolbar: false,
  inline: true,
  button: false,
  title: false,
  navbar: false,
  zoomRatio: 0.1,
  zoomOnWheel: false
}

const viewerMy = ref(null)
const inited = (e) => {
  viewerMy.value = e
}

const changeSize = (zoomRatio) => {
  if (viewerMy.value == null) {
    return
  }
  viewerMy.value.zoom(zoomRatio, true)
}
const rotate = () => {
  viewerMy.value.rotate(90, true)
}
const isOne2One = ref(false)
const resize = () => {
  isOne2One.value = !isOne2One.value
  if (!isOne2One.value) {
    viewerMy.value.zoomTo(viewerMy.value.initialImageData.radio, true)
  } else {
    viewerMy.value.zoomTo(1, true)
  }
}
const onWheel = (e) => {
  if (fileList.value[0].fileType != 0) {
    return
  }
  console.log('e', e)

  if (e.deltaY < 0) {
    changeSize(0.1)
  } else {
    changeSize(-0.1)
  }
}
const localServerPort = ref(0)
//拿到当前文件信息
const getCurrentFile = () => {
  if (dplayer.value) {
    dplayer.value.switchVideo({
      url: ''
    })
  }
  const curFile = allFileList.value[currentIndex.value]
  // console.log('curFile', curFile)
  const url = getUrl(curFile)
  fileList.value.splice(0, 1, {
    url: url,
    fileType: curFile.fileType,
    status: 1,
    fileSize: curFile.fileSize,
    fileName: curFile.fileName
  })

  if (dplayer.value) {
    dplayer.value.switchVideo({
      url: url
    })
  }
}

//上一张与下一张函数
const next = (index) => {
  if (currentIndex.value + index < 0 || currentIndex.value + index >= allFileList.value.length) {
    return
  }
  currentIndex.value += index
  getCurrentFile()
}
//拿到图片路径
const getUrl = (curFile) => {
  return `http://127.0.0.1:${localServerPort.value}/file?fileId=${curFile.fileId}&partType=${curFile.partType}&fileType=${curFile.fileType}&forceGet=${curFile.forceGet}&${new Date().getTime()}`
}

const player = ref()
const dplayer = ref()
const initPlayer = () => {
  dplayer.value = new DPlayer({
    element: player.value,
    theme: '#b7daff',
    screenshot: true,
    video: {
      url: ''
    }
  })
}
//保存文件函数
const saveAs = () => {
  const curFile = allFileList.value[currentIndex.value]
  window.ipcRenderer.send('saveAs', {
    partType: curFile.partType,
    fileId: curFile.fileId
  })
}
//关闭此时窗口函数
onMounted(() => {
  initPlayer()
  window.ipcRenderer.on('pageInitData', (e, data) => {
    // console.log('data', data)
    allFileList.value = data.fileList
    localServerPort.value = data.localServerPort
    let index = 0
    if (data.currentFileId) {
      console.log('data.currentFileId', data.currentFileId)
      index = allFileList.value.findIndex((item) => item.fileId == data.currentFileId)
      index = index == -1 ? 0 : index
    }
    currentIndex.value = index
    getCurrentFile()
  })
  document.addEventListener('wheel', onWheel)
})
onUnmounted(() => {
  document.removeEventListener('wheel', onWheel)
  window.ipcRenderer.removeAllListeners('pageInitData')
})
</script>

<style lang="scss" scoped>
.media-window {
  padding: 0;
  height: calc(100vh);
  border: 1px solid #ddd;
  background: #fff;
  position: relative;
  overflow: hidden;
  .win-title {
    height: 37px;
  }
  .media-op {
    position: absolute;
    top: 0;
    left: 0;
    height: 35px;
    line-height: 35px;
    display: flex;
    align-items: center;
    .iconfont {
      font-size: 18px;
      padding: 0 10px;
      &:hover {
        background: #f3f3f3;
        cursor: pointer;
      }
    }
    .not-allow {
      cursor: not-allowed;
      color: #ddd;
      text-decoration: none;
      &:hover {
        cursor: not-allowed;
        color: #ddd;
        background: none;
      }
    }
  }
  .media-panel {
    height: calc(100vh - 37px);
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    :deep(.viewer-backdrop) {
      background: #f5f5f5;
    }
    .file-panel {
      .file-item {
        margin-top: 5px;
      }
    }
    .download {
      margin-top: 20px;
      text-align: center;
    }
  }
}
</style>
