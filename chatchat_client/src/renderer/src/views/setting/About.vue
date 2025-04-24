<template>
  <ContentPanel>
    <el-form ref="formDataRef" label-width="80px" @submit.prevent>
      <el-form-item label="版本信息">
        <div class="version-info">
          <div>Wechat {{ config.version }}</div>
          <div><el-button type="primary" @click="checkUpdate">检查更新</el-button></div>
        </div>
      </el-form-item>
    </el-form>
  </ContentPanel>
  <Update ref="updateRef" :auto-update="false"></Update>
</template>

<script setup>
import ContentPanel from '../../components/ContentPanel.vue'
import config from '../../../../../package.json'
import Update from '../Update.vue'
import { ref } from 'vue'

const updateRef = ref()
const checkUpdate = () => {
  updateRef.value.checkUpdate()
}
</script>

<style lang="scss" scoped></style>
