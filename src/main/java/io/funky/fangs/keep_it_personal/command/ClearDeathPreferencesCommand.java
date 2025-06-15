package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.text.Text.translatable;

public class ClearDeathPreferencesCommand extends KeepItPersonalCommand {
    public static final String NAME = "nothing";
    public static final String PERMISSION = KeepItPersonalCommand.PERMISSION + '.' + NAME;
    private static final String PREFERENCES_CLEARED = "keep-it-personal.message.preferences-cleared";

    @Override
    public Integer run(CommandContext<ServerCommandSource> context, DeathPreferenceContainer container) {
        container.clearDeathPreferences();
        context.getSource().sendFeedback(() -> translatable(PREFERENCES_CLEARED), false);
        return SINGLE_SUCCESS;
    }
}
