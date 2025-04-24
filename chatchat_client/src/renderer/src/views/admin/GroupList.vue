<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="80px" label-position="right">
        <el-row>
          <el-col :span="5">
            <el-form-item label="群组Id" label-width="55  px">
              <el-input
                v-model="searchForm.userId"
                class="password-input"
                clearable
                @keyup.native="loadDataList"
              ></el-input> </el-form-item
          ></el-col>
          <el-col :span="5">
            <el-form-item label="群名称">
              <el-input
                v-model="searchForm.groupNameFuzzy"
                placeholder="支持模糊搜索"
                class="password-input"
                clearable
                @keyup="loadDataList"
              ></el-input> </el-form-item
          ></el-col>
          <el-col :span="5">
            <el-form-item label="群主ID">
              <el-input
                v-model="searchForm.groupOwnerId"
                class="password-input"
                clearable
                @keyup="loadDataList"
              ></el-input> </el-form-item
          ></el-col>
          <el-col :span="5" :style="{ paddingLeft: '20px' }">
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
      <template #slotGroupName="{ row }"> {{ row.groupName }}({{ row.groupId }}) </template>
      <template #slotGroupOwnerNickName="{ row }">
        {{ row.groupOwnerNickName }}({{ row.groupOwnerId }})
      </template>
      <template #slotJoinType="{ row }">
        <div>{{ row.joinType == 0 ? '直接加入' : '管理员同意后加入' }}</div>
      </template>
      <template #slotStatus="{ row }">
        <span v-if="row.status == 0" :style="{ color: 'red' }">已解散</span>
        <span v-else :style="{ color: 'green' }">正常</span>
      </template>
      <template #slotOperation="{ row }">
        <div class="row-op-panel">
          <a v-if="row.status == 1" href="javascript:void(0)" @click="dissolutionGroup(row)"
            >解散</a
          >
        </div>
      </template>
    </Table>
  </el-card>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()
import Table from '../../components/Table.vue'
import AvatarBase from '../../components/AvatarBase.vue'
const tableData = ref({})
const tableOptions = {}
const columns = [
  {
    label: '头像',
    prop: 'gtoupId',
    width: 70,
    scopedSlots: 'slotAvatar'
  },
  {
    label: '群名称',
    prop: 'groupName',
    scopedSlots: 'slotGroupName'
  },
  {
    label: '群主',
    prop: 'groupOwnerNickName',
    scopedSlots: 'slotGroupOwnerNickName'
  },
  {
    label: '群员',
    prop: 'memberCount',
    width: 200
  },
  {
    label: '创建时间',
    prop: 'createTime',
    width: 200
  },
  {
    label: '加入方式',
    prop: 'joinType',
    width: 150,
    scopedSlots: 'slotJoinType'
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

const searchForm = ref({})
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize
  }
  Object.assign(params, searchForm.value)
  let result = await proxy.Request({
    url: proxy.api.loadGroup,
    params
  })
  if (!result) {
    return
  }
  Object.assign(tableData.value, result)
}
const dissolutionGroup = (data) => {
  proxy.confirm({
    message: `确定要解散【${data.groupName}】 吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.api.adminDissolutionGroup,
        params: { groupId: data.groupId }
      })
      if (!result) {
        return
      }
      proxy.message.success('解散成功')
      loadDataList()
    }
  })
}
</script>

<style lang="scss" scoped></style>
