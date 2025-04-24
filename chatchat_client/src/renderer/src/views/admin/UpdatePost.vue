<template>
  <Dailog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="500px"
    @close="dialogConfig.show = false"
  >
    <el-form ref="formDataRef" :model="formData" label-width="100px" :rules="rules" @submit.prevent>
      <!--input输入-->
      <el-form-item label="版本号">
        {{ formData.version }}
      </el-form-item>
      <el-form-item prop="status" label="发布状态">
        <el-radio-group v-model="formData.status">
          <el-radio :label="0">取消发布</el-radio>
          <el-radio :label="1">灰度发布</el-radio>
          <el-radio :label="2">全网发布</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.status == 1" prop="grayscaleUid" label="灰度Uid">
        <el-tag
          v-for="(tag, index) in formData.grayscaleUid"
          :key="tag"
          closeable
          :type="tag.type"
          class="tag-panel"
          @close="closeTag(index)"
        >
          {{ tag }}
        </el-tag>
        <div v-if="showInput" class="tag input">
          <el-input
            v-model="tagInput"
            size="small"
            placeholder="请输入Uid"
            clearable
            @blur="addDeviceId"
            @keyup.enter="addDeviceId"
          ></el-input>
        </div>
        <div v-if="!showInput" class="tag">
          <el-button size="small" type="primary" @click="showInputHandle">添加</el-button>
        </div>
      </el-form-item>
    </el-form></Dailog
  >
</template>

<script setup>
import { ref, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
const dialogConfig = ref({
  show: false,
  title: '发布更新',
  buttons: [
    {
      type: 'primary',
      text: '确定',
      click: () => {
        submitForm()
      }
    }
  ]
})

const formData = ref({})
const formDataRef = ref()
const rules = {
  status: [{ required: true, message: '请选择发布状态' }]
}
const emit = defineEmits(['reload'])
const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value)

    params.grayscaleUid = params.grayscaleUid.join(',')
    delete params.updateDescList
    let result = await proxy.Request({
      url: proxy.api.postUpdate,
      params
    })
    if (!result) {
      return
    }
    dialogConfig.value.show = false
    emit('reload')
  })
}
const showEdit = (data) => {
  dialogConfig.value.show = true
  nextTick(() => {
    formDataRef.value.resetFields()

    formData.value = Object.assign({
      id: data.id,
      version: data.version,
      status: data.status,
      grayscaleUid: data.grayscaleUid ? data.grayscaleUid.split(',') : []
    })
  })
}

defineExpose({
  showEdit
})

const showInput = ref(false)
const tagInput = ref()
const addDeviceId = () => {
  if (tagInput.value) {
    formData.value.grayscaleUid.push(tagInput.value)
  }
  tagInput.value = ''
  showInput.value = false
}
const showInputHandle = () => {
  showInput.value = true
}

const closeTag = (index) => {
  formData.value.grayscaleUid.splice(index, 1)
}
</script>

<style lang="scss" scoped>
.tag-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  .tag {
    margin: 0 5px 5px 0;
  }
  .input {
    width: 150px;
  }
}
</style>
