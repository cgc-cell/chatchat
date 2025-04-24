<template>
  <div v-if="count > 0" class="badge" :style="{ top: top + 'px', left: left + 'px' }">
    {{ count > 99 ? '99+' : count }}
  </div>
</template>

<script setup>
const props = defineProps({
  count: {
    type: Number,
    default: 0
  },
  top: {
    type: Number,
    default: 0
  },
  left: {
    type: Number,
    default: 0
  }
})
</script>

<style lang="scss" scoped>
.badge {
  position: absolute;
  font-size: 12px;
  padding: 1px 6px;
  background: #fa5151;
  color: #fff;
  z-index: 1;
  zoom: 0.85;
  border-radius: 50%;
}
</style>
