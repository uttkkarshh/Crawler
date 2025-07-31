package com.ut.crawler.platform;

public enum PlatformType {
    YOUTUBE("YouTube"),
    X("Twitter"),
    INSTAGRAM("Instagram"),
    REDDIT("Reddit"),
    OTHERS("Others");

    private final String displayName;

    PlatformType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
