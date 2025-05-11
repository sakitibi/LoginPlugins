package com.example.command

import com.example.LoginState
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.UUID

object RegisterCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<ServerCommandSource>, _, _ ->
            // /login <password> <emails>
            dispatcher.register(
                CommandManager.literal("login")
                    .then(CommandManager.argument("password", StringArgumentType.word())
                        .then(CommandManager.argument("emails", StringArgumentType.word())
                            .executes { context ->
                                val player: ServerPlayerEntity = context.source.player ?: run {
                                    context.source.sendFeedback(Text.literal("❌ プレイヤー情報を取得できません"), false)
                                    return@executes 0
                                }

                                val password = StringArgumentType.getString(context, "password")
                                val emails = StringArgumentType.getString(context, "emails")

                                val uuid: UUID = player.uuid
                                val server = context.source.server
                                val loginState = LoginState.get(server)

                                return@executes if (loginState.hasAnyoneLoggedIn()) {
                                    context.source.sendFeedback(Text.literal("❌ 誰かがすでにログインしています"), false)
                                    0
                                } else {
                                    loginState.waitingForConfirmation.add(uuid)
                                    context.source.sendFeedback(Text.literal("⚠ 本当にログインしますか？ (y/n)"), false)
                                    1
                                }
                            }
                        )
                    )
            )

            // /y
            dispatcher.register(
                CommandManager.literal("y")
                    .executes { context ->
                        val player: ServerPlayerEntity = context.source.player ?: run {
                            context.source.sendFeedback(Text.literal("❌ プレイヤー情報を取得できません"), false)
                            return@executes 0
                        }

                        val uuid = player.uuid
                        val server = context.source.server
                        val loginState = LoginState.get(server)

                        return@executes if (loginState.waitingForConfirmation.remove(uuid)) {
                            loginState.logins[uuid.toString()] = true
                            loginState.updateGlobalLoginScore(server)
                            context.source.sendFeedback(Text.literal("✅ ログインが確定しました"), false)
                            1
                        } else {
                            context.source.sendFeedback(Text.literal("❌ 質問中ではありません"), false)
                            0
                        }
                    }
            )

            // /n
            dispatcher.register(
                CommandManager.literal("n")
                    .executes { context ->
                        val player: ServerPlayerEntity = context.source.player ?: run {
                            context.source.sendFeedback(Text.literal("❌ プレイヤー情報を取得できません"), false)
                            return@executes 0
                        }

                        val uuid = player.uuid
                        val loginState = LoginState.get(context.source.server)

                        return@executes if (loginState.waitingForConfirmation.remove(uuid)) {
                            context.source.sendFeedback(Text.literal("❎ ログインをキャンセルしました"), false)
                            1
                        } else {
                            context.source.sendFeedback(Text.literal("❌ 質問中ではありません"), false)
                            0
                        }
                    }
            )
        }
    }
}
