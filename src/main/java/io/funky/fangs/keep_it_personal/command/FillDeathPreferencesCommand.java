package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.text.Text.translatable;

public class FillDeathPreferencesCommand extends KeepItPersonalCommand {
    public static final String NAME = "everything";
    public static final String PERMISSION = KeepItPersonalCommand.PERMISSION + '.' + NAME;
    private static final String PREFERENCES_FILLED = "keep-it-personal.message.preferences-filled";

    @Override
    public Integer run(CommandContext<ServerCommandSource> context, DeathPreferenceContainer container) {
        container.fillDeathPreferences();
        context.getSource().sendFeedback(() -> translatable(PREFERENCES_FILLED), false);
        return SINGLE_SUCCESS;
    }
}
