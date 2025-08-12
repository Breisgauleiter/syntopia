/**
 * Quest System Frontend Integration Test
 * 
 * Diese Datei testet die Integration zwischen dem Quest Service und der QuestsView.vue
 * Verwenden Sie sie für manuelle Tests der Quest-Funktionalität
 */

import questService from '../services/quest.service'
import { useUserStore } from '../stores/user'

// Teste Quest Service Funktionen
export const testQuestService = {
  
  /**
   * Teste alle Quests abrufen
   */
  async testGetAllQuests() {
    console.log('🧪 Testing getAllQuests...')
    
    try {
      const response = await questService.getQuests()
      console.log('✅ getAllQuests successful:', response)
      return response
    } catch (error) {
      console.error('❌ getAllQuests failed:', error)
      return null
    }
  },

  /**
   * Teste verfügbare Quests für User abrufen
   */
  async testGetAvailableQuests(userId: string) {
    console.log('🧪 Testing getAvailableQuests for user:', userId)
    
    try {
      const response = await questService.getUserQuests()
      if (response.success) {
        const count = response.data?.data?.length || 0
        console.log(`✅ getAvailableQuests successful: ${count} entries (page ${response.data?.pagination?.page})`)
      } else {
        console.warn('⚠️ getAvailableQuests returned unsuccessful response', response)
      }
      return response
    } catch (error) {
      console.error('❌ getAvailableQuests failed:', error)
      return null
    }
  },

  /**
   * Teste Quest akzeptieren
   */
  async testAcceptQuest(questId: string, userId: string) {
    console.log('🧪 Testing acceptQuest:', { questId, userId })
    
    try {
      const response = await questService.acceptQuest(questId)
      console.log('✅ acceptQuest successful:', response)
      return response
    } catch (error) {
      console.error('❌ acceptQuest failed:', error)
      return null
    }
  },

  /**
   * Teste Quest abschließen
   */
  async testCompleteQuest(questId: string, userId: string) {
    console.log('🧪 Testing completeQuest:', { questId, userId })
    
    try {
      const response = await questService.completeQuest(questId)
      console.log('✅ completeQuest successful:', response)
      return response
    } catch (error) {
      console.error('❌ completeQuest failed:', error)
      return null
    }
  },

  /**
   * Teste Quest Statistiken
   */
  async testGetQuestStats() {
    console.log('🧪 Testing getQuestStats...')
    try {
      const response = await questService.getUserQuestStatistics()
      if (response.success) {
        console.log('✅ getQuestStats successful:', response.data)
      } else {
        console.warn('⚠️ getQuestStats unsuccessful:', response)
      }
      return response
    } catch (error) {
      console.error('❌ getQuestStats failed:', error)
      return null
    }
  },

  /**
   * Vollständiger Test-Flow
   */
  async runFullTestFlow() {
    console.log('🚀 Starting full Quest System test flow...')
    
    const userStore = useUserStore()
    const userId = userStore.user?.id

    if (!userId) {
      console.error('❌ No user logged in, cannot run full test flow')
      return
    }

    // 1. Lade alle Quests
    const allQuests = await this.testGetAllQuests()
    if (!allQuests?.success) return

    // 2. Lade verfügbare Quests
    const availableQuests = await this.testGetAvailableQuests(userId)
    if (!availableQuests?.success) return

    // 3. Teste Quest Statistiken
    await this.testGetQuestStats()

    // 4. Simuliere Quest akzeptieren (falls verfügbare Quests vorhanden)
    if (availableQuests.data && Array.isArray(availableQuests.data.data) && availableQuests.data.data.length > 0) {
      const firstQuest = availableQuests.data.data[0]
      console.log('🎯 Found available quest for testing:', firstQuest.questId || firstQuest.quest?.id)
      
      // Akzeptiere Quest
      const questId = (firstQuest.questId || firstQuest.quest?.id)
      if (!questId) {
        console.warn('⚠️ Could not determine questId for first quest, skipping accept test')
      } else {
        const acceptResult = await this.testAcceptQuest(questId, userId)
        if (acceptResult?.success) {
          console.log('🎉 Quest acceptance successful! You can now test completion in the UI.')
        }
      }
    } else {
      console.log('📝 No available quests found for testing')
    }

    console.log('✅ Full test flow completed!')
  }
}

// Exportiere Test-Utilities für Browser Console
if (typeof window !== 'undefined') {
  (window as any).testQuestSystem = testQuestService
  console.log('🔧 Quest System tests available via window.testQuestSystem')
  console.log('📖 Usage examples:')
  console.log('   window.testQuestSystem.testGetAllQuests()')
  console.log('   window.testQuestSystem.runFullTestFlow()')
}

export default testQuestService
