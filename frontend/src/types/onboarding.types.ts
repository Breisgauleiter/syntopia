// TypeScript type definitions for onboarding system
export interface OnboardingQuest {
  id: string
  title: string
  description: string
  level: number
  xpReward: number
  synPrinciple: string
  roleDialect: string
  steps: string[]
  iconType: string
  profileRequired: boolean
  metadata: QuestMetadata
}

export interface QuestMetadata {
  animationType: string
  principleIntro: string
  uiFeatures?: string[]
  synPrinciplesTest?: boolean
  roleAvatar?: boolean
  communityFeatures?: string[]
  githubIntegration?: boolean
  questSynchronization?: boolean
}

export interface SynPrinciple {
  level: number
  name: string
  subtitle: string
  description: string
}

export interface ExperienceLevel {
  level: number
  xpRequired: number
  title: string
  description: string
}

export interface OnboardingProgress {
  userId: string
  currentLevel: number
  currentXP: number
  completedQuests: string[]
  selectedRole: string | null
  isGitHubConnected: boolean
  synPrinciplesUnderstood: string[]
  onboardingStartedAt: Date
  lastActivityAt: Date
}

export interface QuestCompletionResult {
  questId: string
  xpAwarded: number
  newLevel: number
  newTotalXP: number
  isLevelUp: boolean
  unlockedFeatures: string[]
  completedAt: Date
}

export interface SacredRole {
  name: string
  dialect: string
  description: string
  iconType: string
  color: string
  characteristics: string[]
}

export interface QuestStep {
  id: string
  title: string
  description: string
  isCompleted: boolean
  isOptional: boolean
  requirements?: string[]
  hints?: string[]
}

export interface OnboardingState {
  isLoading: boolean
  currentQuest: OnboardingQuest | null
  availableQuests: OnboardingQuest[]
  progress: OnboardingProgress | null
  selectedRole: SacredRole | null
  synPrinciples: SynPrinciple[]
  experienceLevels: ExperienceLevel[]
  error: string | null
}

export interface GitHubIntegration {
  isConnected: boolean
  username: string | null
  repositories: GitHubRepository[]
  availableQuests: GitHubQuest[]
  lastSyncAt: Date | null
}

export interface GitHubRepository {
  id: string
  name: string
  fullName: string
  description: string
  language: string
  stars: number
  isForked: boolean
  syncedQuests: string[]
}

export interface GitHubQuest {
  id: string
  title: string
  repositoryName: string
  issueNumber: number
  labels: string[]
  difficulty: 'easy' | 'medium' | 'hard'
  xpReward: number
  roleAlignment: string[]
  synPrincipleAlignment: string
}

// Sacred Geometry Animation Types
export interface SacredGeometryAnimation {
  type: 'emergence' | 'transformation' | 'network' | 'synchronicity'
  duration: number
  complexity: 'simple' | 'medium' | 'complex'
  colors: string[]
  interactive: boolean
}

// Quest UI Component Props
export interface QuestCardProps {
  quest: OnboardingQuest
  isActive: boolean
  isCompleted: boolean
  isLocked: boolean
  onStart: () => void
  onContinue: () => void
  onComplete: () => void
}

export interface QuestProgressProps {
  progress: OnboardingProgress
  experienceLevels: ExperienceLevel[]
  showDetails: boolean
}

export interface RoleSelectionProps {
  availableRoles: SacredRole[]
  selectedRole: SacredRole | null
  onRoleSelect: (role: SacredRole) => void
  showDescriptions: boolean
}

export interface SynPrincipleCardProps {
  principle: SynPrinciple
  isUnlocked: boolean
  isStudied: boolean
  onStudy: () => void
  animationType: SacredGeometryAnimation
}

// API Response Types
export interface OnboardingApiResponse<T> {
  data: T
  message: string
  success: boolean
  timestamp: Date
}

export interface QuestValidationResult {
  isValid: boolean
  completedSteps: string[]
  missingSteps: string[]
  nextAction: string
  canProceed: boolean
}

