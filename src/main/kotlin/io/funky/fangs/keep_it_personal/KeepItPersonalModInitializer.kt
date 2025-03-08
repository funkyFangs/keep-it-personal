package io.funky.fangs.keep_it_personal

import com.mojang.brigadier.arguments.BoolArgumentType
import io.funky.fangs.keep_it_personal.command.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager

class KeepItPersonalModInitializer: ModInitializer {
    companion object {
        const val MOD_ID = "keep_it_personal"
    }

    @Override
    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            val keepCommand = CommandManager.literal(KEEP_COMMAND)

            dispatcher.register(keepCommand.executes(ViewDeathPreferencesCommand()))

            for (keepPreference in DeathPreference.entries) {
                dispatcher.register(
                    keepCommand.then(CommandManager.literal(keepPreference.toString())
                        .executes(GetDeathPreferenceCommand(keepPreference))
                        .then(CommandManager.argument(ARGUMENT_NAME, BoolArgumentType.bool())
                            .executes(SetDeathPreferenceCommand(keepPreference))
                        )
                    )
                )
            }

            dispatcher.register(
                keepCommand.then(CommandManager.literal(FillDeathPreferencesCommand.NAME)
                    .executes(FillDeathPreferencesCommand())
                )
            )

            dispatcher.register(
                keepCommand.then(CommandManager.literal(ClearDeathPreferencesCommand.NAME)
                    .executes(ClearDeathPreferencesCommand())
                )
            )
        }
    }
}
