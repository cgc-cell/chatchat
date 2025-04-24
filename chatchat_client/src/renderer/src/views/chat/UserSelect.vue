<template>
  <div>
    <Dailog
      :show="dialogConfig.show"
      :title="dialogConfig.title"
      :buttons="dialogConfig.buttons"
      width="660px"
      @close="dialogConfig.show = false"
    >
      <el-transfer
        v-model="formData.selectedMemberIds"
        :titles="['全部', '已选']"
        :format="{ noChecked: '${total}', hasChecked: '${checked}/${total}' }"
        :data="dataList"
        :props="{ key: 'contactId', label: 'contactName' }"
        filterable
        :filter-method="search"
      >
        <template #default="{ option }">
          <div class="select-item">
            <div class="avatar">
              <AvatarBase
                :user-id="option.contactId"
                :width="30"
                :border-radius="5"
                :show-detail="false"
              ></AvatarBase>
            </div>
            <div class="nick-name">
              {{ option.contactName }}
            </div>
          </div>
        </template>
      </el-transfer>
    </Dailog>
  </div>
</template>

<script setup>
import Dailog from '../../components/Dailog.vue'
import AvatarBase from '../../components/AvatarBase.vue'
import { ref, getCurrentInstance } from 'vue'
const { proxy } = getCurrentInstance()
const dialogConfig = ref({
  show: false,
  title: '选择联系人',
  buttons: [
    {
      type: 'primary',
      text: '确定',
      click: () => {
        submitData()
      }
    }
  ]
})

const search = (query, item) => {
  return item.contactName.toLowerCase().includes(query.toLowerCase())
}

const dataList = ref([])
const formData = ref({
  selectedMemberIds: []
})

const show = ({ contactList, groupId, opType }) => {
  dialogConfig.value.title = opType == 0 ? '移除群员' : '添加群员'
  dialogConfig.value.show = true
  dataList.value = contactList
  formData.value = {
    selectedMemberIds: [],
    groupId,
    opType
  }
}

const emit = defineEmits(['callback'])
const submitData = async () => {
  if (formData.value.selectedMemberIds.length == 0) {
    proxy.message.warning('请选择联系人')
    return
  }
  let params = {}
  Object.assign(params, formData.value)
  params.selectedMemberIds = params.selectedMemberIds.join(',')
  let result = await proxy.Request({
    url: proxy.api.addOrRemoveGroupUser,
    params
  })
  if (!result) {
    console.log('请求失败')
    return
  }
  dialogConfig.value.show = false
  emit('callback')
}

defineExpose({
  show
})
</script>

<style lang="scss" scoped>
.el-transfer {
  height: 100%;
  width: 100%;
  display: block !important;
  display: flex !important;
  :deep(.el-transfer-panel) {
    width: 280px;
  }
  :deep(.el-transfer-panel__item) {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 5px;
  }
}
:deep(.el-transfer__buttons) {
  width: 60px;
  flex-direction: column;
  text-align: center;
  padding: 0;
  margin: 0 auto;
  flex: 1;
  .el-transfer__button {
    text-align: center;
    margin-left: 0;
    margin-right: 0;
    margin-top: 5px;
    padding: 10px;
    height: 36px;
    border-radius: 50%;
    position: relative;
  }
}
.select-item {
  display: flex;
  .avatar {
    width: 30px;
    height: 30px;
  }
  .nick-name {
    flex: 1;
    margin-left: 5px;
  }
}
</style>
