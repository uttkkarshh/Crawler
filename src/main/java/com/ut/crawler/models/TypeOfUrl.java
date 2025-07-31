package com.ut.crawler.models;

public enum TypeOfUrl {
    SEED("SEED"),
    POST("POST");

    private final String value;

    TypeOfUrl(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
