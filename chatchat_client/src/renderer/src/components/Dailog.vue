<template>
  <div>
    <el-dialog
      :show-close="showClose"
      :draggable="false"
      :model-value="show"
      :close-on-click-modal="false"
      :title="title"
      class="cust-dialog"
      :top="top + 'px'"
      :width="width"
      @close="close"
    >
      <!-- 内容部分 -->
      <div class="dialog-body" :style="{ 'max-height': maxHeight + 'px', padding: padding + 'px' }">
        <slot></slot>
      </div>
      <!-- 按钮部分 -->
      <template v-if="(buttons && buttons.length > 0) || showCancel">
        <div class="dialog-footer">
          <el-button v-if="showCancel" link @click="close"> 取消 </el-button>
          <el-button
            v-for="btn in buttons"
            :key="btn.text"
            :type="btn.type || 'primary'"
            @click="btn.click"
          >
            {{ btn.text }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
const props = defineProps({
  title: {
    type: String
  },
  show: {
    type: Boolean,
    default: false
  },
  showClose: {
    type: Boolean,
    default: true
  },
  showCancel: {
    type: Boolean,
    default: true
  },
  top: {
    type: Number,
    default: 80
  },
  width: {
    type: String,
    default: '30%'
  },
  buttons: {
    type: Array
  },
  padding: {
    type: Number,
    default: 15
  }
})

const maxHeight = window.innerHeight - props.top - 100

const emit = defineEmits(['close'])
const close = () => {
  emit('close')
}
</script>

<style lang="scss">
.cust-dialog {
  .el-dialog__body {
    padding: 0px;
  }
  .dialog-body {
    border-top: 1px solid #ddd;
    border-bottom: 1px solid #ddd;
    min-height: 80px;
    overflow: auto;
    overflow-x: hidden;
  }
  .dialog-footer {
    text-align: right;
    padding: 5px 20px;
  }
}
</style>
