package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.text.Text.literal;

public class ModifyDeathPreferenceCommand extends GetDeathPreferenceCommand {
    public static final String PARAMETER = "shouldKeep";
    private static final String ADD_SUCCESS_RESPONSE = "Successfully added preference!";
    private static final String ADD_FAILURE_RESPONSE = "Preference already added!";
    private static final String REMOVE_SUCCESS_RESPONSE = "Successfully removed preference!";
    private static final String REMOVE_FAILURE_RESPONSE = "Preference already removed!";

    public ModifyDeathPreferenceCommand(DeathPreference deathPreference) {
        super(deathPreference);
    }

    @Override
    public Integer run(CommandContext<ServerCommandSource> context, DeathPreferenceContainer container) {
        final var shouldKeep = getBool(context, PARAMETER);
        String message;

        if (shouldKeep) {
            if (container.addDeathPreference(deathPreference)) {
                message = ADD_SUCCESS_RESPONSE;
            }
            else {
                message = ADD_FAILURE_RESPONSE;
            }
        }
        else {
            if (container.removeDeathPreference(deathPreference)) {
                message = REMOVE_SUCCESS_RESPONSE;
            }
            else {
                message = REMOVE_FAILURE_RESPONSE;
            }
        }

        context.getSource().sendFeedback(() -> literal(message), false);

        return SINGLE_SUCCESS;
    }
}
