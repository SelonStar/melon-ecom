import { onMounted, ref } from "vue";
import { getDashboardCounts } from "@/api/data";
import { message } from "@/utils/message";

export default function useChartData() {
  const loading = ref(false);

  /** 电商后台首页统计 */
  const userCount = ref<number>(0);
  const productCount = ref<number>(0);
  const warehouseCount = ref<number>(0);
  const stockCount = ref<number>(0);

  const resetCounts = () => {
    userCount.value = 0;
    productCount.value = 0;
    warehouseCount.value = 0;
    stockCount.value = 0;
  };

  const fetchData = async () => {
    loading.value = true;
    try {
      const data = await getDashboardCounts();
      userCount.value = Number(data.userCount ?? 0);
      productCount.value = Number(data.productCount ?? 0);
      warehouseCount.value = Number(data.warehouseCount ?? 0);
      stockCount.value = Number(data.stockCount ?? 0);
    } catch (error) {
      console.error("获取后台统计数据失败:", error);
      message("获取后台统计数据失败", { type: "error" });
      resetCounts();
    } finally {
      loading.value = false;
    }
  };

  onMounted(fetchData);

  return {
    loading,
    userCount,
    productCount,
    warehouseCount,
    stockCount,
    fetchData
  };
}