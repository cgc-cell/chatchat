<template>
  <div
    class="content-panel"
    :style="{ 'border-top': props.showTopBorder ? '1px solid #ddd' : ' ' }"
  >
    <div class="content-inner">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  showTopBorder: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
.content-panel {
  overflow: auto;
  height: calc(100vh - 62px);
  .content-inner {
    width: 400px;
    margin: 0 auto;
  }
}
</style>
