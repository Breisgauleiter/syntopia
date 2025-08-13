# Syntopia Project Roadmap
*Updated: August 2025 (mid) — Focused, actionable, and truthful status*

---

## Test User (for local dev)
- Username: `testuser`
- Email: `testuser@syntopia.com`
- Password: `TestPass123!`
- Current Level: 1 (Onboarding-ready)

---

## Current Status (truthful)

- Platform
  - Frontend: Vue 3 + TypeScript + Vite + Pinia up and running (dev server active)
  - Backend: Spring Boot 3 + Java 21 + ArangoDB (TAO structure seeded via init script)
  - Local Dev: Docker compose present; VS Code tasks for frontend/backend running

- Quest System
  - Models/Repo: `Quest`, `UserQuest`, `QuestRepository`, `UserQuestRepository` present
  - Backend: `QuestController`/`QuestService` implemented; GitHub quest endpoints scaffolded
  - Frontend: `quest.service.ts` with types; `QuestsView.vue` + Quest components integrated
  - Recent alignment: ✅ UserQuestRepository AQL now inlines `quest` data (MERGE) so frontend always has quest details
  - Recent alignment: ✅ Frontend maps `UserQuest.status` to quest.status for consistent button states; reduced full reloads
  - Known gaps
    - Verification UI: backend verification endpoint & USER_VERIFIED status added; frontend needs verify action/button & optimistic update
    - Pagination adoption: backend pagination + UserQuestDTO (explicit userId/questId) implemented; frontend service/view must consume paginated ApiResponse
    - Onboarding generator: `OnboardingQuestGenerator` is stubbed (generate methods incomplete)
    - Testing: add backend tests (repository embedding, verification transition) & frontend tests (status mapping, pagination, verification)
    - Cleanup: remove legacy derivation logic once quest.service.ts updated for DTO
  - Frontend activation: ✅ onboarding CTAs now call `POST /api/onboarding/accept`; tracker/panels refresh active list

- Profile System
  - Backend: `ProfileController`/`ProfileService` implemented (get/update/avatar/achievements/social)
  - Frontend: `ProfileView.vue` integrated with `profile.service.ts`
  - Known gaps
    - Achievements/social in service currently sample/mocked; replace with TAO queries
    - Some profile fields (bio/social links) not fully persisted by current save handler

- Community System
  - Backend: ✅ `CommunityController` fully implemented with proper DTOs; `CommunityService` uses real ArangoDB data
  - Repositories: ✅ `UserCollaborationRepository`, `UserProjectRepository`, `UserQuestRepository` enhanced with comprehensive AQL queries
  - Data Layer: ✅ All community endpoints now return persisted data (feed, connections, projects, leaderboard, stats)
  - Frontend: ✅ Contracts aligned; `community.service.ts` normalizes payloads (projects pagination, leaderboard entries); `CommunityView.vue` now uses the service exclusively; basic pagination and error handling wired
  - New (Aug mid): ✅ Connections Pinia store with optimistic send/accept/decline/cancel; ✅ pagination & filtering (status/direction) + UI controls; ✅ toast notification system integrated (Community/Profile); ✅ component + store tests for optimistic lifecycle & rollback
  - Known gaps
    - Feed reflection: ensure accepted/declined connection events appear promptly (may require polling or SSE next)
    - DTO completeness: confirm connections endpoint returns both `fromUserId` & `toUserId` (direction currently inferred; explicit fields reduce logic)
    - Pagination edge tests: add frontend tests for multi-page load & filter resets; backend tests for boundary (empty last page)
    - Error surfaces: toast on load failures & retry affordance (basic errors logged only now)

  - Testing: Backend MockMvc and frontend Vitest + Playwright scaffolding added; backend and unit tests green locally; see TESTING.md for commands

- Cross-cutting
  - Response pattern: Aim to standardize on a unified `ApiResponse` across controllers (partially applied)
  - Types: TS types generally good; a few TODOs remain in community/profile flows

---

## Immediate Priorities (Weeks 1–2)

1) ✅ Community backend: replace mocks with real data - **COMPLETED**
- ✅ Added repositories: `UserCollaborationRepository`, `UserProjectRepository` with comprehensive AQL queries
- ✅ Implemented in `CommunityService`:
  - Feed: recent `user_quests` status changes, new projects, new connections with real aggregation
  - Connections: request/create/accept/decline flows with edge writes and validation
  - Projects: list/create with `user_projects` edges and member management
  - Leaderboard/stats: aggregate via AQL (XP, completed quests) with pagination
