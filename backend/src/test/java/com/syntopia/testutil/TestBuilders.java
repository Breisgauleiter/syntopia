package com.syntopia.testutil;

import com.syntopia.model.Quest;
import com.syntopia.model.User;
import com.syntopia.model.UserQuest;

import java.time.LocalDateTime;

/** Utility factory methods for constructing model objects in tests. */
public final class TestBuilders {
    private TestBuilders() {}

    public static User newUser(String id, int level, String selectedRole) {
        User u = new User();
        u.setId(id);
        u.setCurrentLevel(level);
        u.setSelectedRole(selectedRole);
        return u;
    }

    public static Quest newQuest(String id, int requiredLevel, String role, boolean reusable, int maxCompletions, int experience) {
        Quest q = new Quest();
        q.setId(id);
        q.setRequiredLevel(requiredLevel);
        q.setRole(role);
        q.setReusable(reusable);
        q.setMaxCompletions(maxCompletions);
        q.setCurrentCompletions(0);
        q.setExperienceReward(experience);
        return q;
    }

    public static UserQuest newUserQuest(User user, Quest quest, UserQuest.UserQuestStatus status) {
        UserQuest uq = new UserQuest(user, quest);
        uq.setId("uq-" + quest.getId());
        uq.setQuest(quest);
        uq.setStatus(status);
        if (status == UserQuest.UserQuestStatus.USER_ACTIVE) {
            uq.setStartedAt(LocalDateTime.now());
            uq.setProgress(10);
        }
        if (status == UserQuest.UserQuestStatus.USER_COMPLETED || status == UserQuest.UserQuestStatus.USER_VERIFIED) {
            uq.setProgress(100);
            uq.setCompletedAt(LocalDateTime.now().minusMinutes(5));
        }
        return uq;
    }
}
