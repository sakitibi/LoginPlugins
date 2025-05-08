package com.example.command

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import com.example.LoginState // ここを追加！

object RegisterCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<ServerCommandSource>, _, _ ->
            dispatcher.register(
                CommandManager.literal("login").executes { context ->
                    val server = context.source.server
                    val loginState = LoginState.get(server) // LoginStateを使う
                    if (loginState.hasAnyoneLoggedIn()) {
                        context.source.sendFeedback(Text.literal("誰かがすでにログインしています"), false)
                    } else {
                        context.source.sendFeedback(Text.literal("ログインしました"), false)
                    }
                    1
                }
            )
        }
    }
}
