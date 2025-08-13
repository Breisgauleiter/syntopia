package com.syntopia.service;

import com.syntopia.model.OnboardingQuest;
import com.syntopia.repository.OnboardingQuestRepository;
import com.syntopia.util.RoleDialectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Persistent access layer for onboarding quest definitions.
 * Seeds the database once (idempotent) using the dynamic generator and serves cached results afterwards.
 */
@Service
public class OnboardingQuestService {

    private final OnboardingQuestRepository repository;
    private final OnboardingQuestGenerator generator;

    private volatile Map<String, OnboardingQuest> cacheById = new HashMap<>();

    public OnboardingQuestService(OnboardingQuestRepository repository, OnboardingQuestGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    @PostConstruct
    public void seedIfEmpty() {
        if (repository.count() == 0) {
            repository.saveAll(generator.generateAllOnboardingQuests());
        }
        refreshCache();
    }

    private void refreshCache() {
        Map<String, OnboardingQuest> map = new HashMap<>();
        repository.findAll().forEach(q -> map.put(q.getId(), q));
        this.cacheById = map; // atomic swap
    }

    /** Force refresh the in-memory cache from persistent storage (admin use). */
    public void forceRefreshCache() {
        refreshCache();
    }

    public List<OnboardingQuest> getAll() {
        return new ArrayList<>(cacheById.values());
    }

    public List<OnboardingQuest> getByRoleDisplay(String roleDisplay) {
        String dialect = RoleDialectMapper.toDialect(roleDisplay);
        return repository.findByRoleDialect(dialect);
    }

    public List<OnboardingQuest> getByLevel(int level) {
        return repository.findByLevel(level);
    }

    public Optional<OnboardingQuest> getByRoleAndLevel(String roleDisplay, int level) {
    String roleCode = roleDisplay.replaceAll(" ", "").toLowerCase();
    String id = String.format("onboarding_%s_level_%d", roleCode, level);
    return Optional.ofNullable(cacheById.get(id));
    }

    /**
     * Ensure a specific quest definition exists (lazy create if new role introduced).
     */
    public OnboardingQuest ensureQuest(String roleDisplay, int level) {
        return getByRoleAndLevel(roleDisplay, level).orElseGet(() -> {
            OnboardingQuest created = generator.generateQuestForRoleAndLevel(roleDisplay, level);
            repository.save(created);
            cacheById.put(created.getId(), created);
            return created;
        });
    }
}
