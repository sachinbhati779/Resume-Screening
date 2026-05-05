package com.resumescreening.util;

public final class ScreeningConstants {

    public static final int DEFAULT_SKILL_WEIGHT = 40;
    public static final int DEFAULT_EXPERIENCE_WEIGHT = 25;
    public static final int DEFAULT_PROJECT_WEIGHT = 15;
    public static final int DEFAULT_EDUCATION_WEIGHT = 10;
    public static final int DEFAULT_KEYWORD_WEIGHT = 10;

    public static final double SHORTLIST_THRESHOLD = 80.0;
    public static final double CONSIDER_THRESHOLD = 60.0;

    public static final String STATUS_SHORTLISTED = "SHORTLISTED";
    public static final String STATUS_CONSIDER = "CONSIDER";
    public static final String STATUS_REJECTED = "REJECTED";

    public static final String INTERVIEW_IN_PROGRESS = "IN_PROGRESS";
    public static final String INTERVIEW_COMPLETED = "COMPLETED";

    private ScreeningConstants() {
    }
}
