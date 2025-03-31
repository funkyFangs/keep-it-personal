package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.text.Text.literal;

public class ClearDeathPreferencesCommand extends KeepItPersonalCommand {
    public static final String NAME = "nothing";
    public static final String PERMISSION = KeepItPersonalCommand.PERMISSION + '.' + NAME;
    private static final String RESPONSE = "Cleared your preferences!";

    @Override
    public Integer run(CommandContext<ServerCommandSource> context, DeathPreferenceContainer container) {
        container.clearDeathPreferences();
        context.getSource().sendFeedback(() -> literal(RESPONSE), false);
        return SINGLE_SUCCESS;
    }
}
