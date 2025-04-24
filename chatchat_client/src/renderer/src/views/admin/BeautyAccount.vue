<template>
  <div>
    <div class="top-panel">
      <el-form :model="searchForm" label-width="70px" label-position="right">
        <el-row>
          <el-col :span="5">
            <el-form-item label="靓号" label-width="40px">
              <el-input
                v-model="searchForm.userId"
                class="password-input"
                clearable
                placeholder="支持模糊搜索"
                @keyup.native="loadDataList"
              ></el-input> </el-form-item
          ></el-col>
          <el-col :span="5">
            <el-form-item label="邮箱">
              <el-input
                v-model="searchForm.emailFuzzy"
                placeholder="支持模糊搜索"
                class="password-input"
                clearable
                @keyup="loadDataList"
              ></el-input> </el-form-item
          ></el-col>
          <el-col :span="4" :style="{ paddingLeft: '15px' }">
            <el-button type="success" @click="loadDataList()">查询</el-button>
            <el-button type="success" @click="editAccount()">新增靓号</el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <Table
      :columns="columns"
      :fetch="loadDataList"
      :data-source="tableData"
      :options="tableOptions"
    >
      <template #slotAvatar="{ row }">
        <Avatar :width="50" :user-id="row.userId" part-type="avatar"></Avatar>
      </template>
      <template #slotNickName="{ row }">
        {{ row.nickName }}

        <span v-if="row.sex == 0" class="iconfont icon-woman"></span>
        <span v-if="row.sex == 1" class="iconfont icon-man"></span>
      </template>
      <template #slotStatus="{ row }">
        <span v-if="row.status == 0" :style="{ color: 'red' }">未使用</span>
        <span v-else :style="{ color: 'green' }">已使用</span>
      </template>
      <template #slotOnline="{ row }">
        <span v-if="row.onlineType == 1" :style="{ color: 'green' }">在线</span>
        <span v-else :style="{ color: '#8a8a8a' }">离线</span>
      </template>
      <template #slotOperation="{ row }">
        <el-dropdown v-if="userInfo.userId != row.userId" placement="bottom-end" trigger="click">
          <span class="iconfont icon-more"></span>
          <template #dropdown>
            <el-dropdown-item @click="editAccount(row)">修改 </el-dropdown-item>
            <el-dropdown-item v-if="row.onlineType == 1" @click="delAccount(row)"
              >删除
            </el-dropdown-item>
          </template>
        </el-dropdown>
      </template>
    </Table>
  </div>
  <BeautyAccountEdit ref="beautyAccountEditRef" @reload="loadDataList"></BeautyAccountEdit>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import Table from '../../components/Table.vue'
import BeautyAccountEdit from './BeautyAccountEdit.vue'
import Avatar from '../../components/Avatar.vue'

const tableOptions = {}
const { proxy } = getCurrentInstance()
const columns = [
  {
    label: '邮箱',
    prop: 'email'
  },
  {
    label: '靓号',
    prop: 'userId'
  },
  {
    label: '状态',
    prop: 'status',
    scopedSlots: 'slotStatus'
  },
  {
    label: '操作',
    prop: 'operation',
    scopedSlots: 'slotOperation'
  }
]
const searchForm = ref({})
const tableData = ref({})
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize
  }
  Object.assign(params, searchForm.value)
  let result = await proxy.Request({
    url: proxy.api.loadBeautyAccount,
    params
  })
  if (!result) {
    return
  }
  Object.assign(tableData.value, result)
}

const beautyAccountEditRef = ref(null)
const editAccount = (row) => {
  beautyAccountEditRef.value.showEdit(row)
}
</script>

<style lang="scss" scoped></style>
