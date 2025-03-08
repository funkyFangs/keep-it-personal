package io.funky.fangs.keep_it_personal.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ViewDeathPreferencesCommand: KeepItPersonalCommand() {
    override fun run(
        keepCommandState: KeepCommandState,
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        val preferences = keepCommandState.getPlayerPreferences(player.uuid)
        val message = Text.literal(
            "Keeping: [${preferences.joinToString(", ")}]"
        )
        context.source.sendFeedback({ message }, false)
        return Command.SINGLE_SUCCESS
    }
}
