# 🗄️ Syntopia Quest Database Architecture

## 📊 **Übersicht: Quest & User Relationship System**

Die Quest-DB ist jetzt komplett für Multi-User-Support ausgelegt mit individueller Fortschrittsverfolgung!

## 🏗️ **Database Collections & Edges**

### **1. Quest Collection** (`quests`)
```
Quest Document:
├── Basic Info (id, title, description, role, requiredLevel)
├── Rewards (experienceReward)
├── Type & Difficulty (type, difficulty, status)
├── GitHub Integration (githubIssueUrl, githubRepository)
├── Sacred Geometry (geometryPatterns, geometryData)
├── Quest Category & Reusability ⭐
│   ├── category: INDIVIDUAL | SHARED | UNIQUE
│   ├── isReusable: boolean
│   ├── maxCompletions: int (0 = unlimited)
│   └── currentCompletions: int
└── Metadata (flexible quest data)
```

### **2. User Collection** (`users`)
```
User Document:
├── Basic Info (id, username, email)
├── Profile (selectedRole, currentLevel)
├── Progress (experiencePoints, completedQuestCount)
└── Settings & Preferences
```

### **3. UserQuest Edge Collection** (`user_quests`) ⭐
```
UserQuest Edge:
├── Relationship (_from: user, _to: quest)
├── Individual Status & Progress
│   ├── status: USER_AVAILABLE | USER_ACTIVE | USER_COMPLETED | USER_ABANDONED
│   ├── progress: 0-100%
│   └── progressData: Map<String, Object>
├── Timestamps
│   ├── acceptedAt, startedAt, completedAt, abandonedAt
│   └── lastProgressUpdate
├── Completion Details
│   ├── experienceAwarded: long
│   ├── completionNotes: String
│   └── completionData: Map<String, Object>
└── GitHub Integration (githubPullRequestUrl, githubCommitSha, isVerified)
```

## 🎯 **Quest-Kategorien & Multi-User-Support**

### **INDIVIDUAL Quests** (Onboarding)
```
✅ Jeder User bekommt eigene Instanz
✅ Mehrere User können dieselbe Quest machen
✅ Individuelle Fortschrittsverfolgung
✅ Keine Completion-Limits

Beispiel: "Tech Development Level 1 Onboarding"
- category: INDIVIDUAL
- isReusable: true
- maxCompletions: 0 (unlimited)
```

### **SHARED Quests** (Learning/Community)
```
✅ Mehrere User können teilnehmen
✅ Individueller Fortschritt pro User
✅ Gemeinsame Ressourcen/Inhalte
✅ Flexible Completion-Limits

Beispiel: "Sacred Geometry Fundamentals Study"
- category: SHARED  
- isReusable: true
- maxCompletions: 100 (max 100 completions)
```

### **UNIQUE Quests** (GitHub Issues)
```
✅ Nur ein User kann abschließen
✅ Real-world Impact (GitHub PR)
✅ Nach Completion archiviert
✅ Strict Completion-Limit

Beispiel: "Fix Bug #123 in Syntopia Frontend"
- category: UNIQUE
- isReusable: false
- maxCompletions: 1
```

## 🔄 **Quest-Status-Management**

### **Quest-Level Status** (Quest Collection)
```
AVAILABLE  → Quest ist verfügbar
ACTIVE     → Mindestens ein User arbeitet daran
COMPLETED  → Quest erreichte maxCompletions
ARCHIVED   → Quest nicht mehr verfügbar
SUSPENDED  → Temporär deaktiviert
```

### **User-Level Status** (UserQuest Edge)
```
USER_AVAILABLE → User kann Quest starten
USER_ACTIVE    → User arbeitet an Quest
USER_COMPLETED → User hat Quest abgeschlossen
USER_ABANDONED → User hat Quest abgebrochen
USER_VERIFIED  → Quest-Completion verifiziert (GitHub)
```

## 📈 **Beispiel-Szenarien**

