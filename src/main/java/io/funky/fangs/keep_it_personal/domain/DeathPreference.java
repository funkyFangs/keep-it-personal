package io.funky.fangs.keep_it_personal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

public enum DeathPreference {
    INVENTORY("inventory"),
    EXPERIENCE("experience"),
    HOTBAR("hotbar"),
    ARMOR("armor"),
    OFFHAND("offhand"),
    CURSED("cursed");

    private final String value;

    DeathPreference(String value) {
        this.value = value;
    }

    @JsonValue
    public String toString() {
        return value;
    }

    private static final Map<String, DeathPreference> VALUE_MAP = Stream.of(values())
            .collect(toUnmodifiableMap(DeathPreference::toString, identity()));

    @JsonCreator
    public static DeathPreference fromString(String value) {
        return VALUE_MAP.get(value);
    }

    public static DeathPreference fromOrdinal(int ordinal) {
        return values()[ordinal];
    }
}
