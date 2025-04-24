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
      <div v-if="!searchKey" class="contact-list">
        <template v-for="item in partList" :key="item.partName">
          <div class="part-title">{{ item.partName }}</div>
          <div class="part-list">
            <div
              v-for="sub in item.children"
              :key="sub.name"
              :class="['part-item', route.path == sub.path ? 'active' : '']"
              @click="partJump(sub)"
            >
              <div :class="['iconfont', sub.icon]" :style="{ background: sub.iconBgColor }"></div>
              <div class="test">{{ sub.name }}</div>
              <Badge :count="messageCountStore.getCount(sub.countKey)" :top="3" :left="45"></Badge>
            </div>
            <template v-for="contact in item.contactData" :key="contact.contactId">
              <div
                :class="[
                  'part-item',
                  contact[item.contactId] == route.query.contactId ? 'active' : ''
                ]"
                @click="contactDetail(contact, item)"
              >
                <Avatar :user-id="contact[item.contactId]" :width="35"></Avatar>
                <div class="test">{{ contact[item.contactName] }}</div>
              </div>
            </template>
            <template v-if="item.contactData && item.contactData.length == 0">
              <div class="no-data">
                {{ item.emptyMsg }}
              </div>
            </template>
          </div>
        </template>
      </div>
      <div v-else>
        <contactSearchResult
          v-for="item in searchList"
          :key="item.contactId || item.groupId"
          :data="item"
          @click="searchClickHandle(item)"
        ></contactSearchResult>
      </div>
    </template>
    <template #right-content>
      <div class="title-panel drag">{{ rightTitle }}</div>
      <router-view v-slot="{ Component }">
        <component :is="Component" ref="componentRef" />
      </router-view>
    </template> </Layout
  ><WinOp></WinOp>
</template>

<script setup>
import contactSearchResult from './contactSearchResult.vue'
import Layout from '../../components/Layout.vue'
import { ref, getCurrentInstance, watch } from 'vue'
import { useRoute } from 'vue-router'
import WinOp from '../../components/WinOp.vue'
import router from '../../router'
import Avatar from '../../components/Avatar.vue'
import { useContactStateStore } from '../../store/contactStateStore'
import Badge from '../../components/Badge.vue'
import { useMessageCountStore } from '../../store/MessageCountStore'
const messageCountStore = useMessageCountStore()
const contactStateStore = useContactStateStore()
const { proxy } = getCurrentInstance()
const route = useRoute()
const partList = ref([
  {
    partName: '新朋友',
    children: [
      {
        name: '搜好友',
        icon: 'icon-search',
        iconBgColor: '#fa9d3b',
        path: '/contact/search'
      },
      {
        name: '新的朋友',
        icon: 'icon-plane',
        iconBgColor: '#08bf61',
        path: '/contact/contactNotice',
        showTitle: true,
        countKey: 'contactApplyCount'
      }
    ]
  },
  {
    partName: '我的群聊',
    children: [
      {
        name: '新建群聊',
        icon: 'icon-add-group',
        iconBgColor: '#1485ee',
        path: '/contact/createGroup'
      }
    ],
    contactId: 'groupId',
    contactName: 'groupName',
    showTitle: true,
    name: '群聊',
    contactData: [],
    contactPath: '/contact/groupDetail'
  },
  {
    partName: '我加入的群聊',
    contactId: 'contactId',
    contactName: 'contactName',
    showTitle: true,
    contactData: [],
    contactPath: '/contact/groupDetail',
    emptyMsg: '暂无群聊'
  },
  {
    partName: '我的好友',
    children: [],
    contactId: 'contactId',
    contactName: 'contactName',
    contactData: [],
    contactPath: '/contact/userDetail',
    emptyMsg: '暂无好友'
  }
])
const rightTitle = ref('')
//跳转路由函数
const partJump = (item) => {
  if (item.showTitle) {
    rightTitle.value = item.name
  } else {
    rightTitle.value = ''
  }
  if (item.countKey) {
    messageCountStore.setCount(item.countKey, 0, true)
    window.ipcRenderer.send('updateContactNoReadCount')
  }
  router.push(item.path)
}
//加载已有联系人
const loadContact = async (contactType) => {
  let result = await proxy.Request({
    url: proxy.api.loadContact,
    params: {
      contactType
    }
  })
  if (!result) {
    return
  }
  if (contactType == 'GROUP') {
    partList.value[2].contactData = result
  } else if (contactType == 'USER') {
    partList.value[3].contactData = result
  }
}
loadContact('GROUP')
loadContact('USER')
//加载现有我创建的群聊
const loadMyGroup = async () => {
  let result = await proxy.Request({
    url: proxy.api.loadMyGroup,
    showLoading: false
  })
  if (!result) {
    return
  }
  partList.value[1].contactData = result
}
loadMyGroup()
//跳转到联系人详情页(右边模块)
const contactDetail = (contact, part) => {
  if (part.showTitle) {
    rightTitle.value = contact[part.contactName]
  }
  router.push({
    path: part.contactPath,
    query: {
      contactId: contact[part.contactId]
    }
  })
}
//主页面所有联系人搜索
const searchKey = ref('')
const searchList = ref([])
const search = () => {
  if (!searchKey.value) {
    return
  }
  searchList.value = []
  const regx = new RegExp('(' + searchKey.value + ')', 'gi')
  let allContactList = ref([])
  partList.value.forEach((item) => {
    if (item.contactData) {
      allContactList.value = allContactList.value.concat(item.contactData)
    }
  })
  allContactList.value.forEach((item) => {
    let contactName = item.groupId ? item.groupName : item.contactName
    if (contactName.includes(searchKey.value)) {
      let newData = Object.assign({}, item)
      console.log('newData', newData.groupId)
      if (newData.groupId) {
        newData.searchContactName = newData.groupName.replace(
          regx,
          '<span class="highlight">$1</span>'
        )
      } else {
        newData.searchContactName = newData.contactName.replace(
          regx,
          '<span class="highlight">$1</span>'
        )
      }
      searchList.value.push(newData)
    }
  })
}

