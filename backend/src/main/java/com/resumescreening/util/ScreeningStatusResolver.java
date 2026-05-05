package com.resumescreening.util;

public final class ScreeningStatusResolver {

    private ScreeningStatusResolver() {
    }

    public static String resolve(double score) {
        if (score >= ScreeningConstants.SHORTLIST_THRESHOLD) {
            return ScreeningConstants.STATUS_SHORTLISTED;
        }
        if (score >= ScreeningConstants.CONSIDER_THRESHOLD) {
            return ScreeningConstants.STATUS_CONSIDER;
        }
        return ScreeningConstants.STATUS_REJECTED;
    }
}
