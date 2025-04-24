<template>
  <div class="search-item">
    <Avatar :user-id="data.groupId || data.contactId" :show-detail="false"></Avatar>
    <div class="contact-info">
      <div class="contact-name" v-html="data.searchContactName"></div>
    </div>
  </div>
</template>

<script setup>
import Avatar from '../../components/Avatar.vue'

const props = defineProps({
  data: Object
})
</script>

<style lang="scss" scoped>
.search-item {
  padding: 10px;
  display: flex;
  align-items: center;
  .contact-info {
    margin-left: 10px;
    :deep(.highlight) {
      color: red;
    }
    .last-message {
      color: #999999;
    }
  }
  &:hover {
    background: #ededed;
  }
}
</style>
