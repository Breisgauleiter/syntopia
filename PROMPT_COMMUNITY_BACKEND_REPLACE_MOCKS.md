# Prompt: Replace Community Backend Mocks with Real Data (ArangoDB / Spring Boot)

Goal
- Replace all mock/sample returns in CommunityService with repository-backed AQL queries and real persistence. Align API contracts with frontend. Deliver DB-backed feed, connections, projects, stats, and leaderboard.

Scope (backend only for this prompt)
- Files: backend/src/main/java/com/syntopia/service/CommunityService.java, backend/src/main/java/com/syntopia/controller/CommunityController.java, backend/src/main/java/com/syntopia/repository/** (UserCollaborationRepository, UserProjectRepository, UserQuestRepository, UserRepository), backend/src/main/java/com/syntopia/model/**
- Database: ArangoDB TAO collections: users, projects, quests; edges: user_collaborations, user_projects, user_quests

Current state (truthful)
- CommunityController endpoints exist. CommunityService returns mock/sample data for: feed, connections, collaboration, projects, stats, leaderboard. Repositories for collaborations/projects exist but may lack needed queries. Parameter mismatch exists in frontend service for sendConnectionRequest (expects toUserId, connectionType).

Target outcomes (Acceptance)
- All Community endpoints return persisted data, no hardcoded samples
- Connections lifecycle: create (PENDING), respond (ACCEPTED/DECLINED) updates edge; validations enforced
- Projects: list and create persisted; membership via user_projects edge
- Feed: recent events from user_quests (status changes), new projects, new connections
- Leaderboard/stats: aggregated via AQL (XP, completed quests), paginated
- Consistent response envelope ApiResponse { success, data, error?, pagination? }

API contracts (confirm and enforce)
- POST /api/community/connect
  - Body: { toUserId: string (users/{key}), connectionType: 'friend'|'mentor'|'collaborator' }
  - Returns: created connection edge with id/status=PENDING
- PUT /api/community/connect/{connectionId}
  - Body: { action: 'accept'|'decline' }
  - Returns: updated connection edge
- GET /api/community/connections?status=PENDING|ACCEPTED|DECLINED&direction=in|out|all&page=1&size=20
  - Returns: list with other user details and pagination
- GET /api/community/projects?mine=true|false&page=1&size=20
  - Returns: projects with member counts and roles
- POST /api/community/projects
  - Body: { title, description, tags?: string[], visibility?: 'public'|'private' }
  - Returns: created project and membership edge for creator (role='owner')
- GET /api/community/leaderboard?window=all|week|month&page=1&size=20
  - Returns: users with xp, completed counts, rank
- GET /api/community/stats
  - Returns: { users, activeUsers7d, projects, connections, questsCompleted }
- GET /api/community/feed?page=1&size=20
  - Returns: heterogeneous activity items, sorted by timestamp

Data model mapping
- user_collaborations (connections) edge: { _from: users/{id}, _to: users/{id}, type, status: 'PENDING'|'ACCEPTED'|'DECLINED', createdAt, updatedAt }
- user_projects edge: { _from: users/{id}, _to: projects/{id}, role: 'owner'|'member', createdAt }
- user_quests edge: { _from: users/{id}, _to: quests/{id}, status, xpEarned, completedAt?, updatedAt }

Indexes (ensure present)
- user_collaborations: persistent on [_from], [_to], [status], [createdAt]
- user_projects: persistent on [_from], [_to], [createdAt]
- user_quests: persistent on [_from], [status], [completedAt], [updatedAt]

Implementation plan (step-by-step)
1) Contracts and validation
- Standardize request DTOs for connect/create project/respond; validate presence and shape
- Return ApiResponse consistently in controller methods

2) Repositories: add AQL methods
- UserCollaborationRepository
  - createConnection(fromId, toId, type) -> edge with status=PENDING
  - findConnections(userId, {status?, direction?, page, size}) -> connections with other user docs
  - updateConnectionStatus(edgeId, status) -> updated edge (guard authorization)
  - counts(userId) -> totals by status
- UserProjectRepository
  - createProject(projectDoc) -> project
  - addMember(userId, projectId, role) -> user_projects edge
  - listProjects(userId?, mine?, page, size) -> projects with member counts
- UserQuestRepository
  - recentUserQuestEvents(userIdList?, window?) -> for feed aggregation

3) CommunityService: replace mocks
- getUserConnections: query repo with filters; map to DTO with other user profile fields (displayName, avatar)
- sendConnectionRequest: persist edge; prevent duplicates; self-connect guard; emit event record
- respondToConnectionRequest: validate requester vs recipient; update status; emit event
- getCommunityProjects / createCommunityProject: persist project and user_projects; return DTO with counts
- getCommunityLeaderboard: aggregate XP and completed quests
  - AQL sketch: FOR uq IN user_quests FILTER uq.status=='COMPLETED' AND withinWindow RETURN { user: uq._from, xp: SUM(uq.xpEarned), completed: COUNT(uq) } COLLECT user WITH SUM xp, SUM completed
- getCommunityStats: simple counts from collections and edges; recent activity window via user_quests
- getCommunityFeed: union of recent events
  - AQL sketch:
    - Completed quests: FOR uq IN user_quests FILTER uq.status=='COMPLETED' SORT uq.completedAt DESC LIMIT @n RETURN {type:'quest_completed', ts:uq.completedAt, user:uq._from, quest:uq._to, xp:uq.xpEarned}
    - New projects: FOR p IN projects SORT p.createdAt DESC LIMIT @n RETURN {type:'project_created', ts:p.createdAt, project:p._id, owner:p.ownerId}
    - New connections: FOR c IN user_collaborations FILTER c.status=='ACCEPTED' SORT c.updatedAt DESC LIMIT @n RETURN {type:'connection_accepted', ts:c.updatedAt, from:c._from, to:c._to}
    - MERGE/SORT/LIMIT for page/size; hydrate user/project/quest refs

4) Security and ownership checks
- Use email -> userId mapping from request to enforce: only recipient can accept/decline; only creator can modify own project (if needed)

5) Pagination and hydration
- Accept page/size; compute total when feasible; hydrate refs with additional queries or AQL subqueries (FETCH user displayName, avatar)

6) Controller wiring
- Replace return samples with service outputs; map to ApiResponse

7) Tests and smoke checks
- Add slice tests for service methods (mock repositories)
- Manual smoke: curl examples
  - POST /api/community/connect { "toUserId": "users/123", "connectionType":"friend" }
  - PUT /api/community/connect/{id} { "action":"accept" }
  - GET /api/community/connections?status=ACCEPTED
  - POST /api/community/projects { "title":"Alpha" }
  - GET /api/community/leaderboard

DTOs and shapes
- ConnectionDTO: { id, type, status, createdAt, otherUser: { id, displayName, avatarUrl } }
- ProjectDTO: { id, title, description, tags, visibility, createdAt, membersCount, owner: { id, displayName } }
- LeaderboardEntryDTO: { user: { id, displayName, avatarUrl }, xp, completed, rank }
- FeedItemDTO (discriminated union):
  - QuestCompleted: { type:'quest_completed', ts, user, quest, xp }
  - ProjectCreated: { type:'project_created', ts, project, owner }
  - ConnectionAccepted: { type:'connection_accepted', ts, from, to }

Error semantics
- 400 for validation errors (missing fields, bad direction/status)
- 403 for unauthorized respond attempts
- 404 for missing edge/project
- 409 for duplicate connection request

Performance notes
- Use LIMIT/OFFSET with stable SORT by timestamp
- Prefer AQL subqueries to hydrate display fields; avoid N+1
- Review indexes and use AQL EXPLAIN on heavy queries

Checklist (execute in order)
- [ ] Confirm/adjust controller DTOs to contracts above
- [ ] Implement UserCollaborationRepository queries
- [ ] Implement UserProjectRepository queries
- [ ] Add leaderboard/stats AQL in CommunityService
- [ ] Replace feed mocks with union query and hydration
- [ ] Enforce validation and ownership checks
- [ ] Return ApiResponse consistently
- [ ] Add minimal tests and manual curl smoke scripts

Deliverable
- PR: "feat(community): replace mocks with Arango-backed queries + contracts aligned"
- Includes repository/query changes, service logic, controller responses, tests, and brief docs in ROADMAP.md
