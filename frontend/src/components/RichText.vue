<template>
  <div class="rich-text-content ql-editor" v-html="sanitizedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  content: string
}>()

function sanitizeHtml(html: string) {
  return html
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, '')
    .replace(/<style[\s\S]*?>[\s\S]*?<\/style>/gi, '')
    .replace(/\son\w+="[^"]*"/gi, '')
    .replace(/\son\w+='[^']*'/gi, '')
    .replace(/javascript:/gi, '')
}

const sanitizedContent = computed(() => {
  if (!props.content) return ''
  let html = sanitizeHtml(props.content)
  // 将纯文本中的换行转为 <br>（如果不含 HTML 标签）
  if (!/<[a-z][\s\S]*>/i.test(html)) {
    html = html.replace(/\n/g, '<br>')
  }
  return html
})
</script>

<style lang="scss" scoped>
.rich-text-content {
  color: var(--text-light);
  line-height: 1.8;

  :deep(img) {
    max-width: 100%;
    border-radius: var(--radius-md);
  }

  :deep(a) {
    color: var(--steam-light-blue);
  }

  :deep(h1), :deep(h2), :deep(h3) {
    color: var(--text-white);
    margin: 16px 0 8px;
  }

  :deep(p) {
    margin-bottom: 8px;
  }

  :deep(ul), :deep(ol) {
    padding-left: 24px;
    margin-bottom: 8px;
  }
}
</style>