const searchClickHandle = (data) => {
  searchKey.value = ''
  router.push({
    path: '/chat',
    query: {
      chatId: data.groupId || data.contactId,
      timestamp: new Date().getTime()
    }
  })
}
//实时监听是否有新的好友或群组加入
watch(
  () => contactStateStore.contactReload,
  (newVal) => {
    if (!newVal) {
      return
    }
    switch (newVal) {
      case 'MY':
        loadMyGroup()
        break
      case 'USER':
      case 'GROUP':
        loadContact(newVal)
        break
      case 'DISSOLUTION_GROUP':
        loadMyGroup()
        router.push('/contact/blank')
        rightTitle.value = null
        break
      case 'LEAVE_GROUP':
        loadContact('GROUP')
        router.push('/contact/blank')
        rightTitle.value = null
        break
      case 'REMOVE_USER':
        loadContact('USER')
        router.push('/contact/blank')
        rightTitle.value = null
        break
    }
  },
  {
    immediate: true,
    deep: true
  }
)
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
.contact-list {
  height: calc(100vh - 62px);
  border-top: 1px solid #ddd;
  overflow: hidden;
  &:hover {
    overflow: auto; // 鼠标悬浮时显示滚动条
  }
  .part-title {
    color: #515151;
    padding-left: 10px;
    margin-top: 10px;
  }
  .part-list {
    border-bottom: 1px solid #d6d6d6;
    .part-item {
      display: flex;
      align-items: center;
      padding: 10px 10px;
      position: relative;
      &:hover {
        cursor: pointer;
        background: #d6d6d7;
      }
      .iconfont {
        width: 35px;
        height: 35px;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 20px;
        color: #fff;
      }
      .test {
        flex: 1;
        color: #000000;
        margin-left: 10px;
        overflow: hidden;
        text-overflow: ellipsis; // 文字超出部分显示省略号
        white-space: nowrap; // 文字不换行
      }
    }
    .no-data {
      text-align: center;
      font-size: 12px;
      color: #9d9d9d;
      line-height: 30px;
    }
    .active {
      background: #c4c4c4;
      &:hover {
        background: #c4c4c4;
      }
    }
  }
}
.title-panel {
  width: 100%;
  height: 60px;
  display: flex;
  align-items: center;
  padding-left: 10px;
  font-size: 18px;
  color: #000000;
}
</style>
