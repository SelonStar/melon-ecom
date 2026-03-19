<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'

import { batchAddCartItems } from '@/api/cart'
import { createTryOnTask, getTryOnCandidateSkus, getTryOnTask, uploadTryOnPhoto } from '@/api/tryon'
import TryOnPhotoUploader from '@/components/tryon/TryOnPhotoUploader.vue'
import TryOnPreviewPanel from '@/components/tryon/TryOnPreviewPanel.vue'
import TryOnProductSelector from '@/components/tryon/TryOnProductSelector.vue'
import TryOnResultPanel from '@/components/tryon/TryOnResultPanel.vue'
import type {
  CreateTryOnTaskReq,
  TryOnCandidateSkuVO,
  TryOnPhotoType,
  TryOnSelectionMap,
  TryOnSlotType,
  TryOnTaskVO,
  UploadTryOnPhotoResp,
} from '@/types/tryon'

const photo = ref<UploadTryOnPhotoResp | null>(null)
const photoUploading = ref(false)
const generating = ref(false)
const currentTask = ref<TryOnTaskVO | null>(null)
const taskHistory = ref<TryOnTaskVO[]>([])
const pollTimer = ref<number | null>(null)

const selection = reactive<TryOnSelectionMap>({
  TOP: null,
  BOTTOM: null,
  DRESS: null,
  SHOES: null,
})

const slotTitles: Record<TryOnSlotType, string> = {
  TOP: '2. 选择上装',
  BOTTOM: '3. 选择下装',
  DRESS: '4. 选择裙子',
  SHOES: '5. 选择鞋子',
}

const slotList = ref<Record<TryOnSlotType, TryOnCandidateSkuVO[]>>({
  TOP: [],
  BOTTOM: [],
  DRESS: [],
  SHOES: [],
})

const slotLoading = ref<Record<TryOnSlotType, boolean>>({
  TOP: false,
  BOTTOM: false,
  DRESS: false,
  SHOES: false,
})

const selectedSkuIds = computed(() =>
  [selection.TOP?.skuId, selection.BOTTOM?.skuId, selection.DRESS?.skuId, selection.SHOES?.skuId].filter(
    Boolean
  ) as number[]
)

const canGenerate = computed(() => !!photo.value?.photoUrl && selectedSkuIds.value.length > 0)
const canAddCart = computed(() => currentTask.value?.status === 'SUCCESS' && selectedSkuIds.value.length > 0)
const canReplace = computed(() => !!currentTask.value && canGenerate.value)

const fetchCandidates = async (slotType: TryOnSlotType) => {
  slotLoading.value[slotType] = true
  try {
    const res = await getTryOnCandidateSkus({
      slotType,
      photoType: photo.value?.photoType || 'FULL_BODY',
      pageNum: 1,
      pageSize: 8,
    })

    if (res.code !== 0 || !res.data) {
      slotList.value[slotType] = []
      return
    }

    slotList.value[slotType] = res.data.records || []
  } catch (error) {
    console.error(error)
    slotList.value[slotType] = []
  } finally {
    slotLoading.value[slotType] = false
  }
}

const fetchAllCandidates = async () => {
  await Promise.all((['TOP', 'BOTTOM', 'DRESS', 'SHOES'] as TryOnSlotType[]).map(fetchCandidates))
}

