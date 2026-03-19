<script setup lang="ts">
import type { TryOnTaskVO } from '@/types/tryon'

defineProps<{
  task?: TryOnTaskVO | null
  history?: TryOnTaskVO[]
  generating?: boolean
}>()
</script>

<template>
  <section class="result-card">
    <div class="result-card__head">
      <div>
        <h3>试穿结果</h3>
        <p>生成完成后可继续换款重生成，历史结果会保留在下方方便展示。</p>
      </div>
    </div>

    <div v-if="!task" class="result-card__empty">
      上传照片并选择至少一件支持试穿的商品后，即可生成试穿图。
    </div>
    <div v-else class="result-card__body">
      <div class="result-card__status">
        <span>任务状态：{{ task.status }}</span>
        <span v-if="generating">AI 生成中，请稍候...</span>
      </div>

      <div v-if="task.resultImageUrl" class="result-card__image-wrap">
        <img :src="task.resultImageUrl" alt="try-on result" class="result-card__image" />
      </div>
      <div v-else class="result-card__pending">
        <div v-if="task.errorMessage">{{ task.errorMessage }}</div>
        <div v-else>试穿任务已提交，正在等待结果。</div>
      </div>
    </div>

    <div v-if="history && history.length > 0" class="result-card__history">
      <div class="result-card__history-title">历史结果</div>
      <div class="result-card__history-grid">
        <div v-for="item in history" :key="item.taskId" class="history-tile">
          <img v-if="item.resultImageUrl" :src="item.resultImageUrl" alt="" />
          <div class="history-tile__meta">任务 #{{ item.taskId }}</div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.result-card {
  border: 1px solid #d5d9d9;
  border-radius: 10px;
  background: #fff;
  padding: 16px;
}

.result-card__head h3 {
  margin: 0;
  color: #0f1111;
  font-size: 18px;
  font-weight: 700;
}

.result-card__head p {
  margin: 8px 0 0;
  color: #565959;
  font-size: 12px;
  line-height: 1.5;
}

.result-card__empty,
.result-card__pending {
  margin-top: 16px;
  border-radius: 12px;
  background: #f7fafa;
  color: #565959;
  font-size: 13px;
  padding: 20px;
}

.result-card__status {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  color: #565959;
  font-size: 12px;
}

.result-card__image-wrap {
  margin-top: 16px;
  overflow: hidden;
  border-radius: 12px;
  background: #f7f8fa;
}

.result-card__image {
  width: 100%;
  display: block;
}

.result-card__history {
  margin-top: 20px;
  border-top: 1px solid #eaeded;
  padding-top: 16px;
}

.result-card__history-title {
  color: #0f1111;
  font-size: 14px;
  font-weight: 700;
}

.result-card__history-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.history-tile {
  overflow: hidden;
  border-radius: 10px;
  background: #f7f8fa;
}

.history-tile img {
  width: 100%;
  aspect-ratio: 3 / 4;
  object-fit: cover;
}

.history-tile__meta {
  color: #565959;
  font-size: 12px;
  padding: 8px;
}

@media (max-width: 767px) {
  .result-card__history-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
