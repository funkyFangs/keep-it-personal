package io.funky.fangs.keep_it_personal.configuration

import io.funky.fangs.keep_it_personal.command.DeathPreference

data class KeepItPersonalConfiguration(
    val preferences: PreferencesConfiguration = PreferencesConfiguration()
)

data class PreferencesConfiguration(
    val disabled: Set<DeathPreference> = emptySet(),
    val enabled: Set<DeathPreference> = emptySet()
)