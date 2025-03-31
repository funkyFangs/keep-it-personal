package io.funky.fangs.keep_it_personal.domain;

import jakarta.annotation.Nonnull;

import java.util.Set;

public interface DeathPreferenceContainer {
    @Nonnull
    Set<DeathPreference> getDeathPreferences();

    default boolean hasDeathPreference(@Nonnull DeathPreference deathPreference) {
        return getDeathPreferences().contains(deathPreference);
    }

    boolean addDeathPreference(@Nonnull DeathPreference deathPreference);
    boolean removeDeathPreference(@Nonnull DeathPreference deathPreference);
    void clearDeathPreferences();
    void fillDeathPreferences();
}
