package com.syntopia.service;

import com.syntopia.model.OnboardingQuest;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for generating role-specific onboarding quests for levels 1-4
 * Each quest embodies one of the 4 SYN principles: Syntropie, Synthese, Synarchie, Synchronizität
 */
@Service
public class OnboardingQuestGenerator {
    
    private static final String[] ROLES = {
        "Tech Development", "Business Development", "UX Design", "Data Science",
        "Legal Advisory", "Finance Analysis", "Sustainability Lead"
    };
    
    private static final String[] SYN_PRINCIPLES = {
        "Syntropie", "Synthese", "Synarchie", "Synchronizität"
    };
    
    private static final int[] FIBONACCI_XP = {100, 200, 300, 500};
    
    /**
     * Generate all onboarding quests for all roles and levels 1-4
     */
    public List<OnboardingQuest> generateAllOnboardingQuests() {
        List<OnboardingQuest> quests = new ArrayList<>();
        
        for (String role : ROLES) {
            for (int level = 1; level <= 4; level++) {
                quests.add(generateQuestForRoleAndLevel(role, level));
            }
        }
        
        return quests;
    }
    
    /**
     * Generate a specific quest for a role and level
     */
    public OnboardingQuest generateQuestForRoleAndLevel(String role, int level) {
        OnboardingQuest quest = new OnboardingQuest();
        quest.setId(generateQuestId(role, level));
        quest.setLevel(level);
        quest.setXpReward(FIBONACCI_XP[level - 1]);
        quest.setSynPrinciple(SYN_PRINCIPLES[level - 1]);
        quest.setRoleDialect(getRoleDialect(role));
        
        switch (level) {
            case 1:
                return generateLevel1Quest(quest, role); // Syntropie - Profile Setup
            case 2:
                return generateLevel2Quest(quest, role); // Synthese - Role Avatar & Principles
            case 3:
                return generateLevel3Quest(quest, role); // Synarchie - Community Engagement
            case 4:
                return generateLevel4Quest(quest, role); // Synchronizität - GitHub Integration
            default:
                throw new IllegalArgumentException("Invalid level: " + level);
        }
    }
    
    /**
     * Level 1: SYNTROPIE - Setting up harmonic profile order
     */
    private OnboardingQuest generateLevel1Quest(OnboardingQuest quest, String role) {
        quest.setTitle(getLevel1Title(role));
        quest.setDescription(getLevel1Description(role));
        quest.setSteps(getLevel1Steps(role));
        quest.setIconType("profile-setup");
        quest.setProfileRequired(true);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("animationType", "sacred-geometry-emergence");
        metadata.put("principleIntro", "Syntropie: Harmonische Ordnung durch Bewusstsein");
        metadata.put("uiFeatures", Arrays.asList("profile-form", "role-selection", "avatar-upload"));
        quest.setMetadata(metadata);
        
        return quest;
    }
    
    /**
     * Level 2: SYNTHESE - Evolution and role-specific transformation
     */
    private OnboardingQuest generateLevel2Quest(OnboardingQuest quest, String role) {
        quest.setTitle(getLevel2Title(role));
        quest.setDescription(getLevel2Description(role));
        quest.setSteps(getLevel2Steps(role));
        quest.setIconType("synthesis-evolution");
        quest.setProfileRequired(false);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("animationType", "transformation-spiral");
        metadata.put("principleIntro", "Synthese: Evolution von Idee zu Manifestation");
        metadata.put("synPrinciplesTest", true);
        metadata.put("roleAvatar", true);
        quest.setMetadata(metadata);
        
        return quest;
    }
    
    /**
     * Level 3: SYNARCHIE - Network building and organic leadership
     */
    private OnboardingQuest generateLevel3Quest(OnboardingQuest quest, String role) {
        quest.setTitle(getLevel3Title(role));
        quest.setDescription(getLevel3Description(role));
        quest.setSteps(getLevel3Steps(role));
        quest.setIconType("network-synarchy");
        quest.setProfileRequired(false);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("animationType", "network-formation");
        metadata.put("principleIntro", "Synarchie: Netzwerk aus Netzwerken");
        metadata.put("communityFeatures", Arrays.asList("first-post", "user-discovery", "connections"));
        quest.setMetadata(metadata);
        
        return quest;
    }
    
