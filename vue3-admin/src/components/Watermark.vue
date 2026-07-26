<template>
  <div
    v-if="visible"
    class="watermark-wrap"
    :style="{ backgroundImage: `url(${imageUrl})` }"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  fontSize: {
    type: Number,
    default: 14
  },
  color: {
    type: String,
    default: 'rgba(0, 0, 0, 0.08)'
  },
  rotate: {
    type: Number,
    default: -30
  },
  gapX: {
    type: Number,
    default: 180
  },
  gapY: {
    type: Number,
    default: 140
  }
})

const visible = computed(() => !!props.text?.trim())

function createCanvas() {
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')

  const font = `${props.fontSize}px sans-serif`
  ctx.font = font
  const { width: textWidth } = ctx.measureText(props.text)

  const boxWidth = textWidth + 40
  const boxHeight = props.fontSize * 2 + 40

  canvas.width = boxWidth
  canvas.height = boxHeight

  ctx.translate(boxWidth / 2, boxHeight / 2)
  ctx.rotate((props.rotate * Math.PI) / 180)
  ctx.font = font
  ctx.fillStyle = props.color
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(props.text, 0, 0)

  return canvas.toDataURL('image/png')
}

const imageUrl = computed(() => createCanvas())
</script>

<style scoped>
.watermark-wrap {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 9999;
  background-repeat: repeat;
  background-position: 0 0;
}
</style>
