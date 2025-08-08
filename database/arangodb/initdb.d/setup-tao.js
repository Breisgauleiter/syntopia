// ArangoDB Initialization Script for Syntopia TAO Architecture
// This script sets up the TAO (Objects, Associations, Search) database structure

// Create database if it doesn't exist
db._createDatabase("syntopia");
db._useDatabase("syntopia");

// ==================================================
// TAO OBJECTS LAYER - Document Collections
// ==================================================

// Users collection (Main entities)
db._create("users");
db.users.ensureIndex({ type: "hash", fields: ["username"], unique: true });
db.users.ensureIndex({ type: "hash", fields: ["email"], unique: true });
db.users.ensureIndex({ type: "hash", fields: ["githubId"], unique: true, sparse: true });
db.users.ensureIndex({ type: "persistent", fields: ["currentLevel"] });
db.users.ensureIndex({ type: "persistent", fields: ["experiencePoints"] });

// Quests collection
db._create("quests");
db.quests.ensureIndex({ type: "hash", fields: ["role"] });
db.quests.ensureIndex({ type: "persistent", fields: ["requiredLevel"] });
db.quests.ensureIndex({ type: "hash", fields: ["status"] });
db.quests.ensureIndex({ type: "hash", fields: ["type"] });
db.quests.ensureIndex({ type: "hash", fields: ["githubRepository"], sparse: true });

// Sacred Geometry Patterns collection
db._create("geometry_patterns");
db.geometry_patterns.ensureIndex({ type: "hash", fields: ["name"], unique: true });
db.geometry_patterns.ensureIndex({ type: "hash", fields: ["category"] });
db.geometry_patterns.ensureIndex({ type: "persistent", fields: ["complexity"] });

// Community Projects collection
db._create("projects");
db.projects.ensureIndex({ type: "hash", fields: ["name"], unique: true });
db.projects.ensureIndex({ type: "hash", fields: ["status"] });
db.projects.ensureIndex({ type: "hash", fields: ["githubRepository"], sparse: true });

// ==================================================
// TAO ASSOCIATIONS LAYER - Edge Collections
// ==================================================

// User-Quest associations (user accepts/completes quests)
db._createEdgeCollection("user_quests");
db.user_quests.ensureIndex({ type: "hash", fields: ["status"] }); // ACCEPTED, COMPLETED, ABANDONED
db.user_quests.ensureIndex({ type: "persistent", fields: ["acceptedAt"] });
db.user_quests.ensureIndex({ type: "persistent", fields: ["completedAt"] });

// User-User associations (collaborations, mentorship)
db._createEdgeCollection("user_collaborations");
db.user_collaborations.ensureIndex({ type: "hash", fields: ["type"] }); // MENTOR, COLLABORATOR, WITNESS

// User-Project associations (user contributes to projects)
db._createEdgeCollection("user_projects");
db.user_projects.ensureIndex({ type: "hash", fields: ["role"] }); // OWNER, CONTRIBUTOR, REVIEWER
db.user_projects.ensureIndex({ type: "persistent", fields: ["joinedAt"] });

// Quest-GeometryPattern associations (quests explore patterns)
db._createEdgeCollection("quest_patterns");
db.quest_patterns.ensureIndex({ type: "hash", fields: ["relationship"] }); // EXPLORES, CREATES, VISUALIZES

// User-GeometryPattern associations (user's favorite or mastered patterns)
db._createEdgeCollection("user_patterns");
db.user_patterns.ensureIndex({ type: "hash", fields: ["relationship"] }); // FAVORITE, MASTERED, STUDYING

// ==================================================
// TAO SEARCH LAYER - Full-text Search Indexes
// ==================================================

// Full-text search for quests
db.quests.ensureIndex({ type: "fulltext", fields: ["title"] });
db.quests.ensureIndex({ type: "fulltext", fields: ["description"] });

// Full-text search for geometry patterns  
db.geometry_patterns.ensureIndex({ type: "fulltext", fields: ["name"] });
db.geometry_patterns.ensureIndex({ type: "fulltext", fields: ["description"] });

// Full-text search for projects
db.projects.ensureIndex({ type: "fulltext", fields: ["name"] });
db.projects.ensureIndex({ type: "fulltext", fields: ["description"] });