    /**
     * Level 4: SYNCHRONIZITÄT - Meaningful connections through GitHub integration
     */
    private OnboardingQuest generateLevel4Quest(OnboardingQuest quest, String role) {
        quest.setTitle(getLevel4Title(role));
        quest.setDescription(getLevel4Description(role));
        quest.setSteps(getLevel4Steps(role));
        quest.setIconType("synchronicity-github");
        quest.setProfileRequired(false);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("animationType", "synchronicity-waves");
        metadata.put("principleIntro", "Synchronizität: Intelligentes Purpose-Matching");
        metadata.put("githubIntegration", true);
        metadata.put("questSynchronization", true);
        quest.setMetadata(metadata);
        
        return quest;
    }
    
    // === LEVEL 1 QUEST CONTENT ===
    
    private String getLevel1Title(String role) {
        switch (role) {
            case "Tech Development":
                return "🔧 Codebase Initialization - Deine digitale Identität";
            case "Business Development":
                return "🌱 Venture Genesis - Dein unternehmerisches Profil";
            case "UX Design":
                return "🎨 Interface Awakening - Deine kreative Essenz";
            case "Data Science":
                return "📊 Algorithm Genesis - Dein analytisches Bewusstsein";
            case "Legal Advisory":
                return "⚖️ Justice Framework - Dein rechtliches Fundament";
            case "Finance Analysis":
                return "💰 Capital Consciousness - Dein finanzielles Profil";
            case "Sustainability Lead":
                return "🌍 Planetary Stewardship - Dein ökologisches Bewusstsein";
            default:
                return "🚀 Sacred Profile - Deine bewusste Identität";
        }
    }
    
    private String getLevel1Description(String role) {
        switch (role) {
            case "Tech Development":
                return "Initialisiere dein Codesmith-Profil in der Sacred Technology Matrix. Konfiguriere deine Entwicklungsumgebung im Syntopia-Ökosystem und etabliere deine digitale Präsenz als bewusster Code-Architekt.";
            case "Business Development":
                return "Manifestiere dein Visionary-Profil im unternehmerischen Raum. Definiere deine Business-Vision und etabliere dich als Brücke zwischen Innovation und nachhaltiger Wertschöpfung im Syntopia-Netzwerk.";
            case "UX Design":
                return "Erwecke dein Creator-Profil zum Leben durch sacred interface design. Verbinde Ästhetik mit Funktionalität und werde zum Architekten bewusster User Experiences in der digitalen Transformation.";
            case "Data Science":
                return "Aktiviere dein Analyst-Profil in der Daten-Bewusstseins-Matrix. Transformiere rohe Informationen in erkenntnisreiche Weisheit und werde zum Navigator durch die Komplexität moderner Datenlandschaften.";
            case "Legal Advisory":
                return "Etabliere dein Guardian-Profil im Rechtsraum ethischer Innovation. Schaffe Brücken zwischen traditionellem Recht und bewusster Technologie-Governance für eine gerechtere digitale Zukunft.";
            case "Finance Analysis":
                return "Initialisiere dein Steward-Profil im nachhaltigen Finanzwesen. Verbinde traditionelle Finanzanalyse mit conscious capital flows und regenerativen Wirtschaftsmodellen.";
            case "Sustainability Lead":
                return "Manifestiere dein Steward-Profil als planetarer Bewusstseinshüter. Integriere ökologische Weisheit in alle Aspekte der technologischen und sozialen Innovation für eine regenerative Zukunft.";
            default:
                return "Erwecke dein authentisches Profil zum Leben und finde deinen Platz im bewussten Kollektiv der Weltveränderer.";
        }
    }
    
