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

                            // 22行目付近を以下に差し替え
                            if (password == LoginManager.CORRECT_PASSWORD) {
                                LoginManager.loginState[player.uuid] = true
                                player.sendMessage(Text.literal("§aログイン成功"), false) // source ではなく player に直接送る
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