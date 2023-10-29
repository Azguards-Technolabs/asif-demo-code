package io.azguards.services.enterprise.data.common;

public enum TargetId {

    Firebase("Firebase"),
    CDCEvent("CDCEvent");

    private final String value;

    TargetId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