const onUpload = async (payload: { file: File; photoType: TryOnPhotoType }) => {
  photoUploading.value = true
  try {
    const res = await uploadTryOnPhoto(payload.photoType, payload.file)
    if (res.code !== 0 || !res.data) {
      ElMessage.error(res.message || '上传失败')
      return
    }
    photo.value = res.data
    ElMessage.success('试穿照片上传成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    photoUploading.value = false
  }
}

const onSelectSku = (slotType: TryOnSlotType, value: TryOnCandidateSkuVO) => {
  selection[slotType] = value

  if (slotType === 'DRESS' && value) {
    selection.TOP = null
    selection.BOTTOM = null
  }

  if ((slotType === 'TOP' || slotType === 'BOTTOM') && value) {
    selection.DRESS = null
  }
}

const stopPolling = () => {
  if (pollTimer.value != null) {
    window.clearTimeout(pollTimer.value)
    pollTimer.value = null
  }
}

const pushCurrentTaskToHistory = () => {
  if (!currentTask.value?.resultImageUrl) return
  if (taskHistory.value.some((item) => item.taskId === currentTask.value?.taskId)) return
  taskHistory.value.unshift(currentTask.value)
  taskHistory.value = taskHistory.value.slice(0, 6)
}

const schedulePoll = (taskId: number) => {
  stopPolling()
  pollTimer.value = window.setTimeout(async () => {
    try {
      const res = await getTryOnTask(taskId)
      if (res.code !== 0 || !res.data) return
      currentTask.value = res.data
      if (res.data.status === 'SUBMITTED' || res.data.status === 'PROCESSING') {
        schedulePoll(taskId)
      } else {
        generating.value = false
      }
    } catch (error) {
      console.error(error)
      generating.value = false
    }
  }, 2000)
}

const buildTaskReq = (): CreateTryOnTaskReq => ({
  photoUrl: photo.value!.photoUrl,
  photoType: photo.value!.photoType,
  topSkuId: selection.TOP?.skuId || null,
  bottomSkuId: selection.BOTTOM?.skuId || null,
  dressSkuId: selection.DRESS?.skuId || null,
  shoesSkuId: selection.SHOES?.skuId || null,
  currentResultImageUrl: currentTask.value?.resultImageUrl,
  providerCode: 'MOCK',
})

const submitTask = async () => {
  if (!canGenerate.value) {
    ElMessage.warning('请先上传照片并至少选择一件可试穿商品')
    return
  }

  pushCurrentTaskToHistory()
  generating.value = true

  try {
    const res = await createTryOnTask(buildTaskReq())
    if (res.code !== 0 || !res.data) {
      ElMessage.error(res.message || '试穿生成失败')
      generating.value = false
      return
    }

    currentTask.value = res.data
    if (res.data.status === 'SUBMITTED' || res.data.status === 'PROCESSING') {
      schedulePoll(res.data.taskId)
    } else {
      generating.value = false
    }
  } catch (error) {
    console.error(error)
    generating.value = false
    ElMessage.error('试穿生成失败，请稍后重试')
  }
}

const replaceAndRegenerate = async () => {
  if (!canReplace.value) {
    ElMessage.warning('请先完成一次试穿，再更换单品重生成')
    return
  }
  await submitTask()
}

const resetSelection = () => {
  selection.TOP = null
  selection.BOTTOM = null
  selection.DRESS = null
  selection.SHOES = null
  currentTask.value = null
  taskHistory.value = []
  stopPolling()
}

const addCurrentOutfitToCart = async () => {
  if (!canAddCart.value) {
    ElMessage.warning('请先生成成功的试穿结果')
    return
  }

  try {
    const res = await batchAddCartItems(selectedSkuIds.value.map((skuId) => ({ skuId, quantity: 1 })))
    if (res.code !== 0 || !res.data) {
      ElMessage.error(res.message || '加入购物车失败')
      return
    }

    const success = res.data.successCount || 0
    const fail = res.data.failCount || 0
    const failMessages = (res.data.items || [])
      .filter((item) => !item.success)
      .map((item) => `SKU ${item.skuId}: ${item.message || '加入失败'}`)

    ElNotification({
      title: '搭配已加入购物车',
      message: failMessages.length > 0
        ? `成功 ${success} 件，失败 ${fail} 件。${failMessages.join('；')}`
        : `共成功加入 ${success} 件商品。`,
      type: fail > 0 ? 'warning' : 'success',
      duration: 5000,
    })
  } catch (error) {
    console.error(error)
    ElMessage.error('加入购物车失败，请稍后重试')
  }
}

watch(
  () => photo.value?.photoType,
  (value) => {
    if (!value) return
    if (value === 'HALF_BODY') {
      selection.BOTTOM = null
      selection.SHOES = null
    }
    fetchAllCandidates()
  }
)

onMounted(fetchAllCandidates)
onBeforeUnmount(stopPolling)
</script>

<template>
  <div class="tryon-page">
    <div class="tryon-hero">
      <div class="tryon-hero__inner">
        <div>
          <!-- <div class="tryon-hero__eyebrow">Amazon 风格电商亮点模块</div> -->
          <h1>AI 试穿</h1>
          <p>先上传用户照片，再按上装、下装、裙子、鞋子选择支持试穿的白名单商品，生成适合演示的穿搭结果。</p>
        </div>
        <div class="tryon-hero__tips">
          <div>半身照：优先上装 / 裙子</div>
          <div>全身照：支持完整穿搭</div>
          <div>裙子与上装/下装互斥</div>
          <div>加入购物车按当前 SKU 批量加入</div>
        </div>
      </div>
    </div>

    <div class="tryon-container">
      <div class="tryon-main">
        <TryOnPhotoUploader v-model="photo" :uploading="photoUploading" @upload="onUpload" />

        <div class="selector-section">
          <TryOnProductSelector
            v-for="slot in (['TOP', 'BOTTOM', 'DRESS', 'SHOES'] as TryOnSlotType[])"
            :key="slot"
            :title="slotTitles[slot]"
            :slot-type="slot"
            :photo-type="photo?.photoType || 'FULL_BODY'"
            :list="slotList[slot]"
            :loading="slotLoading[slot]"
            :selected-sku-id="selection[slot]?.skuId || null"
            @select="onSelectSku(slot, $event)"
          />
        </div>
      </div>

      <aside class="tryon-side">
        <TryOnPreviewPanel :selection="selection" />

        <div class="action-card">
          <h3>操作区</h3>
          <button class="action-card__btn action-card__btn--primary" :disabled="!canGenerate || generating" @click="submitTask">
            生成试穿图
          </button>
          <button class="action-card__btn" :disabled="!canReplace || generating" @click="replaceAndRegenerate">
            更换单品后重生成
          </button>
          <button class="action-card__btn" @click="resetSelection">
            重置搭配
          </button>
          <button class="action-card__btn action-card__btn--accent" :disabled="!canAddCart" @click="addCurrentOutfitToCart">
            加入购物车
          </button>
        </div>

        <TryOnResultPanel :task="currentTask" :history="taskHistory" :generating="generating" />
      </aside>
    </div>
  </div>
</template>

<style scoped>
.tryon-page {
  min-height: 100%;
  background:
    linear-gradient(180deg, #131921 0 280px, #f3f3f3 280px 100%);
}

.tryon-hero {
  padding: 28px 24px 40px;
}

.tryon-hero__inner {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(320px, 1fr);
  gap: 24px;
  max-width: 1320px;
  margin: 0 auto;
  color: #fff;
}

.tryon-hero__eyebrow {
  color: #ffd814;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.tryon-hero h1 {
  margin: 10px 0 0;
  font-size: 40px;
  line-height: 1.1;
}

.tryon-hero p {
  max-width: 760px;
  margin: 14px 0 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 15px;
  line-height: 1.7;
}

.tryon-hero__tips {
  display: grid;
  gap: 10px;
  align-content: start;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(6px);
}

.tryon-container {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(360px, 420px);
  gap: 24px;
  max-width: 1320px;
  margin: 0 auto;
  padding: 0 24px 32px;
}

.tryon-main,
.tryon-side {
  display: grid;
  gap: 20px;
  align-content: start;
}

.selector-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.action-card {
  border: 1px solid #d5d9d9;
  border-radius: 10px;
  background: #fff;
  padding: 16px;
}

.action-card h3 {
  margin: 0 0 12px;
  color: #0f1111;
  font-size: 18px;
  font-weight: 700;
}

.action-card__btn {
  width: 100%;
  border: 1px solid #d5d9d9;
  border-radius: 999px;
  background: #f7fafa;
  color: #0f1111;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  padding: 12px 16px;
  transition: opacity 0.2s ease;
}

.action-card__btn + .action-card__btn {
  margin-top: 10px;
}

.action-card__btn--primary {
  border-color: #fcd200;
  background: #ffd814;
}

.action-card__btn--accent {
  border-color: #ff8f00;
  background: #ffa41c;
}

.action-card__btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

@media (max-width: 1180px) {
  .tryon-container {
    grid-template-columns: minmax(0, 1fr);
  }

  .tryon-hero__inner {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 767px) {
  .tryon-hero h1 {
    font-size: 32px;
  }

  .selector-section {
    grid-template-columns: minmax(0, 1fr);
  }

  .tryon-hero,
  .tryon-container {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
