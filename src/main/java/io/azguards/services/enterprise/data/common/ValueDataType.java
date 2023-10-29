package io.azguards.services.enterprise.data.common;

public enum ValueDataType {

    String("String"),
    Integer("Integer"),
    BigDecimal("BigDecimal"),
    Long("Long");

    private final String value;

    ValueDataType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
