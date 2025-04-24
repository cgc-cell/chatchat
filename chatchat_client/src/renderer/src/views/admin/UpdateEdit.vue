<template>
  <Dailog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="500px"
    @close="dialogConfig.show = false"
  >
    <el-form ref="formDataRef" :model="formData" label-width="100px" :rules="rules">
      <!--input输入-->
      <el-form-item prop="version" label="版本号">
        <el-input v-model="formData.version" placeholder="eg:1.0.0" :max-length="10"></el-input>
      </el-form-item>
      <el-form-item prop="fileType" label="文件类型">
        <el-radio-group v-model="formData.fileType">
          <el-radio :label="0">本地文件</el-radio>
          <el-radio :label="1">外链</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="formData.fileType == 0" label="文件" prop="fileName" class="file-select"
        ><div class="file-name">{{ formData.fileName }}</div>
        <el-upload
          name="file"
          :show-file-list="false"
          accept=".exe"
          :multiple="false"
          :http-request="selectFile"
        >
          <el-button type="primary" size="small">选择文件</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item v-if="formData.fileType == 1" label="外链地址" prop="outerLink">
        <el-input
          v-model="formData.outerLink"
          placeholder="请输入完整的外链地址"
          :max-length="200"
        ></el-input>
      </el-form-item>
      <el-form-item label="更新内容" class="update-form-item">
        <div v-for="(item, index) in formData.updateDescList" :key="index" class="update-desc-item">
          <el-form-item
            :prop="'updateDescList.' + index + '.title'"
            :rules="{ required: true, message: '更新内容不能为空' }"
          >
            <div class="update-desc">
              <div class="num">{{ index + 1 }}</div>
              <div class="input">
                <el-input
                  v-model="item.title"
                  placeholder="请输入更新内容"
                  :max-length="11"
                ></el-input>
              </div>
              <div v-if="index == 0" class="iconfont icon-add" @click="addLine"></div>
              <div v-else class="iconfont icon-min" @click="delLie(index)"></div>
            </div>
          </el-form-item>
        </div>
      </el-form-item>
    </el-form>
  </Dailog>
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

const formData = ref({
  updateDescList: []
})
const formDataRef = ref()
const rules = {
  version: [
    { required: true, message: '请输入版本号' },
    { validator: proxy.Verify.version, message: '版本号只能是数字和点' }
  ],
  fileType: [{ required: true, message: '请选择文件类型' }],
  fileName: [{ required: true, message: '请选择更新文件' }],
  outerLink: [{ required: true, message: '请选择外链地址' }],
  updateType: [{ required: true, message: '请选择更新内容' }]
}
const selectFile = (file) => {
  file = file.file
  formData.value.file = file
  formData.value.fileName = file.name
}

const addLine = () => {
  formData.value.updateDescList.push({ title: '' })
}
const delLie = (index) => {
  formData.value.updateDescList.splice(index, 1)
}
const emit = defineEmits(['reload'])
const submitForm = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value)
    const updateDescArray = params.updateDescList.map((item) => item.title)
    params.updateDesc = updateDescArray.join('|')
    delete params.updateDescList
    let result = await proxy.Request({
      url: proxy.api.saveUpdate,
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
    if (data) {
      data.updateDescList = data.updateDescArray.map((item) => ({ title: item }))
      data.fileName = 'ChatChat.' + data.version + '.exe'
    }
    formData.value = Object.assign({}, data || { updateDescList: [{ title: '' }] })
  })
}

defineExpose({
  showEdit
})
</script>

<style lang="scss" scoped>
.file-select {
  display: flex;
  .file-name {
    color: #409eff;
    margin-right: 10px;
  }
}
.update-form-item {
  margin-bottom: 0;
  .update-desc-item {
    width: 100%;
    margin-bottom: 15px;
    .update-desc {
      width: 100%;
      display: flex;
      .num {
        width: 15px;
        margin-right: 2px;
      }
      .input {
        flex: 1;
      }
      .iconfont {
        cursor: pointer;
        margin-left: 10px;
        text-align: right;
      }
    }
  }
}
</style>
