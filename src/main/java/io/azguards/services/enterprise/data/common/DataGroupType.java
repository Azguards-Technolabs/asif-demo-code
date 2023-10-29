package io.azguards.services.enterprise.data.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum DataGroupType {

    SingleItem("SingleItem"),
    MultipleItem("MultipleItem");

    private final String value;

    DataGroupType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
  
    private static Map<String, DataGroupType> dataGroupTypeMap = new HashMap<>();

    static {
        for (DataGroupType c : values()) {
            dataGroupTypeMap.put(c.value, c);
        }
    }

    public static DataGroupType getByValue(String value) {
        return Optional.ofNullable(dataGroupTypeMap.get(value)).orElse(null);
    }
}
