<template>
  <div>
    <ShowLocalImage
      :file-id="data.messageId"
      part-type="chat"
      :file-type="data.fileType"
      :show-play="true"
    ></ShowLocalImage>
  </div>
</template>

<script setup>
import ShowLocalImage from '../../components/ShowLocalImage.vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})
</script>

<style lang="scss" scoped></style>