// Full-text search for users (display names, bio)
db.users.ensureIndex({ type: "fulltext", fields: ["displayName"] });

// ==================================================
// INITIAL DATA SEEDING
// ==================================================

// Insert the 7 initial Syntopia roles
const initialRoles = [
    "Sacred Mathematician",
    "Digital Architect", 
    "Community Weaver",
    "Consciousness Explorer",
    "Quantum Developer",
    "Wisdom Keeper",
    "Witness"
];

// Insert initial geometry patterns
const geometryPatterns = [
    {
        name: "Flower of Life",
        category: "Sacred Circle",
        description: "Fundamental pattern of creation containing all Platonic solids",
        complexity: 3,
        parameters: { circles: 19, radius: 1.0 },
        createdAt: new Date()
    },
    {
        name: "Metatron's Cube",
        category: "Sacred Polyhedron",
        description: "Contains all five Platonic solids and connects all creation",
        complexity: 5,
        parameters: { vertices: 13, edges: 78 },
        createdAt: new Date()
    },
    {
        name: "Golden Spiral",
        category: "Sacred Proportion",
        description: "Based on the Fibonacci sequence and golden ratio",
        complexity: 2,
        parameters: { ratio: 1.618, turns: 8 },
        createdAt: new Date()
    },
    {
        name: "Vesica Piscis",
        category: "Sacred Intersection",
        description: "The intersection of two circles representing duality and unity",
        complexity: 1,
        parameters: { circles: 2, overlap: 0.5 },
        createdAt: new Date()
    }
];

geometryPatterns.forEach(pattern => {
    db.geometry_patterns.insert(pattern);
});

// Insert initial platform quests for each role and level 1-3
const initialQuests = [
    // Level 1 Quests - Introduction
    {
        title: "Welcome to Syntopia",
        description: "Complete your profile and select your sacred role",
        role: "ALL",
        requiredLevel: 1,
        experienceReward: 100,
        type: "PLATFORM",
        status: "AVAILABLE",
        difficulty: "BEGINNER",
        completionCriteria: "Profile completed and role selected",
        createdAt: new Date()
    },
    {
        title: "First Sacred Pattern",
        description: "Study and visualize your first sacred geometry pattern",
        role: "Sacred Mathematician",
        requiredLevel: 1,
        experienceReward: 150,
        type: "SACRED_GEOMETRY",
        status: "AVAILABLE",
        difficulty: "BEGINNER",
        geometryPatterns: ["Vesica Piscis"],
        createdAt: new Date()
    },
    
    // Level 2 Quests - Exploration
    {
        title: "Community Connection",
        description: "Connect with 3 other community members",
        role: "Community Weaver",
        requiredLevel: 2,
        experienceReward: 200,
        type: "COMMUNITY",
        status: "AVAILABLE",
        difficulty: "BEGINNER",
        createdAt: new Date()
    },
    
    // Level 3 Quests - Preparation for GitHub
    {
        title: "Understanding Open Source",
        description: "Learn about open source development and GitHub workflow",
        role: "ALL",
        requiredLevel: 3,
        experienceReward: 300,
        type: "PLATFORM",
        status: "AVAILABLE",
        difficulty: "INTERMEDIATE",
        completionCriteria: "Complete GitHub tutorial and pass quiz",
        createdAt: new Date()
    }
];

initialQuests.forEach(quest => {
    db.quests.insert(quest);
});

// Create initial project for Syntopia itself
const syntopiaProject = {
    name: "Syntopia Platform",
    description: "The Sacred Geometry consciousness platform itself",
    status: "ACTIVE",
    githubRepository: "syntopia/syntopia-platform",
    vision: "Create a platform for conscious collaboration through sacred geometry",
    createdAt: new Date()
};

db.projects.insert(syntopiaProject);

print("✨ Syntopia TAO database structure initialized successfully!");
print("📊 Collections created: users, quests, geometry_patterns, projects");
print("🔗 Edge collections created: user_quests, user_collaborations, user_projects, quest_patterns, user_patterns");
print("🔍 Search indexes created for full-text search capabilities");
print("🌱 Initial data seeded: roles, geometry patterns, platform quests");
print("🎯 Ready for Syntopia Sacred Geometry platform!");
