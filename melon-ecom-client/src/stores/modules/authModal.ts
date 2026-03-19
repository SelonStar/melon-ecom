import { defineStore } from 'pinia'
import { ref } from 'vue'

type Tab = 'login' | 'register'
type PendingAction = null | (() => void | Promise<void>)

export const useAuthModalStore = defineStore('authModal', () => {
  const visible = ref(false)
  const activeTab = ref<Tab>('login')
  const pendingAction = ref<PendingAction>(null)

  function open(tab: Tab = 'login', action?: PendingAction) {
    activeTab.value = tab
    visible.value = true
    pendingAction.value = action ?? null
  }

  function close() {
    visible.value = false
    pendingAction.value = null
    activeTab.value = 'login'
  }

  function setActiveTab(tab: Tab) {
    activeTab.value = tab
  }

  async function runPendingAction() {
    const act = pendingAction.value
    pendingAction.value = null
    if (act) await act()
  }

  return { visible, activeTab, open, close, setActiveTab, runPendingAction }
})