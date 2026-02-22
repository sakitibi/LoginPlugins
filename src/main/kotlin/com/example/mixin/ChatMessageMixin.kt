package com.example.mixin

import com.example.auth.LoginManager
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayNetworkHandler::class)
class ChatMessageMixin {

    // Shadowを使わず、メソッドの引数からプレイヤーを取得する安全な方法
    @Inject(method = ["onChatMessage"], at = [At("HEAD")], cancellable = true)
    private fun onChat(packet: net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket, ci: CallbackInfo) {
        val handler = this as Any as ServerPlayNetworkHandler
        val player = handler.player
        
        if (LoginManager.loginState[player.uuid] != true) {
            player.sendMessage(Text.literal("§6ログインしてください: /login <password>"), false)
            ci.cancel()
        }
    }

    @Inject(method = ["onCommandExecution"], at = [At("HEAD")], cancellable = true)
    private fun onCommand(packet: net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket, ci: CallbackInfo) {
        val handler = this as Any as ServerPlayNetworkHandler
        val player = handler.player
        val command = packet.command()

        // loginコマンド以外をすべてブロック
        if (!command.startsWith("login ") && LoginManager.loginState[player.uuid] != true) {
            player.sendMessage(Text.literal("§6ログインしてください: /login <password>"), false)
            ci.cancel()
        }
    }
}