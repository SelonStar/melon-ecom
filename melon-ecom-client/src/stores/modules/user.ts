import { defineStore } from 'pinia'
import piniaPersistConfig from '@/stores/helper/persist'
import type { UserState, UserModel } from '@/stores/interface/index'
import { login, logout, getUserInfo } from '@/api/user'

export const UserStore = defineStore('UserStore', {
  state: (): UserState => ({
    userInfo: {} as Partial<UserModel>,
    isLoggedIn: false,
  }),
  actions: {
    // 设置用户信息（后端 userAvatar -> 前端 avatarUrl）
    setUserInfo(userInfo: any, token?: string) {
      this.userInfo = {
        userId: userInfo.userId,
        username: userInfo.username,
        phone: userInfo.phone,
        email: userInfo.email,
        avatarUrl: userInfo.userAvatar,
        introduction: userInfo.introduction,
        token,
      }
      this.isLoggedIn = true
    },

    updateUserAvatar(avatarUrl: string) {
      this.userInfo.avatarUrl = avatarUrl
    },

    clearUserInfo() {
      this.userInfo = {}
      this.isLoggedIn = false
    },

    async userLogin(loginData: { email: string; password: string }) {
      try {
        const loginRes = await login(loginData)
        if (loginRes.code !== 0) {
          return { success: false, message: loginRes.message || '登录失败' }
        }

        const token = loginRes.data // 登录接口返回 token

        // 先把 token 存入 store，让请求拦截器能带上 Authorization 头
        this.userInfo = { token }

        const infoRes = await getUserInfo()
        if (infoRes.code !== 0) {
          this.clearUserInfo()
          return { success: false, message: infoRes.message || '获取用户信息失败' }
        }

        this.setUserInfo(infoRes.data, token)
        return { success: true, message: '登录成功' }
      } catch (e: any) {
        this.clearUserInfo()
        return { success: false, message: e?.message || '登录失败' }
      }
    },

    async userLogout() {
      try {
        const res = await logout()
        if (res.code === 0) {
          this.clearUserInfo()
          return { success: true, message: '退出成功' }
        }
        return { success: false, message: res.message || '退出失败' }
      } catch (e: any) {
        return { success: false, message: e?.message || '退出失败' }
      }
    },
  },
  persist: piniaPersistConfig('UserStore'),
})