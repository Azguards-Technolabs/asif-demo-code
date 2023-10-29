package io.azguards.services.enterprise.data.common;

public enum DetailsLevel {

    FULL("FULL");

    private final String value;

    DetailsLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
