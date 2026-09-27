import type { Directive } from 'vue'
import { useUserStore } from '@/store/modules/user'

export const vPerm: Directive = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const perms = binding.value
    if (perms && !userStore.permissionList.includes(perms)) {
      el.parentNode?.removeChild(el)
    }
  }
}