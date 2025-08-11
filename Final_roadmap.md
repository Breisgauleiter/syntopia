# Syntopia – Final Roadmap (Verified)

Date: 2025-08-11

## Scope and method
- Verified by reading backend controllers/services/repositories, DTOs/models, and frontend services/views.
- Focused on concrete implementations; flagged FE/BE contract gaps for alignment.

## Implemented (backend)
- Community: Feed, user directory, connections (request/respond/list), projects (list/create), stats, leaderboard. DB-backed via AQL in UserCollaborationRepository, UserProjectRepository, UserQuestRepository. ApiResponse used.
- Quests: UserQuest edge-based progression (available/accept/complete/abandon/progress/statistics) and discovery; onboarding seeding; GitHub quest creation. Legacy quest-status endpoints still present.
- Profile: GET/PUT, avatar upload, achievements, social, public profile, completion.

## Implemented (frontend)
- Views: Community, Quests, Profile. Services: community, quest, profile. API wrapper and types in place.
- UserQuest flow used in Quests view; community screens wired to backend services.

## Gaps and alignment tasks
- Quest progression: Standardize on UserQuest; avoid legacy quest-status endpoints in FE.
- Community contracts:
  - Leaderboard query param should be 'window' (week|month|all). Fixed in FE service.
  - Feed: Normalize backend feed item variants for display. Simple adapter added in CommunityView.
  - Connection respond: Ensure lowercase action ('accept'|'decline'). Adjusted in view.
- ApiResponse consistency: Audit controllers for uniform structure where FE expects ApiResponse.
- Profile stats: Complete projectsCount/questsCompleted in ProfileService.getCompleteProfile.

## Milestones and acceptance criteria
1) Community FE/BE alignment (Now)
- FE sends leaderboard 'window' param; feed adapter maps quest/project/connection items; respondToRequest uses lowercase action.
- Acceptance: CommunityView shows items without console errors; leaderboard loads for all windows.

2) Quest progression unification
- FE relies only on /user-quests endpoints; legacy endpoints hidden behind admin/testing flag.
- Acceptance: QuestsView flows (accept/complete/abandon) work exclusively via UserQuest; no mixed calls in logs.

3) ApiResponse standardization
- All controllers respond with { success, data, message? } consistently.
- Acceptance: FE services remove fallback parsing and rely on ApiResponse only.

4) Profile stats completion
- Backend ProfileService populates projectsCount and questsCompleted from repos.
- Acceptance: ProfileView shows accurate counts matching DB.

5) Onboarding generator integration
- Seed generator used idempotently across roles/levels 1–4; FE surfaces the generated items where applicable.
- Acceptance: Admin seed triggers don’t duplicate; users see role-appropriate onboarding quests.

## Risks and follow-ups
- Dual quest flows can drift; retire legacy soon.
- Feed shape evolution: keep adapter or move normalization server-side via a typed DTO.
- GitHub integration scope may expand; keep DTOs stable.

## Done in this commit
- Fixed FE leaderboard param ('window').
- Added feed adapter normalization in CommunityView.
- Lowercased connection respond actions and corrected success toast text derivation.

## Next actions
- Run FE to smoke-test Community tabs (feed/leaderboard/connections).
- Backend: standardize ApiResponse for any endpoints that deviate.
- Implement profile stats completion and tiny tests.