    private List<String> getLevel1Steps(String role) {
        List<String> baseSteps = Arrays.asList(
            "Vervollständige dein Sacred Profile mit persönlichen Informationen",
            "Wähle deine primäre Sacred Role: " + role,
            "Lade ein Avatar-Bild hoch, das deine Essenz widerspiegelt",
            "Setze deine Intention für deine Syntopia-Reise"
        );
        
        List<String> steps = new ArrayList<>(baseSteps);
        
        // Role-specific additional steps
        switch (role) {
            case "Tech Development":
                steps.add("Erkunde die Sacred Geometry UI-Elemente");
                steps.add("Verstehe das Gamification-System für Entwickler");
                break;
            case "Business Development":
                steps.add("Definiere deine Vision für bewusste Innovation");
                steps.add("Erkunde die Business-Netzwerk-Features");
                break;
            case "UX Design":
                steps.add("Erkunde die Sacred Design-System Prinzipien");
                steps.add("Teste die interaktiven Geometry-Komponenten");
                break;
            case "Data Science":
                steps.add("Erkunde die Analytics-Dashboard Grundlagen");
                steps.add("Verstehe Privacy-First Data-Prinzipien");
                break;
            case "Legal Advisory":
                steps.add("Erkunde die Governance-Framework Grundlagen");
                steps.add("Verstehe die dezentrale Entscheidungsstrukturen");
                break;
            case "Finance Analysis":
                steps.add("Erkunde die Sustainable Finance Metriken");
                steps.add("Verstehe die Conscious Capital Flows");
                break;
            case "Sustainability Lead":
                steps.add("Erkunde die Planetary Impact Dashboards");
                steps.add("Verstehe die Regenerative Metrics");
                break;
        }
        
        return steps;
    }
    
    // === LEVEL 2 QUEST CONTENT ===
    
    private String getLevel2Title(String role) {
        switch (role) {
            case "Tech Development":
                return "⚡ Code Synthesis Mastery - Algorithmic Consciousness";
            case "Business Development":
                return "🔄 Innovation Synthesis - Venture Alchemy";
            case "UX Design":
                return "🎭 Design Synthesis - Creative Transformation";
            case "Data Science":
                return "🧠 Data Synthesis - Pattern Recognition Awakening";
            case "Legal Advisory":
                return "🔗 Legal Synthesis - Justice Architecture";
            case "Finance Analysis":
                return "📈 Capital Synthesis - Conscious Finance Flow";
            case "Sustainability Lead":
                return "🌿 Planetary Synthesis - Regenerative Integration";
            default:
                return "🔄 Sacred Synthesis - Transformation Mastery";
        }
    }
    
    private String getLevel2Description(String role) {
        switch (role) {
            case "Tech Development":
                return "Evolviere deine Coding-Fähigkeiten durch die 4 SYN-Prinzipien. Lerne Sacred Development Patterns, verstehe Conscious Architecture und transformiere Code in lebendige, bewusste Systeme.";
            case "Business Development":
                return "Transformiere Business-Ideen durch die SYN-Prinzipien in manifeste Innovationen. Entwickle dein Verständnis für conscious entrepreneurship und regenerative business models.";
            case "UX Design":
                return "Synthese von Ästhetik und Bewusstsein durch die 4 SYN-Prinzipien. Kreiere Interfaces, die nicht nur schön sind, sondern das Bewusstsein der User erweitern und transformieren.";
            case "Data Science":
                return "Fusioniere Datenanalytik mit bewusster Erkenntnis durch die SYN-Prinzipien. Entwickle ethische AI-Modelle und privacy-preserving analytics für conscious decision making.";
            case "Legal Advisory":
                return "Synthese von traditionellem Recht und bewusster Governance durch die SYN-Prinzipien. Erschaffe rechtliche Frameworks für dezentrale, ethische Technologie-Innovation.";
            case "Finance Analysis":
                return "Evolution der Finanzanalyse durch bewusste Kapitalflüsse und die SYN-Prinzipien. Verstehe regenerative economics und impact-driven investment strategies.";
            case "Sustainability Lead":
                return "Synthese von ökologischer Weisheit und technologischer Innovation durch die SYN-Prinzipien. Entwickle ganzheitliche Sustainability-Frameworks für bewusste Organisationen.";
            default:
                return "Meistere die Kunst der bewussten Transformation durch die 4 SYN-Prinzipien und werde zum Architekten positiven Wandels.";
        }
    }
    
