package io.funky.fangs.keep_it_personal.command

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.function.Function.identity
import java.util.stream.Collectors.toUnmodifiableMap

enum class DeathPreference(
    private val preferenceName: String
) {
    INVENTORY("inventory"),
    EXPERIENCE("experience"),
    HOTBAR("hotbar"),
    ARMOR("armor"),
    OFFHAND("offhand"),
    CURSED("cursed");

    companion object {
        private val VALUE_MAP: Map<String, DeathPreference> = entries.stream()
            .collect(toUnmodifiableMap(DeathPreference::toString, identity()))

        @JsonCreator
        fun fromString(value: String): DeathPreference? {
            return VALUE_MAP[value]
        }
    }

    @JsonValue
    override fun toString(): String {
        return preferenceName
    }
}

class SetDeathPreferenceCommand(private val deathPreference: DeathPreference): KeepItPersonalCommand() {
    override fun run(
        keepCommandState: KeepCommandState,
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        val shouldKeep = context.getArgument(ARGUMENT_NAME, Boolean::class.java)
        val source = context.source

        if (shouldKeep) {
            keepCommandState.addPlayerPreference(player.uuid, deathPreference)
            source.sendFeedback({ Text.literal("Added $deathPreference to preferences!") }, false)
        }
        else {
            keepCommandState.removePlayerPreference(player.uuid, deathPreference)
            source.sendFeedback({ Text.literal("Removed $deathPreference from preferences!") }, false)
        }

        return SINGLE_SUCCESS
    }
}

class GetDeathPreferenceCommand(private val deathPreference: DeathPreference): KeepItPersonalCommand() {
    override fun run(
        keepCommandState: KeepCommandState,
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        try {
            val preferences = keepCommandState.getPlayerPreferences(player.uuid)
            context.source.sendFeedback(
                { Text.literal("Keeping $deathPreference: ${preferences.contains(deathPreference)}") },
                false
            )
            return SINGLE_SUCCESS
        }
        catch (error: RuntimeException) {
            return SINGLE_ERROR
        }
    }
}
