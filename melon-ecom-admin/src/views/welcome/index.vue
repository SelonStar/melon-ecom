<script setup lang="ts">
import ReCol from "@/components/ReCol";
import { useDark } from "./utils";
import { ReNormalCountTo } from "@/components/ReCountTo";
import { ChartLine } from "./components/charts";
import useChartData from "./hooks/useChartData";
import GroupLine from "@iconify-icons/ri/group-line";
import ShoppingBag3Line from "@iconify-icons/ri/shopping-bag-3-line";
import HomeWarehouseLine from "@iconify-icons/ri/store-2-line";
import ArchiveStackLine from "@iconify-icons/ri/archive-2-line";

defineOptions({
  name: "Welcome"
});

const { isDark } = useDark();

const { userCount, productCount, warehouseCount, stockCount } = useChartData();
</script>

<template>
  <div>
    <el-row :gutter="24" justify="space-around">
      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{ opacity: 0, y: 100 }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: { delay: 80 }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">用户数量</span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{ backgroundColor: isDark ? 'transparent' : '#effaff' }"
            >
              <IconifyIconOffline :icon="GroupLine" :color="'#41b6ff'" width="18" />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="2200"
                :fontSize="'2.5em'"
                :startVal="0"
                :endVal="userCount"
              />
            </div>
            <ChartLine
              class="!w-1/2"
              :color="'#41b6ff'"
              :data="[12, 18, 25, 32, 45, 56, 68]"
            />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{ opacity: 0, y: 100 }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: { delay: 160 }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">商品数量</span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{ backgroundColor: isDark ? 'transparent' : '#fff5f4' }"
            >
              <IconifyIconOffline
                :icon="ShoppingBag3Line"
                :color="'#e85f33'"
                width="18"
              />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="2200"
                :fontSize="'2.5em'"
                :startVal="0"
                :endVal="productCount"
              />
            </div>
            <ChartLine
              class="!w-1/2"
              :color="'#e85f33'"
              :data="[5, 9, 15, 22, 28, 35, 48]"
            />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{ opacity: 0, y: 100 }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: { delay: 240 }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">仓库数量</span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{ backgroundColor: isDark ? 'transparent' : '#eff8f4' }"
            >
              <IconifyIconOffline
                :icon="HomeWarehouseLine"
                :color="'#26ce83'"
                width="18"
              />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="2200"
                :fontSize="'2.5em'"
                :startVal="0"
                :endVal="warehouseCount"
              />
            </div>
            <ChartLine
              class="!w-1/2"
              :color="'#26ce83'"
              :data="[1, 1, 2, 2, 3, 3, 4]"
            />
          </div>
        </el-card>
      </re-col>

      <re-col
        v-motion
        class="mb-[18px]"
        :value="6"
        :md="12"
        :sm="12"
        :xs="24"
        :initial="{ opacity: 0, y: 100 }"
        :enter="{
          opacity: 1,
          y: 0,
          transition: { delay: 320 }
        }"
      >
        <el-card class="line-card" shadow="never">
          <div class="flex justify-between">
            <span class="text-md font-medium">库存记录数</span>
            <div
              class="w-8 h-8 flex justify-center items-center rounded-md"
              :style="{ backgroundColor: isDark ? 'transparent' : '#f6f4fe' }"
            >
              <IconifyIconOffline
                :icon="ArchiveStackLine"
                :color="'#7846e5'"
                width="18"
              />
            </div>
          </div>
          <div class="flex justify-between items-start mt-3">
            <div class="w-1/2">
              <ReNormalCountTo
                :duration="2200"
                :fontSize="'2.5em'"
                :startVal="0"
                :endVal="stockCount"
              />
            </div>
            <ChartLine
              class="!w-1/2"
              :color="'#7846e5'"
              :data="[20, 35, 48, 62, 75, 96, 120]"
            />
          </div>
        </el-card>
      </re-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
:deep(.el-card) {
  --el-card-border-color: none;

  .el-progress--line {
    width: 85%;
  }

  .el-progress-bar__innerText {
    font-size: 15px;
  }

  .el-scrollbar__bar {
    display: none;
  }

  .el-timeline-item {
    margin: 0 6px;
  }
}

.main-content {
  margin: 20px 20px 0 !important;
}

.bar-position {
  margin-top: 18px;
}
</style>