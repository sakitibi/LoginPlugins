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

                            if (password == LoginManager.CORRECT_PASSWORD) {
                                LoginManager.loginState[player.uuid] = true
                                context.source.sendFeedback({ Text.literal("§aログイン成功") }, false)
                            } else {
                                context.source.sendFeedback({ Text.literal("§cパスワードが違います") }, false)
                            }
                            1
                        }
                )
            )
        }
    }
}