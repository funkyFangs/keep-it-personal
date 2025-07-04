package io.funky.fangs.keep_it_personal.configuration;

import io.funky.fangs.keep_it_personal.domain.DeathPreference;

import java.util.Set;

import static java.util.Collections.emptySet;

public record PreferencesConfiguration(
        Set<DeathPreference> disabled,
        Set<DeathPreference> enabled
) {
    public static final Set<DeathPreference> DEFAULT_DISABLED = emptySet();
    public static final Set<DeathPreference> DEFAULT_ENABLED = emptySet();

    public PreferencesConfiguration() {
        this(DEFAULT_DISABLED, DEFAULT_ENABLED);
    }

    @Override
    public Set<DeathPreference> enabled() {
        return enabled == null ? DEFAULT_ENABLED : enabled;
    }

    @Override
    public Set<DeathPreference> disabled() {
        return disabled == null ? DEFAULT_DISABLED : disabled;
    }
}
