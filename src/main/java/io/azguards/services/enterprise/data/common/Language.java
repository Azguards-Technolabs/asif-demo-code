package io.azguards.services.enterprise.data.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language {

	EN_US("en-US");

    private final String value;
    
    private static Map<String, Language> languageMap = new HashMap<>();

    static {
        for (Language c : values()) {
        	languageMap.put(c.value, c);
        }
    }

    public static Language getByValue(String value) {
        return Optional.ofNullable(languageMap.get(value)).orElse(null);
    }
}
