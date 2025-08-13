import { config } from '@vue/test-utils'

// RouterLink stub to avoid full router instance in unit tests
config.global.stubs = {
  RouterLink: {
    template: '<a><slot /></a>'
  }
}