// Store/State Management Types
export interface OnboardingStore {
  state: OnboardingState
  actions: {
    loadAvailableQuests: () => Promise<void>
    selectRole: (role: SacredRole) => Promise<void>
    startQuest: (questId: string) => Promise<void>
    completeQuestStep: (questId: string, stepId: string) => Promise<void>
    completeQuest: (questId: string) => Promise<QuestCompletionResult>
    updateProgress: (progress: Partial<OnboardingProgress>) => Promise<void>
    connectGitHub: () => Promise<void>
    syncGitHubQuests: () => Promise<void>
    studySynPrinciple: (principleLevel: number) => Promise<void>
    resetOnboarding: () => Promise<void>
  }
}

// Constants
export const SACRED_ROLES: SacredRole[] = [
  {
    name: 'Tech Development',
    dialect: 'Codesmith',
    description: 'Sacred architects of conscious technology systems',
    iconType: 'code-sacred',
    color: '#00D4FF',
    characteristics: ['Conscious Coding', 'Sacred Architecture', 'Open Source Wisdom']
  },
  {
    name: 'Business Development', 
    dialect: 'Visionary',
    description: 'Conscious entrepreneurs bridging innovation and sustainability',
    iconType: 'vision-sacred',
    color: '#FF6B35', 
    characteristics: ['Conscious Entrepreneurship', 'Regenerative Business', 'Innovation Leadership']
  },
  {
    name: 'UX Design',
    dialect: 'Creator', 
    description: 'Sacred interface designers expanding user consciousness',
    iconType: 'design-sacred',
    color: '#8B5CF6',
    characteristics: ['Sacred Interface Design', 'Consciousness UI/UX', 'Creative Transformation']
  },
  {
    name: 'Data Science',
    dialect: 'Analyst',
    description: 'Ethical data scientists transforming information into wisdom',
    iconType: 'data-sacred',
    color: '#10B981',
    characteristics: ['Ethical Analytics', 'Privacy-First Data', 'Conscious AI']
  },
  {
    name: 'Legal Advisory',
    dialect: 'Guardian',
    description: 'Legal innovators bridging traditional law and conscious governance',
    iconType: 'justice-sacred', 
    color: '#F59E0B',
    characteristics: ['Ethical Governance', 'Legal Innovation', 'Justice Architecture']
  },
  {
    name: 'Finance Analysis',
    dialect: 'Steward',
    description: 'Conscious finance experts building regenerative economic systems',
    iconType: 'finance-sacred',
    color: '#EF4444',
    characteristics: ['Conscious Capital', 'Regenerative Economics', 'Impact Investment']
  },
  {
    name: 'Sustainability Lead',
    dialect: 'Planetary Steward',
    description: 'Environmental guardians integrating ecological wisdom into all systems',
    iconType: 'sustainability-sacred',
    color: '#059669', 
    characteristics: ['Planetary Stewardship', 'Regenerative Systems', 'Ecological Innovation']
  }
]

export const SYN_PRINCIPLES: SynPrinciple[] = [
  {
    level: 1,
    name: 'Syntropie',
    subtitle: 'Harmonische Ordnung durch Bewusstsein',
    description: 'Die Kraft der bewussten Struktur und organischen Organisation für maximale Harmonie.'
  },
  {
    level: 2, 
    name: 'Synthese',
    subtitle: 'Evolution von Idee zu Manifestation',
    description: 'Der Prozess der Transformation und Integration für kontinuierliche Evolution.'
  },
  {
    level: 3,
    name: 'Synarchie', 
    subtitle: 'Netzwerk aus Netzwerken',
    description: 'Organische Führung und kollaborative Governance für dezentrale Innovation.'
  },
  {
    level: 4,
    name: 'Synchronizität',
    subtitle: 'Intelligentes Purpose-Matching & AI-Consciousness', 
    description: 'Bedeutungsvolle Verbindungen und bewusste Technologie für positive Transformation.'
  }
]

export const FIBONACCI_XP_LEVELS: ExperienceLevel[] = [
  {
    level: 1,
    xpRequired: 100,
    title: 'Profile Genesis',
    description: 'Sacred Profile Setup & Role Selection'
  },
  {
    level: 2,
    xpRequired: 200, 
    title: 'Principle Integration',
    description: 'SYN-Principles Mastery & Avatar Creation'
  },
  {
    level: 3,
    xpRequired: 300,
    title: 'Community Formation', 
    description: 'Network Building & Collaboration'
  },
  {
    level: 4,
    xpRequired: 500,
    title: 'Synchronicity Activation',
    description: 'GitHub Integration & Quest Synchronization'
  }
]
