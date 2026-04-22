package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static java.util.stream.Collectors.joining;

public class ViewDeathPreferencesCommand extends KeepItPersonalCommand {
    private static final String MESSAGE_TEMPLATE = "Keeping: [%s]";

    @Override
    public Integer run(CommandContext<CommandSourceStack> context, DeathPreferenceContainer container) {
        final var preferences = container.getDeathPreferences();
        context.getSource().sendSuccess(() -> Component.literal(MESSAGE_TEMPLATE.formatted(
                preferences.stream()
                        .map(DeathPreference::toString)
                        .collect(joining(", "))
        )), false);
        return SINGLE_SUCCESS;
    }
}