### **Szenario 1: Onboarding Quest**
```
1. Quest "Tech Dev Level 1" wird erstellt:
   - category: INDIVIDUAL
   - isReusable: true
   - maxCompletions: 0

2. User A startet Quest:
   - UserQuest Edge: userA → quest (USER_ACTIVE)
   
3. User B startet dieselbe Quest:
   - UserQuest Edge: userB → quest (USER_ACTIVE)
   
4. User A schließt ab:
   - UserQuest userA→quest: USER_COMPLETED
   - Quest.currentCompletions++
   
5. User B schließt ab:
   - UserQuest userB→quest: USER_COMPLETED  
   - Quest.currentCompletions++

✅ Beide User haben individuell completed
✅ Quest bleibt verfügbar für weitere User
```

### **Szenario 2: GitHub Issue Quest**
```
1. GitHub Issue Quest wird erstellt:
   - category: UNIQUE
   - isReusable: false
   - maxCompletions: 1

2. User A startet Quest:
   - UserQuest Edge: userA → quest (USER_ACTIVE)
   
3. User B versucht zu starten:
   - ❌ Fehlschlag: Quest bereits von User A belegt
   
4. User A schließt ab mit GitHub PR:
   - UserQuest userA→quest: USER_COMPLETED
   - Quest.status: COMPLETED (limit reached)
   - Quest nicht mehr verfügbar

✅ Nur ein User konnte die GitHub Quest abschließen
✅ Real-world Impact durch GitHub PR
```

## 🔍 **Database Queries**

### **User-spezifische Queries**
```java
// Alle Quests eines Users
userQuestRepository.findByUserId(userId)

// User's aktive Quests
userQuestRepository.findActiveQuestsByUser(userId)

// User's abgeschlossene Quests
userQuestRepository.findCompletedQuestsByUser(userId)

// User Quest Statistics
questService.getUserQuestStatistics(userId)
```

### **Quest-spezifische Queries**
```java
// Alle User die an Quest arbeiten
userQuestRepository.findUsersWorkingOnQuest(questId)

// Alle User die Quest abgeschlossen haben
userQuestRepository.findUsersWhoCompletedQuest(questId)

// Quest Completion Count
quest.getCurrentCompletions()
```

## 🚀 **API Endpoints (Erweitert)**

### **User Quest Management**
```
POST /api/quests/{questId}/accept
→ questService.acceptUserQuest(userId, questId)

POST /api/quests/{questId}/complete  
→ questService.completeUserQuest(userId, questId, completionData)

POST /api/quests/{questId}/abandon
→ questService.abandonUserQuest(userId, questId)

GET /api/users/{userId}/quests
→ questService.getAvailableQuestsForUserWithProgress(userId)
```

### **Quest Analytics**
```
GET /api/users/{userId}/quest-stats
→ questService.getUserQuestStatistics(userId)

GET /api/quests/{questId}/participants
→ userQuestRepository.findByQuestId(questId)
```

## 💾 **Migration Plan**

### **Phase 1: Structure Update**
```
✅ UserQuest Model & Repository erstellt
✅ Quest Model erweitert (category, reusability)
✅ QuestService erweitert (UserQuest-Management)
```

### **Phase 2: Data Migration**
```
📝 Bestehende Quest-Data migrieren
📝 UserQuest Edges für existierende Completions erstellen
📝 Quest-Kategorien setzen (INDIVIDUAL/SHARED/UNIQUE)
```

### **Phase 3: Frontend Integration**
```
📝 Frontend APIs auf UserQuest-System umstellen
📝 Individual Progress Tracking in UI
📝 Multi-User Quest Visualization
```

---

**🎯 Ergebnis:** Das Quest-System unterstützt jetzt vollständig:
- ✅ Multiple User pro Quest (Onboarding)  
- ✅ Individual Progress Tracking
- ✅ Unique Quests (GitHub Issues)
- ✅ Flexible Quest Categories
- ✅ Comprehensive Analytics
- ✅ Real-world Impact Tracking
