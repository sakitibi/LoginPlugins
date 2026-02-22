package com.example

import com.example.auth.LoginManager
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text

class LoginPlugins : ModInitializer {
    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("login").then(
                    CommandManager.argument("password", StringArgumentType.word())
                        .executes { context ->
                            val player = context.source.player ?: return@executes 0
                            val password = StringArgumentType.getString(context, "password")
                            val server = context.source.server

                            if (password == LoginManager.CORRECT_PASSWORD) {
                                // 1. ログイン状態をメモリに保存
                                LoginManager.loginState[player.uuid] = true
                                player.sendMessage(Text.literal("§aログイン成功"), false)

                                // 2. LoginState（永続データ）にも保存し、スコアを更新する
                                val state = LoginState.get(server)
                                state.logins[player.uuid.toString()] = true
                                state.markDirty() // セーブ対象にする
                                state.updateGlobalLoginScore(server) // スコアボードを反映

                            } else {
                                player.sendMessage(Text.literal("§cパスワードが違います"), false)
                            }
                            1
                        }
                )
            )
        }
    }
}