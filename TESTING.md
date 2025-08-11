# Testing in Syntopia

This repo includes backend unit tests (Maven + JUnit + MockMvc), frontend unit tests (Vitest), and Playwright E2E tests.

## Backend (Spring Boot)
- Run all unit tests:
	```bash
	cd backend
	mvn test -Dtest='*Test'
	```
- Notes:
	- Auth in controllers is resolved via Authentication/SecurityContext/session; tests supply auth via Spring Security test helpers.

## Frontend Unit (Vitest)
- Install deps and run unit tests:
	```bash
	cd frontend
	npm install
	npm run test:unit
	```
- Watch mode:
	```bash
	npm run test:unit:watch
	```

## Frontend E2E (Playwright)
- Prereqs: Backend on :8080 and frontend dev server on :3000.
- Install browsers (first time only):
	```bash
	cd frontend
	npx playwright install --with-deps
	```
- Run tests:
	```bash
	npm run test:e2e
	```
- GUI mode:
	```bash
	npm run test:e2e:ui
	```

## Troubleshooting
- If Playwright specs are detected by Vitest, ensure e2e specs are under `tests/e2e` and Vitest excludes that folder (configured in `vitest.config.ts`).
- If backend tests fail with 401 on authenticated endpoints, confirm the test uses `with(authentication(...)).with(csrf())` or similar.
