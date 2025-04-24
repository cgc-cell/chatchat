<template>
  <ContentPanel>
    <div class="search-form">
      <el-input
        v-model="contactId"
        clearable
        placeholder="请输入用户ID或群组ID"
        size="large"
        @keydown.enter="search"
      ></el-input>
      <div class="search-btn iconfont icon-search" @click="search"></div>
    </div>
    <div v-if="searchResult && Object.keys(searchResult).length > 0" class="search-result-panel">
      <div class="search-result">
        <span class="content-type">{{ contactTypeName }}</span>
        <UserBaseInfo
          :user-info="searchResult"
          :show-area="searchResult.contactType == 'USER'"
        ></UserBaseInfo>
      </div>
      <div v-if="searchResult.contactId != userInfoStore.getUserInfo().userId" class="on-btn">
        <el-button
          v-if="
            searchResult.status == null ||
            searchResult.status == '0' ||
            searchResult.status == '2' ||
            searchResult.status == '3' ||
            searchResult.status == '4'
          "
          type="primary"
          @click="applyContact"
          >{{ searchResult.contactType == 'USER' ? '添加联系人' : '申请加入群聊' }}</el-button
        >
        <el-button v-if="searchResult.status == '1'" type="primary">发消息</el-button>
        <span v-if="searchResult.status == '5' || searchResult.status == '6'"></span>
      </div>
    </div>
    <div v-if="!searchResult" class="no-data">没有搜索到联系人</div>
  </ContentPanel>
  <SearchAdd ref="searchAddRef" @reload="resetForm"></SearchAdd>
</template>

<script setup>
import { ref, getCurrentInstance, computed } from 'vue'
import ContentPanel from '../../components/ContentPanel.vue'
import { useUserInfoStore } from '@/store/userInfoStore.js'
import UserBaseInfo from '../../components/UserBaseInfo.vue'
import SearchAdd from './SearchAdd.vue'
const userInfoStore = useUserInfoStore()

const contactId = ref('')
const { proxy } = getCurrentInstance()
const searchResult = ref({})
//按下搜索后的操作
const search = async () => {
  if (!contactId.value) {
    proxy.message.warning('请输入用户ID或群组ID')
    return
  }
  let result = await proxy.Request({
    url: proxy.api.search,
    params: {
      contactId: contactId.value
    }
  })
  if (!result) {
    return
  }
  searchResult.value = result
}
//联系人类型
const contactTypeName = computed(() => {
  if (userInfoStore.getUserInfo().userId === searchResult.value.contactId) {
    return '自己'
  }
  if (searchResult.value.contactType == 'USER') {
    return '用户'
  }
  if (searchResult.value.contactType == 'GROUP') {
    return '群组'
  }
  return '没有搜索到人'
})
//添加联系人的气泡框
const searchAddRef = ref()
const applyContact = () => {
  searchAddRef.value.show(searchResult.value)
}
//搜索后表单制空
const resetForm = () => {
  searchResult.value = {}
  contactId.value = ''
}
</script>

<style lang="scss" scoped>
.search-form {
  padding-top: 50px;
  display: flex;
  align-items: center;
  :deep(.el-input__wrapper) {
    border-radius: 4px 0 0 4px;
    border-right: none;
  }
  .search-btn {
    background: #07c160;
    color: #fff;
    line-height: 40px;
    width: 80px;
    text-align: center;
    border-radius: 0 5px 5px 0;
    cursor: pointer;
    &:hover {
      background: #0dd36c;
    }
  }
}
.no-data {
  padding: 30px 0;
}
.search-result-panel {
  .search-result {
    padding: 30px 20px 20px 20px;
    background: #fff;
    border-radius: 5px;
    margin-top: 10px;
    position: relative;
    .content-type {
      position: absolute;
      left: 0;
      top: 0;
      padding: 2px 5px;
      background: #2cb6fe;
      color: #fff;
      border-radius: 5px 0 0 0;
      font-size: 12px;
    }
  }
  .on-btn {
    border-radius: 5px;
    margin-top: 10px;
    padding: 10px;
    text-align: center;
    background: #fff;
  }
}
</style>
