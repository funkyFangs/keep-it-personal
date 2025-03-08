package io.funky.fangs.keep_it_personal.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class FillDeathPreferencesCommand: KeepItPersonalCommand() {
    companion object {
        const val NAME = "everything"
    }

    override fun run(
        keepCommandState: KeepCommandState,
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int {
        keepCommandState.fillPlayerPreferences(player.uuid)
        context.source.sendFeedback({ Text.literal("Updated your preferences!") }, false)
        return Command.SINGLE_SUCCESS
    }
}
