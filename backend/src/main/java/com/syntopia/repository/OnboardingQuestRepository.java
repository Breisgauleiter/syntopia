package com.syntopia.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import com.syntopia.model.OnboardingQuest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnboardingQuestRepository extends ArangoRepository<OnboardingQuest, String> {

    List<OnboardingQuest> findByRoleDialect(String roleDialect);

    List<OnboardingQuest> findByLevel(int level);
}
