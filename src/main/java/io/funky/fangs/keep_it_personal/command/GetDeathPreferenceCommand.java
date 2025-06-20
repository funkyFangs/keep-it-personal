package io.funky.fangs.keep_it_personal.command;

import com.mojang.brigadier.context.CommandContext;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.text.Text.literal;

public class GetDeathPreferenceCommand extends KeepItPersonalCommand {
    private static final String TEMPLATE = "Keeping %s: %b";

    protected final DeathPreference deathPreference;

    public GetDeathPreferenceCommand(DeathPreference deathPreference) {
        this.deathPreference = deathPreference;
    }

    @Override
    public Integer run(CommandContext<ServerCommandSource> context, DeathPreferenceContainer container) {
        final var keeping = container.hasDeathPreference(deathPreference);
        context.getSource().sendFeedback(() -> literal(TEMPLATE.formatted(deathPreference, keeping)), false);
        return SINGLE_SUCCESS;
    }
}
