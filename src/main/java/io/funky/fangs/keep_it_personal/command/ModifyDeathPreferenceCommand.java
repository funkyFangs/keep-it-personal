package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static net.minecraft.text.Text.translatable;

public class ModifyDeathPreferenceCommand extends GetDeathPreferenceCommand {
    public static final String PARAMETER = "shouldKeep";
    private static final String PREFERENCE_ADDED = "keep-it-personal.messageKey.preference-added";
    private static final String PREFERENCE_ALREADY_ADDED = "keep-it-personal.messageKey.preference-already-added";
    private static final String PREFERENCE_REMOVED = "keep-it-personal.messageKey.preference-removed";
    private static final String PREFERENCE_ALREADY_REMOVED = "keep-it-personal.messageKey.preference-already-removed";

    public ModifyDeathPreferenceCommand(DeathPreference deathPreference) {
        super(deathPreference);
    }

    @Override
    public Integer run(CommandContext<ServerCommandSource> context, DeathPreferenceContainer container) {
        final var shouldKeep = getBool(context, PARAMETER);
        String messageKey;

        if (shouldKeep) {
            if (container.addDeathPreference(deathPreference)) {
                messageKey = PREFERENCE_ADDED;
            }
            else {
                messageKey = PREFERENCE_ALREADY_ADDED;
            }
        }
        else {
            if (container.removeDeathPreference(deathPreference)) {
                messageKey = PREFERENCE_REMOVED;
            }
            else {
                messageKey = PREFERENCE_ALREADY_REMOVED;
            }
        }

        context.getSource().sendFeedback(() -> translatable(messageKey), false);

        return SINGLE_SUCCESS;
    }
}
