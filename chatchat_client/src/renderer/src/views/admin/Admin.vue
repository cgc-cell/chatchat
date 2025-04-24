<template>
  <div class="admin-window">
    <div class="title drag">管理员</div>
    <div class="body-content">
      <div class="left-side">
        <div
          v-for="item in menuList"
          :key="item.path"
          :class="['menu-item', item.path == route.path ? 'active' : '']"
          @click="menuJump(item)"
        >
          <div :class="['iconfont', item.icon]" :style="{ background: item.iconBgColor }"></div>
          <div class="text">{{ item.name }}</div>
        </div>
      </div>
      <div class="right-side">
        <router-view></router-view>
      </div>
    </div>
  </div>
  <WinOp :show-max="false" :show-set-top="false"></WinOp>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import WinOp from '../../components/WinOp.vue'
import { useGlobalInfoStore } from '../../store/GlobalInfoStore'
const globalInfoStore = useGlobalInfoStore()
const router = useRouter()
const route = useRoute()
const menuList = ref([
  {
    name: '用户管理',
    icon: 'icon-user',
    path: '/admin/userList',
    iconBgColor: '#fa9d3b'
  },
  {
    name: '靓号管理',
    icon: 'icon-beauty-beauty',
    path: '/admin/beautyAccount',
    iconBgColor: '#fe903b'
  },
  {
    name: '群组管理',
    icon: 'icon-group',
    path: '/admin/groupList',
    iconBgColor: '#1485ee'
  },
  {
    name: '系统设置',
    icon: 'icon-setting',
    path: '/admin/sysSetting',
    iconBgColor: '#fa5151'
  },
  {
    name: '版本管理',
    icon: 'icon-refresh',
    path: '/admin/update',
    iconBgColor: '#07c160'
  }
])

const menuJump = (item) => {
  router.push(item.path)
}
onMounted(() => {
  window.ipcRenderer.on('pageInitData', (e, data) => {
    localStorage.setItem('token', data.token)
    globalInfoStore.setGlobalInfo('serverPort', data.localServerPort)
  })
})
onUnmounted(() => {
  window.ipcRenerer.removeAllListeners('pageInitData')
})
</script>

<style lang="scss" scoped>
.admin-window {
  padding: 0;
  border: 1px solid #ddd;
  background: #fff;
  position: relative;
  overflow: hidden;
  .title {
    height: 40px;
    line-height: 40px;
    border-bottom: 1px solid #ddd;
    text-align: center;
    font-weight: bold;
  }
  .body-content {
    height: calc(100vh - 42px);
    display: flex;
    .left-side {
      width: 200px;
      border-right: 1px solid #ddd;
      background: #e6e6e5;
      .menu-item {
        display: flex;
        align-items: center;
        position: relative;
        padding: 10px;
        &:hover {
          cursor: pointer;
          background: #d6d6d7;
        }
        .iconfont {
          width: 35px;
          height: 35px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          font-size: 20px;
        }
        .text {
          flex: 1;
          color: #000000;
          margin-left: 10px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      .active {
        background: #c4c4c4;
        &:hover {
          background: #c4c4c4;
        }
      }
    }
    .right-side {
      flex: 1;
      padding: 10px;
    }
  }
}
</style>
