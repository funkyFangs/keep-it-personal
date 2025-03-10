package io.funky.fangs.keep_it_personal

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.mojang.brigadier.arguments.BoolArgumentType
import io.funky.fangs.keep_it_personal.command.*
import io.funky.fangs.keep_it_personal.configuration.KeepItPersonalConfiguration
import me.lucko.fabric.api.permissions.v0.Permissions
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

            val defaultPermissionLevel = CONFIGURATION.permissions.permissionLevel

            dispatcher.register(
                keepCommand.requires(Permissions.require("${MOD_ID}.${KEEP_COMMAND}", defaultPermissionLevel))
                    .executes(ViewDeathPreferencesCommand())
            )

            DeathPreference.entries
                .filterNot { enabled.contains(it) || disabled.contains(it) }
                .forEach {
                    dispatcher.register(
                        keepCommand.then(CommandManager.literal(it.toString())
                            .requires(Permissions.require(
                                "${MOD_ID}.${KEEP_COMMAND}.${it}",
                                defaultPermissionLevel
                            ))
                            .executes(GetDeathPreferenceCommand(it))
                            .then(CommandManager.argument(ARGUMENT_NAME, BoolArgumentType.bool())
                                .requires(Permissions.require(
                                    "${MOD_ID}.${KEEP_COMMAND}.${it}",
                                    defaultPermissionLevel
                                ))
                                .executes(SetDeathPreferenceCommand(it))
                            )
                        )
                    )
                }

            dispatcher.register(
                keepCommand.then(CommandManager.literal(FillDeathPreferencesCommand.NAME)
                    .requires(Permissions.require(
                        "${MOD_ID}.${KEEP_COMMAND}.${FillDeathPreferencesCommand.NAME}",
                        defaultPermissionLevel
                    ))
                    .executes(FillDeathPreferencesCommand())
                )
            )

            dispatcher.register(
                keepCommand.then(CommandManager.literal(ClearDeathPreferencesCommand.NAME)
                    .requires(Permissions.require(
                        "${MOD_ID}.${KEEP_COMMAND}.${ClearDeathPreferencesCommand.NAME}",
                        defaultPermissionLevel
                    ))
                    .executes(ClearDeathPreferencesCommand())
                )
            )
        }
    }
}

