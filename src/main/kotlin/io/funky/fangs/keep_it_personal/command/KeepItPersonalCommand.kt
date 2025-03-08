package io.funky.fangs.keep_it_personal.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

const val KEEP_COMMAND = "keeping"
const val ARGUMENT_NAME = "shouldKeep"
const val SINGLE_ERROR = -1

abstract class KeepItPersonalCommand: Command<ServerCommandSource> {
    override fun run(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val server = source.server
        val player = source.player

        if (player != null) {
            val keepCommandState = KeepCommandState.getCurrentState(player.world.registryKey, server)
            return run(keepCommandState, context, player)
        }
        else {
            source.sendError(Text.literal("This command can only be used by a player"))
            return SINGLE_ERROR
        }
    }

    abstract fun run(
        keepCommandState: KeepCommandState,
        context: CommandContext<ServerCommandSource>,
        player: ServerPlayerEntity
    ): Int
}
