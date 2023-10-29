package io.azguards.services.enterprise.data.common;

public enum KeyDataType {

    String("String"),
    Integer("Integer"),
    BigDecimal("BigDecimal"),
    Long("Long");

    private final String value;

    KeyDataType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
