package com.example.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import com.example.LoginState

object RegisterCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<ServerCommandSource>, _, _ ->
            dispatcher.register(
                CommandManager.literal("login")
                    .then(CommandManager.argument("password", StringArgumentType.word())
                        .then(CommandManager.argument("emails", StringArgumentType.word())
                            .executes { context ->
                                val password = StringArgumentType.getString(context, "password")
                                val emails = StringArgumentType.getString(context, "emails")

                                val server = context.source.server
                                val loginState = LoginState.get(server)

                                return@executes if (loginState.hasAnyoneLoggedIn()) {
                                    context.source.sendFeedback(Text.literal("❌ 誰かがすでにログインしています"), false)
                                    0
                                } else {
                                    // 必要に応じて UUID 等でログイン状態記録する処理をここに
                                    context.source.sendFeedback(
                                        Text.literal("✅ ログイン成功！パスワード: $password, メール: $emails"),
                                        false
                                    )
                                    1
                                }
                            }
                        )
                    )
            )
        }
    }
}
