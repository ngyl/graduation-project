<script setup lang="ts">
// App component logic
</script>

<template>
  <el-config-provider>
    <div class="app-container">
      <router-view />
    </div>
  </el-config-provider>
</template>

<style>
.app-container {
  width: 100%;
  min-height: 100vh;
}
</style>
