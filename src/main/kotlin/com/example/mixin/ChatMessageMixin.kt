package com.example.mixin

import com.example.auth.LoginManager
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayNetworkHandler::class)
class ChatMessageMixin {

    @Shadow
    lateinit var player: ServerPlayerEntity // 1.19.4 では player フィールド名を確認してください

    @Inject(
        method = ["onCommandExecution"], // チャットではなくコマンド実行をターゲットにする
        at = [At("HEAD")],
        cancellable = true
    )
    private fun onCommandExecution(
        packet: net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket,
        ci: CallbackInfo
    ) {
        val command = packet.command
        
        // loginコマンドの処理
        if (command.startsWith("login ")) {
            val inputPassword = command.removePrefix("login ").trim()

            if (inputPassword == LoginManager.CORRECT_PASSWORD) {
                LoginManager.loginState[player.uuid] = true
                player.sendMessage(Text.literal("ログイン成功"), false)
            } else {
                player.sendMessage(Text.literal("パスワードが違います"), false)
            }

            ci.cancel() // 本物のコマンド処理へ行かせない
            return
        }

        // 未ログイン状態なら他のコマンドも禁止する
        if (LoginManager.loginState[player.uuid] != true) {
            player.sendMessage(Text.literal("ログインしてください: /login <password>"), false)
            ci.cancel()
        }
    }
}