<template>
  <Dailog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="400px"
    :show-cancel="false"
    @close="dialogConfig.show = false"
  >
    <GroupEditForm ref="groupEditFormRef" @edit-back="editBack"></GroupEditForm>
  </Dailog>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import GroupEditForm from './GroupEditForm.vue'
const dialogConfig = ref({
  show: false,
  title: '修改群组',
  buttons: []
})
const groupEditFormRef = ref()
const emit = defineEmits(['reloadGroupInfo'])
const show = (data) => {
  dialogConfig.value.show = true
  nextTick(() => {
    groupEditFormRef.value.show(data)
  })
}
const editBack = () => {
  dialogConfig.value.show = false
  emit('reloadGroupInfo')
}

defineExpose({
  show
})
</script>

<style lang="scss" scoped></style>