    private List<String> getLevel2Steps(String role) {
        List<String> steps = new ArrayList<>();
        
        // Common SYN principles learning
        steps.add("Studiere das 1. SYN-Prinzip: SYNTROPIE - Harmonische Ordnung");
        steps.add("Studiere das 2. SYN-Prinzip: SYNTHESE - Transformation & Integration");
        steps.add("Studiere das 3. SYN-Prinzip: SYNARCHIE - Organische Führung");
        steps.add("Studiere das 4. SYN-Prinzip: SYNCHRONIZITÄT - Bedeutungsvolle Zufälle");
        
        // Role-specific synthesis steps
        switch (role) {
            case "Tech Development":
                steps.add("Konfiguriere deinen Sacred Development Avatar");
                steps.add("Lerne Sacred Coding Patterns und Conscious Architecture");
                steps.add("Implementiere ein Mini-Projekt mit SYN-Prinzipien");
                break;
            case "Business Development":
                steps.add("Erstelle deinen Visionary Business Avatar");
                steps.add("Entwickle eine Innovation durch die SYN-Prinzipien");
                steps.add("Plane ein conscious venture concept");
                break;
            case "UX Design":
                steps.add("Designe deinen Creative Transformation Avatar");
                steps.add("Erstelle ein Sacred Interface Design");
                steps.add("Prototyp eines bewusstseinserweiternden UI Elements");
                break;
            case "Data Science":
                steps.add("Konfiguriere deinen Conscious Analytics Avatar");
                steps.add("Analysiere einen Datensatz mit ethischen Prinzipien");
                steps.add("Entwickle ein Privacy-First Analytics Konzept");
                break;
            case "Legal Advisory":
                steps.add("Etabliere deinen Justice Architecture Avatar");
                steps.add("Entwirf ein ethisches Governance Framework");
                steps.add("Analysiere rechtliche Implikationen von dezentralen Systemen");
                break;
            case "Finance Analysis":
                steps.add("Kreiere deinen Conscious Capital Avatar");
                steps.add("Analysiere Impact-Investment Opportunitäten");
                steps.add("Entwickle regenerative Finance Metriken");
                break;
            case "Sustainability Lead":
                steps.add("Manifestiere deinen Planetary Steward Avatar");
                steps.add("Erstelle einen Sustainability Transformation Plan");
                steps.add("Entwickle regenerative Organisationskonzepte");
                break;
        }
        
        steps.add("Bestehe den SYN-Prinzipien Verständnistest (Mini-Boss)");
        
        return steps;
    }
    
    // === LEVEL 3 QUEST CONTENT ===
    
    private String getLevel3Title(String role) {
        switch (role) {
            case "Tech Development":
                return "🕸️ Code Network Synarchy - Development Collective Building";
            case "Business Development":
                return "🤝 Innovation Synarchy - Conscious Venture Networks";
            case "UX Design":
                return "🎨 Design Synarchy - Creative Community Networks";
            case "Data Science":
                return "📊 Data Synarchy - Analytics Collective Intelligence";
            case "Legal Advisory":
                return "⚖️ Justice Synarchy - Legal Wisdom Networks";
            case "Finance Analysis":
                return "💎 Capital Synarchy - Conscious Finance Communities";
            case "Sustainability Lead":
                return "🌍 Planetary Synarchy - Regenerative Networks";
            default:
                return "🕸️ Sacred Synarchy - Consciousness Network Building";
        }
    }
    
    private String getLevel3Description(String role) {
        switch (role) {
            case "Tech Development":
                return "Aufbau organischer Entwickler-Netzwerke durch bewusste Code-Kollaboration. Werde Teil der Codesmith-Community und etabliere bedeutungsvolle technische Partnerschaften für Sacred Technology Innovation.";
            case "Business Development":
                return "Formung bewusster Venture-Netzwerke für regenerative Innovation. Verbinde dich mit anderen Visionären und baue Synergien für conscious entrepreneurship und nachhaltige Geschäftsmodelle auf.";
            case "UX Design":
                return "Entstehung kreativer Design-Communities für bewusste User Experiences. Vernetze dich mit anderen Creators und entwickle kollaborative Design-Systeme für transformative Interfaces.";
            case "Data Science":
                return "Bildung ethischer Analytics-Kollektive für conscious data science. Verbinde dich mit anderen Analysten und entwickle gemeinsame Standards für privacy-preserving und bewusste Datennutzung.";
            case "Legal Advisory":
                return "Aufbau juristischer Weisheits-Netzwerke für ethische Tech-Governance. Connecte mit anderen Legal Guardians und entwickle collaborative frameworks für bewusste Rechtsinnovation.";
            case "Finance Analysis":
                return "Formation bewusster Finanz-Communities für regenerative Economics. Vernetze dich mit anderen Stewards und baue Systeme für conscious capital allocation und impact-driven investment auf.";
            case "Sustainability Lead":
                return "Entstehung planetarer Regenerations-Netzwerke für systemic change. Verbinde dich mit anderen Planetary Stewards und entwickle ganzheitliche Sustainability-Ökosysteme.";
            default:
                return "Werde Teil der bewussten Community und baue bedeutungsvolle Netzwerke für kollektive Transformation und positive Veränderung auf.";
        }
    }
    
