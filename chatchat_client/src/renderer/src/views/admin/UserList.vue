<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="70px" label-position="right">
        <el-row>
          <el-col :span="7">
            <el-form-item label="UID" label-width="40px">
              <el-input
                v-model="searchForm.userId"
                class="password-input"
                clearable
                @keyup.native="loadDataList"
              ></el-input> </el-form-item
          ></el-col>
          <el-col :span="9">
            <el-form-item label="昵称">
              <el-input
                v-model="searchForm.nickNameFuzzy"
                placeholder="支持模糊搜索"
                class="password-input"
                clearable
                @keyup="loadDataList"
              ></el-input> </el-form-item
          ></el-col>
          <el-col :span="4" :style="{ paddingLeft: '20px' }">
            <el-button type="success" @click="loadDataList()">查询</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
  <el-card class="table-data-card">
    <Table
      :columns="columns"
      :fetch="loadDataList"
      :data-source="tableData"
      :options="tableOptions"
    >
      <template #slotAvatar="{ row }">
        <AvatarBase :width="50" :user-id="row.userId" part-type="avatar"></AvatarBase>
      </template>
      <template #slotNickName="{ row }">
        {{ row.nickName }}
        {{ row.userId }}
        <span v-if="row.sex == 0" class="iconfont icon-woman"></span>
        <span v-if="row.sex == 1" class="iconfont icon-man"></span>
      </template>
      <template #slotStatus="{ row }">
        <span v-if="row.status == 0" :style="{ color: 'red' }">禁用</span>
        <span v-else :style="{ color: 'green' }">启用</span>
      </template>
      <template #slotOnline="{ row }">
        <span v-if="row.onlineType == 1" :style="{ color: 'green' }">在线</span>
        <span v-else :style="{ color: '#8a8a8a' }">离线</span>
      </template>
      <template #slotOperation="{ row }">
        <el-dropdown v-if="userInfo.userId != row.userId" placement="bottom-end" trigger="click">
          <span class="iconfont icon-more"></span>
          <template #dropdown>
            <el-dropdown-item @click="changeAccountStatus(row)"
              >{{ row.status == 0 ? '启用' : '禁用' }}
            </el-dropdown-item>
            <el-dropdown-item v-if="row.onlineType == 1" @click="forceOffLine(row)"
              >强制下线
            </el-dropdown-item>
          </template>
        </el-dropdown>
        <div v-else>管理员</div>
      </template>
    </Table>
  </el-card>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted } from 'vue'
import Table from '../../components/Table.vue'
import AvatarBase from '../../components/AvatarBase.vue'
import { useGlobalInfoStore } from '../../store/GlobalInfoStore'
const globalInfoStore = useGlobalInfoStore()
const { proxy } = getCurrentInstance()
const tableData = ref({})
const tableOptions = {}
const columns = [
  {
    label: '头像',
    prop: 'userId',
    width: 70,
    scopedSlots: 'slotAvatar'
  },
  {
    label: '昵称',
    prop: 'nickName',
    scopedSlots: 'slotNickName'
  },
  {
    label: '邮箱',
    prop: 'email',
    width: 200
  },
  {
    label: '加入时间',
    prop: 'createTime',
    width: 200
  },
  {
    label: '地区',
    prop: 'areaName',
    width: 150
  },
  {
    label: '用户状态',
    prop: 'status',
    width: 100,
    scopedSlots: 'slotStatus'
  },
  {
    label: '在线状态',
    prop: 'onlineType',
    width: 100,
    scopedSlots: 'slotOnline'
  },
  {
    label: '操作',
    prop: 'operation',
    width: 100,
    scopedSlots: 'slotOperation'
  }
]
const searchForm = ref({})
const loadDataList = async () => {
  console.log('globalInfoStore', globalInfoStore.getGlobalInfo('localServerPort'))
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize
  }
  Object.assign(params, searchForm.value)
  let result = await proxy.Request({
    url: proxy.api.loadAdminAccount,
    params
  })
  if (!result) {
    return
  }
  Object.assign(tableData.value, result)
}
const changeAccountStatus = (data) => {
  let status = data.status == 0 ? 1 : 0
  let info = status == 0 ? '禁用' : '启用'
  proxy.confirm({
    message: `确定要${info}用户${data.nickName}吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.updateUserStatus,
        params: {
          userId: data.userId,
          status
        }
      })
      if (!result) {
        return
      }
      proxy.message.success(`${info}成功`)
      loadDataList()
    }
  })
}
const forceOffLine = (data) => {
  proxy.confirm({
    message: `确定要强制下线用户${data.nickName}吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.forceOffLine,
        params: {
          userId: data.userId
        }
      })
      if (!result) {
        return
      }
      proxy.message.success(`强制下线成功`)
      loadDataList()
    }
  })
}
const userInfo = ref({})
const getLoginInfo = async () => {
  let result = await proxy.Request({
    url: proxy.api.getUserInfo
  })
  if (!result) {
    return
  }
  userInfo.value = result
}
getLoginInfo()
onMounted(() => {})
</script>

<style lang="scss" scoped></style>
