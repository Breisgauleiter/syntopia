import { test, expect } from '@playwright/test'

const email = `e2e_${Date.now()}@syntopia.local`
const password = 'TestPass123!'

async function register(page) {
  await page.goto('/')
  await page.getByRole('link', { name: 'Begin Your Journey' }).click()
  await page.getByLabel('Username').fill(`user_${Date.now()}`)
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByLabel('Display Name').fill('E2E User')
  await page.getByRole('button', { name: 'Create Account' }).click()
}

async function login(page) {
  await page.goto('/login')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: 'Sign In' }).click()
}

test('register, login, navigate to quests', async ({ page }) => {
  await register(page)
  // onboarding may auto-accept; navigate home then to quests
  await page.goto('/')
  await expect(page.getByText('Welcome to Syntopia')).toBeVisible()
  await page.getByRole('link', { name: /Continue Quest|Explore Patterns/ }).first().click()
})
