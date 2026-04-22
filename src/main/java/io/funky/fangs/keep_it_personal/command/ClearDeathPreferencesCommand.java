package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ClearDeathPreferencesCommand extends KeepItPersonalCommand {
    public static final String NAME = "nothing";
    public static final String PERMISSION = KeepItPersonalCommand.PERMISSION + '.' + NAME;
    private static final String RESPONSE = "Cleared your preferences!";

    @Override
    public Integer run(CommandContext<CommandSourceStack> context, DeathPreferenceContainer container) {
        container.clearDeathPreferences();
        context.getSource().sendSuccess(() -> Component.literal(RESPONSE), false);
        return SINGLE_SUCCESS;
    }
}
