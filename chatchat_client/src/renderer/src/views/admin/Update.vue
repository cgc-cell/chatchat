<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="70px" label-position="right">
        <el-row>
          <el-col :span="5">
            <el-form-item label="发布日期" label-width="70px">
              <el-date-picker
                v-model="searchFormData.createTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                @change="loadDataList"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="5" :style="{ paddingLeft: '20px' }">
            <el-button type="success" @click="loadDataList()">查询</el-button>
            <el-button type="primary" @click="showEdit()">发布版本</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
  <div>
    <el-card class="table-data-card">
      <Table
        :columns="columns"
        :fetch="loadDataList"
        :data-source="tableData"
        :options="tableOptions"
      >
        <template #slotUpdateDesc="{ row }">
          <div v-for="(item, num) in row.updateDescArray" :key="item">
            {{ num + 1 }}、{{ item }}
          </div>
        </template>
        <template #fileTypeSlot="{ row }">
          <div v-if="row.fileType == 0">本地文件</div>
          <div v-else>{{ row.outerLink }}</div>
        </template>
        <template #slotStatus="{ row }">
          <span v-if="row.status == 0" :style="{ color: '#f56c6c' }">未发布</span>
          <span v-if="row.status == 1" :style="{ color: '#f7ba2a' }">灰度发布</span>
          <span v-if="row.status == 2" :style="{ color: '#529b2e ' }">全网发布</span>
        </template>
        <template #slotOperation="{ row }">
          <el-dropdown placement="bottom-end" trigger="click">
            <span class="iconfont icon-more"></span>
            <template #dropdown>
              <el-dropdown-item v-if="row.status == 0" @click="showEdit(row)">
                修改
              </el-dropdown-item>
              <el-dropdown-item v-if="row.status == 0" @click="updatePost(row)">
                发布
              </el-dropdown-item>
              <el-dropdown-item v-if="row.status == 0" @click="del(row)"> 删除 </el-dropdown-item>
            </template>
          </el-dropdown>
        </template>
      </Table>
    </el-card>
    <UpdateEdit ref="updateEditRef" @reload="loadDataList"></UpdateEdit>
    <UpdatePost ref="updatePostRef" @reload="loadDataList"></UpdatePost>
  </div>
</template>

<script setup>
import UpdatePost from './UpdatePost.vue'
import { ref, getCurrentInstance } from 'vue'
import Table from '../../components/Table.vue'
import UpdateEdit from './UpdateEdit.vue'
const { proxy } = getCurrentInstance()
const tableData = ref({})
const tableOptions = {}
const searchForm = ref({})
const columns = [
  {
    label: '版本',
    prop: 'version',
    width: 120
  },
  {
    label: '更新内容',
    prop: 'updateDesc',
    scopedSlots: 'slotupdateDesc'
  },
  {
    label: '发布时间',
    prop: 'createTime',
    width: 180
  },
  {
    label: '文件类型',
    prop: 'fileType',
    scopedSlots: 'fileTypeSlot'
  },
  {
    label: '状态',
    prop: 'status',
    width: 150,
    scopedSlots: 'slotStatus'
  },
  {
    label: '操作',
    prop: 'operation',
    width: 80,
    scopedSlots: 'slotOperation'
  }
]
let searchFormData = ref({})
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize
  }
  if (searchFormData.value.createTimeRange) {
    params.createTimeStart = searchFormData.value.createTimeRange[0]
    params.createTimeEnd = searchFormData.value.createTimeRange[1]
  }
  delete params.createTimeRange
  Object.assign(params, searchForm.value)
  let result = await proxy.Request({
    url: proxy.api.loadUpdateDataList,
    params
  })
  if (!result) {
    return
  }
  tableData.value = result
}

const updateEditRef = ref(null)
const showEdit = (row) => {
  updateEditRef.value.showEdit(row)
}

const updatePostRef = ref(null)
const updatePost = (row) => {
  updatePostRef.value.showEdit(row)
}

const del = (data) => {
  proxy.confirm({
    message: `确定删除${data.version}吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.delUpdate,
        params: { id: data.id }
      })
      if (!result) {
        return
      }
      proxy.message.success('删除成功')
      loadDataList()
    }
  })
}
</script>

<style lang="scss" scoped></style>
