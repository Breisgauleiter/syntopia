package com.syntopia.util;

public final class RoleDialectMapper {

    private RoleDialectMapper() {}

    public static String toDialect(String roleDisplayName) {
        switch (roleDisplayName) {
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
