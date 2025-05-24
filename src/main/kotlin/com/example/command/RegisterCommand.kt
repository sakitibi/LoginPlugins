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

    // 質問内容
    private val questions = listOf(
        "利用規約に同意しますか？ (y/n)",
        "名前は長い方が有利を批判しますか？ (y/n)",
        "README.htmlを読みましたか？ (y/n)"
    )

    // プレイヤーごとの質問状態
    private val playerQuestionStates = mutableMapOf<UUID, PlayerQuestionState>()

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

                                val uuid = player.uuid
                                val server = context.source.server
                                val loginState = LoginState.get(server)

                                if (loginState.hasAnyoneLoggedIn()) {
                                    context.source.sendFeedback(Text.literal("❌ 誰かがすでにログインしています"), false)
                                    return@executes 0
                                }

                                // 状態を初期化して最初の質問を送信
                                playerQuestionStates[uuid] = PlayerQuestionState()
                                player.sendMessage(Text.literal(questions[0]), false)
                                return@executes 1
                            }
                        )
                    )
            )

            // /y
            dispatcher.register(
                CommandManager.literal("y")
                    .executes { context ->
                        val player = context.source.player ?: return@executes 0
                        handleAnswer(player, true, context.source.server)
                        return@executes 1
                    }
            )

            // /n
            dispatcher.register(
                CommandManager.literal("n")
                    .executes { context ->
                        val player = context.source.player ?: return@executes 0
                        handleAnswer(player, false, context.source.server)
                        return@executes 1
                    }
            )
        }
    }

    private fun handleAnswer(player: ServerPlayerEntity, answer: Boolean, server: net.minecraft.server.MinecraftServer) {
        val uuid = player.uuid
        val state = playerQuestionStates[uuid]

        if (state == null) {
            player.sendMessage(Text.literal("❌ 質問中ではありません"), false)
            return
        }

        state.answers.add(answer)
        state.currentQuestionIndex++

        if (state.currentQuestionIndex < questions.size) {
            // 次の質問を送る
            player.sendMessage(Text.literal(questions[state.currentQuestionIndex]), false)
        } else {
            // 全質問に答え終わった
            playerQuestionStates.remove(uuid)

            if (state.answers.all { it }) {
                // 全て「はい」の場合にのみログイン成功
                val loginState = LoginState.get(server)
                loginState.logins[uuid.toString()] = true
                loginState.updateGlobalLoginScore(server)

                player.sendMessage(Text.literal("✅ ログインが完了しました！"), false)
            } else {
                player.sendMessage(Text.literal("❌ ログインできません"), false)
            }

            // 必要ならログに回答表示
            println("Player ${player.name.string} answers: ${state.answers}")
        }
    }

    private data class PlayerQuestionState(
        var currentQuestionIndex: Int = 0,
        val answers: MutableList<Boolean> = mutableListOf()
    )
}
