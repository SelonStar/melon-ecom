import { defineStore } from 'pinia'
import { useAuthModalStore } from '@/stores/modules/authModal'
import { UserStore } from '@/stores/modules/user'
import { addCartItem } from '@/api/cart'
import { ElMessage } from 'element-plus'
import router from '@/routers'

export const useCartStore = defineStore('cart', () => {
  const userStore = UserStore()
  const authModal = useAuthModalStore()

  async function addToCart(skuId: number, quantity = 1) {
    // 未登录 → 弹窗
    if (!userStore.isLoggedIn || !userStore.userInfo?.token) {
      authModal.open('login', async () => {
        await addToCart(skuId, quantity)
      })
      return
    }

    try {
      const res = await addCartItem({ skuId, quantity })

      if (res.code !== 0) {
        ElMessage.error(res.message || '加入购物车失败')
        return
      }

      ElMessage.success('已加入购物车')
      router.push('/cart')
    } catch (e) {
      console.error(e)
      ElMessage.error('加入购物车失败，请稍后重试')
    }
  }

  return { addToCart }
})
