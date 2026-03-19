<script setup lang="ts">
import { computed } from 'vue'
import LoginForm from '@/components/Auth/LoginForm.vue'
import RegisterForm from '@/components/Auth/RegisterForm.vue'
import { useAuthModalStore } from '@/stores/modules/authModal'

const authModal = useAuthModalStore()

const activeTab = computed({
  get: () => authModal.activeTab,
  set: (v: 'login' | 'register') => authModal.setActiveTab(v),
})

const onClose = () => {
  authModal.close()
}

// 子表单发出 switch-tab
const handleSwitchTab = (tab: 'login' | 'register') => {
  authModal.setActiveTab(tab)
}

// 子表单登录/注册成功
const handleSuccess = async () => {
  // 先关弹窗
  authModal.close()

  // 执行登录后续动作（比如继续加购）
  await authModal.runPendingAction()
}
</script>

<template>
  <el-dialog
    v-model="authModal.visible"
    title="登录 / 注册"
    width="420px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @close="onClose"
  >
    <el-tabs v-model="activeTab" class="w-full">
      <el-tab-pane label="登录" name="login">
        <LoginForm @success="handleSuccess" @switch-tab="handleSwitchTab" />
      </el-tab-pane>

      <el-tab-pane label="注册" name="register">
        <RegisterForm @success="handleSuccess" @switch-tab="handleSwitchTab" />
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>