package io.funky.fangs.keep_it_personal;

import io.funky.fangs.keep_it_personal.command.*;
import io.funky.fangs.keep_it_personal.configuration.KeepItPersonalConfiguration;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.exception.KeepItPersonalException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;

public class KeepItPersonalModInitializer implements ModInitializer {
    public static final String MOD_ID = "keep_it_personal";

    @Override
    public void onInitialize() {
        final var configuration = KeepItPersonalConfiguration.getInstance();
        final var preferences = configuration.preferences();
        final var disabled = preferences.disabled();
        final var enabled = preferences.enabled();
        final int permissionLevel = configuration.permissions().permissionLevel();

        final var hasIntersectingPreferences = Arrays.stream(DeathPreference.values())
                .anyMatch(deathPreference -> enabled.contains(deathPreference)
                        && disabled.contains(deathPreference));

        if (hasIntersectingPreferences) {
            throw new KeepItPersonalException("Overlapping preferences in disabled and enabled preferences are not permitted");
        }
        else if (permissionLevel < 0 || permissionLevel > 4) {
            throw new KeepItPersonalException("Permission level must be between 0 and 4");
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            final var kipCommand = CommandManager.literal(KeepItPersonalCommand.NAME);

            dispatcher.register(
                    kipCommand.requires(Permissions.require(KeepItPersonalCommand.PERMISSION, permissionLevel))
                            .executes(new ViewDeathPreferencesCommand())
            );

            for (final var deathPreference : DeathPreference.values()) {
                if (!enabled.contains(deathPreference) && !disabled.contains(deathPreference)) {
                    final var permission = KeepItPersonalCommand.PERMISSION + '.' + deathPreference;
                    dispatcher.register(
                            kipCommand.then(CommandManager.literal(deathPreference.toString())
                                    .requires(Permissions.require(permission, permissionLevel))
                                    .executes(new GetDeathPreferenceCommand(deathPreference))
                                    .then(CommandManager.argument(ModifyDeathPreferenceCommand.PARAMETER, bool())
                                            .requires(Permissions.require(permission, permissionLevel))
                                            .executes(new ModifyDeathPreferenceCommand(deathPreference))
                                    )
                            )
                    );
                }
            }

            dispatcher.register(
                    kipCommand.then(CommandManager.literal(FillDeathPreferencesCommand.NAME)
                            .requires(Permissions.require(FillDeathPreferencesCommand.PERMISSION, permissionLevel))
                            .executes(new FillDeathPreferencesCommand())
                    )
            );

            dispatcher.register(
                    kipCommand.then(CommandManager.literal(ClearDeathPreferencesCommand.NAME)
                            .requires(Permissions.require(ClearDeathPreferencesCommand.PERMISSION, permissionLevel))
                            .executes(new ClearDeathPreferencesCommand())
                    )
            );
        });
    }
}