    private List<String> getLevel3Steps(String role) {
        List<String> steps = new ArrayList<>();
        
        // Common community steps
        steps.add("Erstelle deinen ersten Community-Post über deine Syntopia-Vision");
        steps.add("Entdecke andere User mit ähnlichen Interessen und Werten");
        steps.add("Kommentiere auf 3 Posts von Community-Mitgliedern");
        
        // Role-specific community building
        switch (role) {
            case "Tech Development":
                steps.add("Tritt der Codesmith-Community bei");
                steps.add("Teile ein Sacred Development Insight oder Pattern");
                steps.add("Verbinde dich mit 2 anderen Tech Development Members");
                steps.add("Starte eine Diskussion über Conscious Architecture");
                break;
            case "Business Development":
                steps.add("Tritt der Visionary-Community bei");
                steps.add("Präsentiere eine Innovation-Idee der Community");
                steps.add("Verbinde dich mit 2 anderen Business Development Members");
                steps.add("Initiiere ein Conscious Venture Brainstorming");
                break;
            case "UX Design":
                steps.add("Tritt der Creator-Community bei");
                steps.add("Teile ein Sacred Design Konzept oder Prototyp");
                steps.add("Verbinde dich mit 2 anderen UX Design Members");
                steps.add("Starte eine Sacred Interface Design Challenge");
                break;
            case "Data Science":
                steps.add("Tritt der Analyst-Community bei");
                steps.add("Teile ethische Data Science Insights");
                steps.add("Verbinde dich mit 2 anderen Data Science Members");
                steps.add("Diskutiere Privacy-First Analytics Approaches");
                break;
            case "Legal Advisory":
                steps.add("Tritt der Guardian-Community bei");
                steps.add("Teile rechtliche Innovationsperspektiven");
                steps.add("Verbinde dich mit 2 anderen Legal Advisory Members");
                steps.add("Diskutiere dezentrale Governance Frameworks");
                break;
            case "Finance Analysis":
                steps.add("Tritt der Steward-Community bei");
                steps.add("Teile Conscious Finance Insights");
                steps.add("Verbinde dich mit 2 anderen Finance Analysis Members");
                steps.add("Diskutiere regenerative Investment Strategien");
                break;
            case "Sustainability Lead":
                steps.add("Tritt der Planetary Steward-Community bei");
                steps.add("Teile Regenerative Innovation Konzepte");
                steps.add("Verbinde dich mit 2 anderen Sustainability Members");
                steps.add("Initiiere eine Planetary Impact Diskussion");
                break;
        }
        
        steps.add("Lerne das Community-System und Governance-Strukturen kennen");
        steps.add("Verstehe die dezentralen Entscheidungsprozesse");
        
        return steps;
    }
    
    // === LEVEL 4 QUEST CONTENT ===
    
    private String getLevel4Title(String role) {
        switch (role) {
            case "Tech Development":
                return "🔗 GitHub Synchronicity - Sacred Code Repository Integration";
            case "Business Development":
                return "🚀 Innovation Synchronicity - Project Repository Connection";
            case "UX Design":
                return "🎨 Design Synchronicity - Creative Repository Integration";
            case "Data Science":
                return "📊 Analytics Synchronicity - Data Repository Connection";
            case "Legal Advisory":
                return "⚖️ Legal Synchronicity - Governance Repository Integration";
            case "Finance Analysis":
                return "💰 Finance Synchronicity - Economic Repository Connection";
            case "Sustainability Lead":
                return "🌱 Planetary Synchronicity - Impact Repository Integration";
            default:
                return "⚡ Sacred Synchronicity - GitHub Integration Mastery";
        }
    }
    