- ✅ All community endpoints return persisted data; no hardcoded samples
- ✅ Created comprehensive `CommunityDTO` classes for proper API contracts

2) ✅ Community frontend: align contracts and centralize API usage — **COMPLETED**
- Updated `community.service.ts` payloads to match backend DTOs; normalized projects pagination and leaderboard entries
- Refactored `CommunityView.vue` to use only service methods; removed direct `api.*` calls
- Wired loading/error states and minimal pagination; happy-path manual tests pass

3) Profile achievements/social: replace samples with real queries
- Implement TAO queries for achievements (from `user_quests`) and social connections
- Ensure `ProfileView.vue` save handler persists bio/social fields fully
- Acceptance: Achievements/social reflect DB state; profile update persists all edited fields

4) Quest UX and endpoints: small but critical
- Endpoints for “list active/completed/user-available”: ✅ available (verify naming/pagination)
- UX polish: ✅ role filter, ✅ glassmorphism on tracker/panels, ✅ onboarding surfaced/pinned, ✅ active count correct with onboarding fallback
- Backend refactor: ✅ UserQuestController now uses ApiResponse, pagination, DTO (UserQuestDTO), verification endpoint (`POST /api/user-quests/{questId}/verify`)
- Remaining (frontend): wire verification button, adapt to paginated `{ data, pagination }` shape, update quest.service.ts to use `userQuestId`
- Acceptance: QuestsView shows active/completed consistently; verification action transitions to USER_VERIFIED without full reload

5) ✅ Onboarding activation wiring (frontend) — COMPLETED
- CTAs in QuestPanel/QuestTracker/QuestsView call `POST /api/onboarding/accept`
- OnboardingView auto-accepts on mount/role-select if not completed
- Tracker listens to onboarding acceptance events and refreshes active list

6) Community connections flow validation — IN PROGRESS
- ✅ Store + optimistic lifecycle + pagination + filters
- ✅ Component integration tests (accept success, decline failure rollback)
- ✅ UI controls for status/direction + Load More
- Remaining: feed event reflection, backend DTO explicit direction fields, multi-page test coverage

---

## Near Term (Weeks 3–4)

- Onboarding quests generator
  - Implement `OnboardingQuestGenerator.generateAllOnboardingQuests()` and per-role/level generation
  - Seed or expose admin endpoint to generate and store onboarding quests
  - Acceptance: Each of 7 roles × 4 levels produces quests with XP per Fibonacci scale; visible in filters

- Unified ApiResponse pattern
  - Progress: UserQuestController migrated (pagination + DTO). Remaining controllers (Profile, Quest, Community) to standardize.
  - Acceptance: All controllers return `{ success, data, error?, pagination? }`

- Quest frontend adoption (NEXT)
  - Implement verification button + optimistic USER_VERIFIED transition
  - Consume paginated `{ data, pagination }` in list views & update tests
  - Remove legacy derivation logic after DTO adoption complete

- Community enhancements (NEXT)
  - Add connection feed polling or lightweight SSE for live updates
  - Add pagination & filter E2E test (Playwright) for connections
  - Surface errors with retry button in Connections section

- Profile achievements/social (carry-over)
  - Replace mock achievements/social with real TAO aggregation & persist additional profile fields

- Basic notifications (non-realtime)
  - Add server-side events (initial), or simple polling for community events
  - Acceptance: User sees new connection requests and quest updates without refresh (polling acceptable)

- CI workflows (unit + lint; optional E2E)
  - Add GitHub Actions to run backend unit tests (mvn -q -DskipITs=false test) and frontend unit tests (Vitest) on PRs
  - Optional: gated Playwright E2E via manual dispatch or label to keep PRs fast
  - Acceptance: PRs show green checks for backend and frontend unit tests; optional E2E job runnable on demand

---

## Recent Validation (smoke tests)
- Auth: register/login/me ✅; JWT persisted for reuse
- Onboarding: `POST /api/onboarding/accept` ✅ creates active onboarding `userQuest`; `GET /api/user-quests/active` shows it
- Community: feed/users/projects/stats/leaderboard ✅
  - Created project via `POST /api/community/projects` ✅ and listed in projects
  - Leaderboard empty for fresh data (expected); feed empty initially (expected)
  - Connection request POST attempted; UI flow validation planned next

---

## Milestones and Deliverables

