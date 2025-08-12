# Syntopia Project - Sacred Geometry Platform

## 🌸 Overview
Syntopia is a consciousness platform that explores the divine mathematics of creation through interactive sacred geometry visualizations, gamified learning, and conscious collaboration.

## 🏗️ Project Structure
```
syntopia/
├── frontend/          # Vue.js 3 + TypeScript frontend
├── backend/           # Spring Boot 3 + Java 21 backend  
├── database/          # ArangoDB with TAO architecture
├── kubernetes/        # K8s deployment manifests
├── .github/           # CI/CD workflows
└── docker-compose.yml # Local development setup
```

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- Java 21
- Docker & Docker Compose
- ArangoDB 7.8+

### Development Setup
```bash
# Clone repository
git clone <repo-url>
cd syntopia

# Start infrastructure
docker-compose up -d

# Frontend development
cd frontend
npm install
npm run dev

# Backend development
cd backend
./mvnw spring-boot:run
```

## 🎯 Current Status
- ✅ Frontend: Vue.js 3 with Glassmorphism design
- ✅ Backend: Spring Boot structure with ArangoDB
- ✅ Views: Home, Sacred Geometry Explorer, Quest System
- ✅ Authentication: JWT + GitHub OAuth ready
- ✅ Roadmap: Comprehensive 2025-2027 development plan

## 🌟 Key Features
- **Sacred Geometry Visualizations**: P5.js powered interactive patterns
- **Gamified Learning**: 25-level progression system with quests
- **GitHub Integration**: Turn open source contributions into quests
- **7 Sacred Roles**: Unique paths through the Syntopia journey
- **Community Platform**: Connect with consciousness explorers

## 🔮 Sacred Geometry Design Philosophy
All interface elements follow sacred geometry principles:
- Golden Ratio (φ = 1.618) proportions in layouts
- Fibonacci sequence spacing (8px, 13px, 21px, 34px, 55px)
- Circular and spiral animations
- Frequency-based color harmonies

## 📱 Mobile-First & PWA
- Progressive Web App with offline capabilities
- Native app wrapper with Capacitor.js
- Touch-first interactions and gestures
- Core Web Vitals optimized performance

## 🌍 Internationalization
- Primary: German & English
- Planned: 10+ languages with cultural adaptation
- Vue i18n with dynamic loading
- Sacred geometry from diverse traditions

## 🔗 Links
- [Roadmap](./ROADMAP.md) - Detailed development plan
- [Frontend](./frontend/README.md) - Vue.js application
- [Backend](./backend/README.md) - Spring Boot API
- [Database](./database/README.md) - ArangoDB setup

## API responses and errors

All backend endpoints use a unified ApiResponse envelope:

- Success with data:
	{ "success": true, "data": { ... } }
- Success with message and data:
	{ "success": true, "message": "Message", "data": { ... } }
- Error:
	{ "success": false, "error": "Reason" }
- Error with details:
	{ "success": false, "error": "Reason", "details": { ... } }
- Paginated list:
	{ "success": true, "data": [ ... ], "pagination": { "page": 0, "size": 20, "total": 123, "totalPages": 7, "hasNext": true, "hasPrev": false } }

Validation errors (400) are returned as:
{ "success": false, "error": "Validation failed", "details": { "validationErrors": { "fieldA": "must not be blank" } } }

Unauthorized (401) responses (controller-thrown or security filter entry point):
{ "success": false, "error": "Authentication required" }

Centralized handling is implemented via GlobalExceptionHandler.

## 📄 License
MIT License - See [LICENSE](./LICENSE) for details

---

*"As above, so below - let the patterns of sacred geometry guide our technological creation."*
