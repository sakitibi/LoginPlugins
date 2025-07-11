package com.example.command

import com.example.LoginState
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.*
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets

object RegisterCommand {

    private val questions = listOf(
        "利用規約に同意しますか？ (y/n)",
        "ウォーターチャレンジを批判しますか？ (y/n)",
        "名前は長い方が有利を批判しますか？ (y/n)",
        "マイ鉄ネットを批判しますか？ (y/n)",
        "餅尾戦争を支持しますか？ (y/n)",
        "README.htmlを読みましたか？ (y/n)"
    )

    // プレイヤーごとの質問状態（emailも含める）
    private val playerQuestionStates = mutableMapOf<UUID, PlayerQuestionState>()

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<ServerCommandSource>, _, _ ->

            dispatcher.register(
                CommandManager.literal("login")
                    .then(CommandManager.argument("password", StringArgumentType.word())
                        .then(CommandManager.argument("emails", StringArgumentType.word())
                            .then(CommandManager.argument("application-key", StringArgumentType.word()))
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

                                    val email = StringArgumentType.getString(context, "emails")
                                    val password = StringArgumentType.getString(context, "password")
                                    val applicationkeyAnswer = "TTJWaU5HVmhaR1l0T0RjeE5DMWtPV0poTFRabE5EQXRZelJqWXpabU16YzBOalpq";
                                    val applicationkey = StringArgumentType.getString(context, "application-key")

                                    fun deobfuscate(input: String): String {
                                        return String(Base64.getDecoder().decode(input))
                                    }

                                    if(applicationkey != deobfuscate(applicationkeyAnswer)){
                                        context.source.sendFeedback(Text.literal("❌ アプリパスワードが正しくありません"), false)
                                        return@executes 0
                                    }

                                    // emailとpasswordをPlayerQuestionStateに保存
                                    playerQuestionStates[uuid] = PlayerQuestionState(password, email)

                                    player.sendMessage(Text.literal(questions[0]), false)
                                    return@executes 1
                                }
                        )
                    )
            )

            dispatcher.register(
                CommandManager.literal("y")
                    .executes { context ->
                        val player = context.source.player ?: return@executes 0
                        handleAnswer(player, true, context.source.server)
                        return@executes 1
                    }
            )

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
            player.sendMessage(Text.literal(questions[state.currentQuestionIndex]), false)
        } else {
            playerQuestionStates.remove(uuid)

            if (state.answers.all { it }) {
                val hashedPassword = hashPassword(state.rawPassword)
                GlobalScope.launch(Dispatchers.IO) {
                    val paid = checkPaymentWithWix(state.email, hashedPassword)

                    // サーバースレッドに戻してMinecraftのメインスレッド処理を行う
                    server.execute {
                        if (paid) {
                            val loginState = LoginState.get(server)
                            loginState.logins[uuid.toString()] = true
                            loginState.updateGlobalLoginScore(server)
                            player.sendMessage(Text.literal("✅ ログインが完了しました！"), false)
                        } else {
                            player.sendMessage(Text.literal("❌ ログイン失敗。"), false)
                        }
                    }
                }
            } else {
                player.sendMessage(Text.literal("❌ ログインできません（質問の回答が条件未達）"), false)
            }

            println("Player ${player.name.string} answers: ${state.answers}")
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun checkPaymentWithWix(email: String, hashedPassword: String): Boolean {
        return try {
            val url = URL("https://12ninstudio.wixsite.com/_functions/paymentCheck")
            val json = """{"email":"$email","password":"$hashedPassword"}"""

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            conn.outputStream.use {
                it.write(json.toByteArray(StandardCharsets.UTF_8))
            }

            val responseCode = conn.responseCode
            val response = conn.inputStream.bufferedReader().readText()

            println("Wix Response ($responseCode): $response")
            response.contains("PAID")
        } catch (e: Exception) {
            println("Error contacting Wix: ${e.message}")
            false
        }
    }

    private data class PlayerQuestionState(
        val rawPassword: String,
        val email: String,
        var currentQuestionIndex: Int = 0,
        val answers: MutableList<Boolean> = mutableListOf()
    )
}