Milestone A — Community Data Live (end of Week 2) - ✅ **COMPLETED**
- ✅ Community feed/connections/projects/leaderboard backed by ArangoDB with real AQL queries
- ✅ Backend DTOs implemented for type-safe API contracts
- ✅ All service methods use real persistence instead of mocks
- Next: Frontend service alignment and integration testing

Milestone B — Profile Insights Real (end of Week 2) — PARTIAL (backend endpoints present; queries pending)
- Achievements/social from graph queries
- Profile editing persists bio/social links

Milestone C — Onboarding Generator + Quest Lists (end of Week 4)
Milestone D — Connections UX Polish (added)
- Criteria: Feed event reflection, error toasts with retries, connection direction fields explicit, pagination edge tests (frontend/backend), live update mechanism (polling or SSE v1)
- Generator implemented and seeded
- Quest endpoints for active/completed lists finalized (backend endpoints present; generator pending)

---

## Technical Tasks (backlog)

- Repositories/AQL - ✅ **MAJOR PROGRESS**
  - ✅ Connection edges: query by direction/status, paginate, counts implemented
  - ✅ Project membership edges: create/list contributors, by role implemented  
  - ✅ Leaderboard aggregations: XP, quests completed; weekly/monthly windows implemented
  - ✅ Feed aggregation: union queries with real data from multiple sources

- Frontend quality
  - Replace remaining direct `api.*` calls with services (audit leftover views)
  - Add types for community DTOs; remove any `any` usage in views (CommunityView feed mapping still uses `any`)
  - Small UI polish for loading/empty/error states
  - Add accessibility: ARIA roles for connections lists & live region for pagination updates

- Infra/ops
  - Ensure avatar upload path exists and is configurable; add cleanup policy
  - Expand docker-compose for local Arango volume persistence

---

## Risks and Mitigations
- Risk: Data model drift between frontend DTOs and backend models
  - Mitigation: Centralize TS types per endpoint and align with controller schemas
- Risk: Arango AQL complexity/performance
  - Mitigation: Index review, EXPLAIN plans, paginate aggressively
- Risk: Contract mismatches in community flows
  - Mitigation: Update service contracts first; generate quick integration tests
 - Risk: Local Arango connectivity ("No host has been set!") can break backend boot
   - Mitigation: Provide defaults in `application.yml` via env (ARANGO_HOST/PORT/DB/USER/PASS) and prefer `docker-compose` for local

---

## Definition of Done (per feature)
- API endpoints:
  - Uses `ApiResponse` format
  - Validates input; returns 4xx with helpful messages
  - Unit or slice tests for service logic where feasible
- Frontend flows:
  - Only service layer used for HTTP
  - Loading/empty/error states present
  - Types accurate; no `any` in new code

---

## Sequenced Work Plan (refreshed)

1. ✅ ~~Align community service contracts (frontend + backend) and remove direct calls~~ **COMPLETED**
2. ✅ ~~Implement community AQL + repositories; wire real data for feed/connections/projects/leaderboard~~ **COMPLETED**
3. ✅ ~~Align frontend `community.service.ts` with new backend DTOs and test integration~~ **COMPLETED**
4. ✅ Quest user endpoints refactor: DTO + pagination + verification (backend)
5. Community connections feed integration & direction fields + pagination edge tests
6. Quests frontend adoption of DTO/pagination + verification UI & tests
7. Profile achievements/social: implement TAO queries; finish profile save handling
8. OnboardingQuestGenerator implementation + seeding path
9. Sweep for ApiResponse consistency across remaining controllers; add minimal tests/docs
10. CI workflows for backend/frontend unit tests (optional E2E) to protect PRs
11. Live update mechanism (polling → potential SSE) for connections/feed

### Recent Additions Summary
- Connections: optimistic lifecycle store, pagination & filtering UI, integration + rollback tests
- Toast notification system (global) replacing alert usage in Community & Profile views
- Component test for connections accept + decline rollback; store tests expanded
- Styling: connections filters bar with responsive layout

---

## Appendix
- Key files to touch next
  - Backend: `CommunityService`, `CommunityController`, `UserQuestController`, `ProfileService`, repositories for collaborations/projects
  - Frontend: `frontend/src/views/CommunityView.vue` (connections actions/UX), `frontend/src/services/quest.service.ts` (progress/complete), `frontend/src/services/profile.service.ts`, `frontend/src/views/ProfileView.vue`

- Review cadence
  - Weekly checkpoint: demo feed/connections/projects working from DB
  - Biweekly: profile achievements/social + onboarding generator status
