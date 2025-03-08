package io.funky.fangs.keep_it_personal.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ClearDeathPreferencesCommand: KeepItPersonalCommand() {
    companion object {
        const val NAME = "nothing"
    }

    override fun run(
        keepCommandState: KeepCommandState,
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        keepCommandState.clearPlayerPreferences(player.uuid)
        context.source.sendFeedback({ Text.literal("Cleared your preferences!") }, false)
        return Command.SINGLE_SUCCESS
    }
}
