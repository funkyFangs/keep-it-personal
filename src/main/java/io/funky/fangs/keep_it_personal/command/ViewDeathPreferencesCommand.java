package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.server.command.ServerCommandSource;

import static java.util.stream.Collectors.joining;
import static net.minecraft.text.Text.literal;

public class ViewDeathPreferencesCommand extends KeepItPersonalCommand {
    private static final String MESSAGE_TEMPLATE = "Keeping: [%s]";

    @Override
    public Integer run(CommandContext<ServerCommandSource> context, DeathPreferenceContainer container) {
        final var preferences = container.getDeathPreferences();
        final var message = literal(MESSAGE_TEMPLATE.formatted(
                preferences.stream()
                        .map(DeathPreference::toString)
                        .collect(joining(", "))
        ));
        context.getSource().sendFeedback(() -> message, false);
        return SINGLE_SUCCESS;
    }
}
