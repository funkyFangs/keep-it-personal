package io.funky.fangs.keep_it_personal

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.mojang.brigadier.arguments.BoolArgumentType
import io.funky.fangs.keep_it_personal.command.*
import io.funky.fangs.keep_it_personal.configuration.KeepItPersonalConfiguration
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.command.CommandManager

class KeepItPersonalModInitializer: ModInitializer {
    companion object {
        const val MOD_ID = "keep_it_personal"

        private val CONFIGURATION_FILE = FabricLoader.getInstance().configDir.resolve("$MOD_ID.toml").toFile()
        val CONFIGURATION: KeepItPersonalConfiguration by lazy {
            TomlMapper().readerFor(KeepItPersonalConfiguration::class.java).readValue(CONFIGURATION_FILE)
        }
    }

    @Override
    override fun onInitialize() {
        if (!CONFIGURATION_FILE.exists()) {
            CONFIGURATION_FILE.createNewFile()
        }

        val (disabled, enabled) = CONFIGURATION.preferences
        if (enabled.intersect(disabled).isNotEmpty()) {
            throw IllegalArgumentException("Overlapping preferences in disabled and enabled are not permitted")
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            val keepCommand = CommandManager.literal(KEEP_COMMAND)

            dispatcher.register(keepCommand.executes(ViewDeathPreferencesCommand()))

            DeathPreference.entries
                .filterNot { enabled.contains(it) || disabled.contains(it) }
                .forEach {
                    dispatcher.register(
                        keepCommand.then(CommandManager.literal(it.toString())
                            .executes(GetDeathPreferenceCommand(it))
                            .then(CommandManager.argument(ARGUMENT_NAME, BoolArgumentType.bool())
                                .executes(SetDeathPreferenceCommand(it))
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

