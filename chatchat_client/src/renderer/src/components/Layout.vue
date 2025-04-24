<template>
  <div class="layout-container">
    <div class="left-sider-inner">
      <slot name="left-content"></slot>
    </div>
    <div class="right-sider-inner">
      <slot name="right-content"></slot>
    </div>
  </div>
</template>

<script setup></script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  .left-sider-inner {
    width: 250px;
    background: #e6e5e5;
    border-color: #ddd;
    border-style: solid;
    border-width: 0 1px 0 0;
  }
  .right-sider-inner {
    flex: 1;
    height: calc(100vh - 2px);
    background: #f5f5f5;
    width: 100%;
  }
}
</style>
