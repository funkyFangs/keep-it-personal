package io.funky.fangs.keep_it_personal.command

import io.funky.fangs.keep_it_personal.KeepItPersonalModInitializer
import net.minecraft.datafixer.DataFixTypes
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.*
import java.util.Collections.unmodifiableSet
import java.util.stream.Collectors.toMap

class KeepCommandState(
    private val playerPreferences: MutableMap<UUID, EnumSet<DeathPreference>> = mutableMapOf()
): PersistentState() {
    companion object {
        private const val PLAYER_PREFERENCES_TAG = "playerPreferences"

        private fun fromNbt(nbtCompound: NbtCompound): KeepCommandState {
            val playerPreferencesCompound = nbtCompound.getCompound(PLAYER_PREFERENCES_TAG)
            return KeepCommandState(
                playerPreferencesCompound.keys
                    .stream()
                    .collect(toMap(UUID::fromString) { playerId ->
                        EnumSet.copyOf(
                            playerPreferencesCompound.getIntArray(playerId)
                                .map { ordinal -> DeathPreference.entries[ordinal] }
                        )
                    })
            )
        }

        private val keepCommandStateType = Type(
            { KeepCommandState() },
            { nbtCompound, _ -> fromNbt(nbtCompound) },
            DataFixTypes.PLAYER
        )

        fun getCurrentState(world: RegistryKey<World>, server: MinecraftServer): KeepCommandState {
            return requireNotNull(server.getWorld(world))
                .persistentStateManager
                .getOrCreate(keepCommandStateType, KeepItPersonalModInitializer.MOD_ID)
        }
    }

    override fun writeNbt(nbtCompound: NbtCompound, registries: RegistryWrapper.WrapperLookup?): NbtCompound {
        val playerPreferencesCompound = nbtCompound.getCompound(PLAYER_PREFERENCES_TAG)
        playerPreferences.forEach { (playerId, preferences) ->
            playerPreferencesCompound.putIntArray(playerId.toString(), preferences.map { it.ordinal }.toIntArray())
        }
        return nbtCompound
    }

    fun getPlayerPreferences(playerId: UUID): Set<DeathPreference> {
        return unmodifiableSet(
            playerPreferences.computeIfAbsent(playerId) { EnumSet.noneOf(DeathPreference::class.java) }
        )
    }

    fun addPlayerPreference(playerId: UUID, preference: DeathPreference) {
        addPlayerPreferences(playerId, EnumSet.of(preference))
    }

    private fun addPlayerPreferences(playerId: UUID, preferences: EnumSet<DeathPreference>) {
        playerPreferences.merge(playerId, preferences) { currentPreferences, preferencesToAdd ->
            val result = EnumSet.copyOf(currentPreferences)
            result.addAll(preferencesToAdd)
            if (result != currentPreferences) {
                markDirty()
            }
            result
        }
    }

    fun removePlayerPreference(playerId: UUID, preference: DeathPreference) {
        removePlayerPreferences(playerId, EnumSet.of(preference))
    }

    private fun removePlayerPreferences(playerId: UUID, preferences: EnumSet<DeathPreference>) {
        playerPreferences.merge(playerId, preferences) { currentPreferences, preferencesToRemove ->
            val result = EnumSet.copyOf(currentPreferences)
            result.removeAll(preferencesToRemove)
            if (result != currentPreferences) {
                markDirty()
            }
            result
        }
    }

    fun fillPlayerPreferences(playerId: UUID) {
        addPlayerPreferences(playerId, EnumSet.allOf(DeathPreference::class.java))
    }

    fun clearPlayerPreferences(playerId: UUID) {
        removePlayerPreferences(playerId, EnumSet.allOf(DeathPreference::class.java))
    }
}
