import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import LoginView from '@/views/LoginView.vue'
import { createTestingPinia } from '@pinia/testing'
import { useUserStore } from '@/stores/user'

function mountWithStores() {
  return mount(LoginView, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn })]
    }
  })
}

describe('LoginView', () => {
  it('renders form and triggers store login on submit', async () => {
    const wrapper = mountWithStores()
    const store = useUserStore()
    store.login = vi.fn().mockResolvedValue(true)

    await wrapper.get('#email').setValue('testuser@syntopia.com')
    await wrapper.get('#password').setValue('TestPass123!')
    await wrapper.get('form').trigger('submit.prevent')

    expect(store.login).toHaveBeenCalledWith({ email: 'testuser@syntopia.com', password: 'TestPass123!' })
  })
})
