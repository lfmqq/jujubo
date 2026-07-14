<template>
  <div></div>
</template>
<script setup>
import { onBeforeMount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()
onBeforeMount(() => {
  const { params, query, name } = route
  // 404 兜底：直接跳回首页
  if (name === 'NotFound') {
    router.replace({ path: '/', query })
    return
  }
  // /redirect/:path 中转
  const rawPath = params.pathMatch || params.path
  const path = Array.isArray(rawPath) ? rawPath.join('/') : rawPath
  router.replace({ path: '/' + (path || ''), query })
})
</script>
