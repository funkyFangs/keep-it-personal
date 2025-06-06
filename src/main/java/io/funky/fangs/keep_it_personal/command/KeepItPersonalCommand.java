package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static io.funky.fangs.keep_it_personal.KeepItPersonalModInitializer.MOD_ID;

public abstract class KeepItPersonalCommand implements Command<ServerCommandSource> {
    public static final int SINGLE_ERROR = -1;
    public static final String NAME = "kip";
    public static final String PERMISSION = MOD_ID + '.' + NAME;
    private static final String ERROR_RESPONSE = "This command can only be used by a player";

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        final var source = context.getSource();
        final var player = source.getPlayer();

        if (player instanceof DeathPreferenceContainer container) {
            final var result = run(context, container);
            return result == null ? SINGLE_ERROR : result;
        }
        else {
            source.sendError(Text.literal(ERROR_RESPONSE));
            return SINGLE_ERROR;
        }
    }

    public abstract Integer run(CommandContext<ServerCommandSource> context, DeathPreferenceContainer container);
}
