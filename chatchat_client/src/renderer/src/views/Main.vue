<template>
  <div class="main">
    <div class="left-sider">
      <!-- 放置头像 -->
      <div>
        <Avatar :user-id="userInfoStore.getUserInfo().userId" :width="35" :show-detail="false" />
      </div>
      <!-- 放置菜单上 -->
      <div class="menu-list">
        <!-- 在 Vue 中，:class 是一种动态绑定 class 的方式，它允许根据数据动态添加、删除或修改元素的 class。 -->
        <template v-for="item in menuList" :key="item.name">
          <div
            v-if="item.position == 'top'"
            :class="['tab-item iconfont', item.icon, currentType.name == item.name ? 'active' : '']"
            @click="changeType(item)"
          >
            <!-- 聊天消息提示区域 -->
            <template v-if="item.name == 'chat' || item.name == 'contact'">
              <Badge :count="messageCountStore.getCount(item.countKey)" :top="3" :left="15"></Badge>
            </template>
          </div>
        </template>
      </div>
      <!-- 放置菜单下 -->
      <div class="menu-list menu-bottom">
        <template v-for="item in menuList" :key="item.name">
          <div
            v-if="item.position == 'bottom'"
            :class="['tab-item iconfont', item.icon, currentType.name == item.name ? 'active' : '']"
            @click="changeType(item)"
          ></div>
        </template>
      </div>
    </div>
    <div class="right-container">
      <!-- { Component } 是解构赋值，表示 router-view 提供的 Component 对象。这个对象是当前匹配路由的组件。 -->
      <router-view v-slot="{ Component }">
        <!-- keep-alive 是 Vue 提供的一个内置组件，可以使被包含的组件保留状态，或避免重新渲染。 -->
        <keep-alive include="Chat">
          <component :is="Component" ref="componentRef" />
        </keep-alive>
      </router-view>
    </div>
  </div>
  <Update></Update>
</template>

<script setup>
//基础数据
import { onMounted, ref, getCurrentInstance, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserInfoStore } from '../store/userInfoStore'
import { useGlobalInfoStore } from '../store/GlobalInfoStore'
import Avatar from '../components/Avatar.vue'
import { useSysSettingStore } from '../store/SysSettingStore'
import { useMessageCountStore } from '../store/MessageCountStore'
import Update from './Update.vue'
const messageCountStore = useMessageCountStore()
const sysSettingStore = useSysSettingStore()
const { proxy } = getCurrentInstance()
const globalInfoStore = useGlobalInfoStore()
const userInfoStore = useUserInfoStore()
const router = useRouter()
const route = useRoute()
const menuList = ref([
  {
    name: 'Chat',
    icon: 'icon-chat',
    path: '/chat',
    countKey: 'chatCount',
    position: 'top'
  },
  {
    name: 'contact',
    icon: 'icon-user',
    path: '/contact',
    countKey: 'contactApplyCount',
    position: 'top'
  },
  {
    name: 'mysetting',
    icon: 'icon-more2',
    path: '/setting',
    position: 'bottom'
  }
])

const getLoginInfo = () => {
  window.ipcRenderer.send('getLocalStore', userInfoStore.getUserInfo().userId + 'localServerPort')
}
//切换菜单函数
const currentType = ref(menuList.value[0])
const changeType = (item) => {
  currentType.value = item
  router.push(item.path)
}

//保存后台所需要的设置限制
const getSysSetting = async () => {
  let result = await proxy.Request({
    url: proxy.api.getSysSetting
  })
  if (!result) {
    return
  }
  // console.log('result', result)
  sysSettingStore.setSetting(result)
}

const menuSelect = (path) => {
  currentType.value = menuList.value.find((item) => path.includes(item.path))
}

watch(
  () => route.path,
  (newVale) => {
    if (newVale) {
      console.log('newVale', newVale)

      menuSelect(newVale)
    }
  }
)
onMounted(() => {
  getSysSetting()
  getLoginInfo()
  window.ipcRenderer.on('getLocalStoreCallback', (event, serverPort) => {
    // console.log('serverPort', serverPort)
    globalInfoStore.setGlobalInfo('serverPort', serverPort)
  })
  window.ipcRenderer.on('reLoginCallback', () => {
    router.push('/login')
  })
})
</script>

<style lang="scss" scoped>
.main {
  background-color: #ddd;
  display: flex;
  border-radius: 0 3px 3px 0;
  overflow: hidden;
  .left-sider {
    width: 55px;
    background: #2e2e2e;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 35px;
    border: 1px solid #2e2e2e;
    border-right: none;
    padding-bottom: 10px;
    .menu-list {
      width: 100%;
      flex: 1;
      .tab-item {
        color: #d3d3d3;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: 10px;
        cursor: pointer;
        position: relative;
        font-size: 22px;
      }
      .active {
        color: #07c160;
      }
    }
    //加入就能到达底下
    .menu-bottom {
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
    }
  }
  .right-container {
    flex: 1;
    border: 1 solid #ddd;
    border-left: none;
    overflow: hidden;
  }
}
</style>
