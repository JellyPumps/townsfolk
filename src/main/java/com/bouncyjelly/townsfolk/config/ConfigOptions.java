package com.bouncyjelly.townsfolk.config;

import java.util.List;
import java.util.Map;

public final class ConfigOptions {
    public static final int MAX_SCHEDULE_ENTRIES = 8;
    public static final int SKIN_VARIANTS = 8;

    public static final List<String> AGE_GROUPS = List.of("child", "teen", "adult", "elder");

    // placeholder list ... will move JSON later
    public static final List<String> PERSONALITIES = List.of(
            "outdoorsy", "homebody", "social", "workaholic", "early_bird", "night_owl");

    public static final Map<String, String> PERSONALITY_BLURBS = Map.of(
            "outdoorsy", "Spends free time outside: walks, exercise, parks.",
            "homebody", "Prefers staying in. Rarely goes out in free time.",
            "social", "Seeks company. Cafes, pubs, visiting friends.",
            "workaholic", "Short breaks, goes to bed late.",
            "early_bird", "Up early, asleep early.",
            "night_owl", "Up late, sleeps in.");

    public enum Tab {
        GENERAL("General"), PLACES("Places"), SCHEDULE("Schedule"), PERSONALITY("Personality"),
        SOCIAL("Social"), LIFE("Life"), STATUS("Status");

        public final String label;
        Tab(String label) { this.label = label; }
    }

    /** Empty options are free text. */
    public record PlannedField(String key, String label, Tab tab, List<String> options) {
        public boolean isChoice() { return !options.isEmpty(); }
    }

    private static final List<String> FREQUENCY = List.of("rarely", "sometimes", "often");

    public static final List<PlannedField> PLANNED = List.of(
            new PlannedField("household", "Household", Tab.SOCIAL, List.of()),
            new PlannedField("household_role", "Role in household", Tab.SOCIAL,
                    List.of("parent", "child", "spouse", "sibling", "grandparent", "other")),
            new PlannedField("social_circle", "Social circle", Tab.SOCIAL, List.of()),
            new PlannedField("favorite_hangout", "Favorite hangout", Tab.SOCIAL, List.of()),
            new PlannedField("talkativeness", "Gossip", Tab.SOCIAL, List.of("quiet", "average", "chatty")),
            new PlannedField("school", "School", Tab.LIFE, List.of()),
            new PlannedField("school_level", "School level", Tab.LIFE,
                    List.of("none", "elementary", "junior_high", "high_school")),
            new PlannedField("job_title", "Job title", Tab.LIFE, List.of()),
            new PlannedField("commute_mode", "Commute", Tab.LIFE,
                    List.of("walk", "train", "bicycle", "car", "truck")),
            new PlannedField("exercise", "Exercise", Tab.LIFE, FREQUENCY),
            new PlannedField("dining_out", "Eating out", Tab.LIFE, FREQUENCY));

    private ConfigOptions() {}

    public static PlannedField planned(String key) {
        for (PlannedField f : PLANNED) if (f.key().equals(key)) return f;
        return null;
    }

    public static String pretty(String id) {
        if (id.isEmpty()) return "(none)";
        String s = id.replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}