    private String getLevel4Description(String role) {
        switch (role) {
            case "Tech Development":
                return "Synchronisiere deine Codesmith-Journey mit dem globalen Open Source Ökosystem. Verbinde Syntopia mit GitHub und erschließe Sacred Development Quests aus real-world repositories für conscious coding impact.";
            case "Business Development":
                return "Integriere deine Visionary-Projekte mit der globalen Innovation-Community. Verbinde GitHub für collaborative venture development und synchronisiere business opportunities mit meaningful coincidences.";
            case "UX Design":
                return "Fusioniere deine Creative-Journey mit der Open Design Community. GitHub-Integration ermöglicht Sacred Design Quests und kollaborative Interface-Entwicklung für conscious user experiences.";
            case "Data Science":
                return "Synchronisiere deine Analytics-Expertise mit ethischen Data Science Projekten. GitHub-Connection öffnet conscious data quests und privacy-preserving research opportunities.";
            case "Legal Advisory":
                return "Verbinde deine rechtliche Expertise mit Open Source Governance Projekten. GitHub-Integration erschließt Legal Guardian Quests für dezentrale rechtliche Innovation.";
            case "Finance Analysis":
                return "Integriere deine Finance-Skills mit Open Economics Initiativen. GitHub-Synchronization öffnet Conscious Capital Quests und regenerative finance project opportunities.";
            case "Sustainability Lead":
                return "Synchronisiere deine Planetary Stewardship mit globalen Regenerations-Projekten. GitHub-Integration erschließt Impact Quests für systemic sustainability transformation.";
            default:
                return "Verbinde deine Sacred Journey mit dem globalen Open Source Kollektiv und erschließe bedeutungsvolle Quest-Synchronizitäten für positive Weltveränderung.";
        }
    }
    
    private List<String> getLevel4Steps(String role) {
        List<String> steps = new ArrayList<>();
        
        // Common GitHub integration steps
        steps.add("Erstelle einen GitHub Account (falls noch nicht vorhanden)");
        steps.add("Verstehe die OAuth-Integration zwischen Syntopia und GitHub");
        steps.add("Autorisiere Syntopia für GitHub-Zugriff (read-only permissions)");
        steps.add("Synchronisiere dein Syntopia-Profil mit GitHub");
        
        // Role-specific quest synchronization
        switch (role) {
            case "Tech Development":
                steps.add("Erkunde Sacred Development Quest-Issues in Open Source Repos");
                steps.add("Filtriere GitHub Issues nach Conscious Coding Labels");
                steps.add("Wähle dein erstes Sacred Development Quest aus");
                break;
            case "Business Development":
                steps.add("Entdecke Innovation Quest-Issues in Business-Repositories");
                steps.add("Identifiziere Venture Development Opportunities");
                steps.add("Wähle dein erstes Conscious Business Quest aus");
                break;
            case "UX Design":
                steps.add("Finde Creative Quest-Issues in Design System Repositories");
                steps.add("Erkunde Sacred Interface Improvement Opportunities");
                steps.add("Wähle dein erstes Conscious Design Quest aus");
                break;
            case "Data Science":
                steps.add("Identifiziere Analytics Quest-Issues in Data Science Repos");
                steps.add("Finde Privacy-First Data Analysis Opportunities");
                steps.add("Wähle dein erstes Ethical Data Quest aus");
                break;
            case "Legal Advisory":
                steps.add("Erkunde Legal Quest-Issues in Governance Repositories");
                steps.add("Identifiziere Regulatory Innovation Opportunities");
                steps.add("Wähle dein erstes Justice Architecture Quest aus");
                break;
            case "Finance Analysis":
                steps.add("Finde Finance Quest-Issues in Economic Analysis Repos");
                steps.add("Entdecke Conscious Capital Analysis Opportunities");
                steps.add("Wähle dein erstes Regenerative Finance Quest aus");
                break;
            case "Sustainability Lead":
                steps.add("Identifiziere Impact Quest-Issues in Sustainability Repos");
                steps.add("Erkunde Planetary Regeneration Project Opportunities");
                steps.add("Wähle dein erstes Planetary Stewardship Quest aus");
                break;
        }
        
        steps.add("Bestätige erfolgreiche Quest-Synchronisation mit GitHub Issues");
        steps.add("Verstehe das Advanced Quest System für Level 5+ GitHub Quests");
        
        return steps;
    }
    
    // === HELPER METHODS ===
    
    private String generateQuestId(String role, int level) {
        String roleCode = role.replaceAll(" ", "").toLowerCase();
        return String.format("onboarding_%s_level_%d", roleCode, level);
    }
    
    private String getRoleDialect(String role) {
        switch (role) {
            case "Tech Development":
                return "Codesmith";
            case "Business Development":
                return "Visionary";
            case "UX Design":
                return "Creator";
            case "Data Science":
                return "Analyst";
            case "Legal Advisory":
                return "Guardian";
            case "Finance Analysis":
                return "Steward";
            case "Sustainability Lead":
                return "Planetary Steward";
            default:
                return "Sacred Practitioner";
        }
    }
}
