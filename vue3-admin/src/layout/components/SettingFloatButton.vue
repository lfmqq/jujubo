<template>
  <div class="setting-float-btn" @click="emit('click')">
    <el-icon :size="20"><Setting /></el-icon>
  </div>
</template>

<script setup>
import { Setting } from '@element-plus/icons-vue'

const emit = defineEmits(['click'])
</script>

<style scoped>
.setting-float-btn {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
  transition: transform 0.3s, background 0.3s;
}

.setting-float-btn:hover {
  transform: rotate(90deg) scale(1.05);
  filter: brightness(1.1);
}

</style>
