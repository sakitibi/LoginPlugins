package com.example.mixin

import com.example.auth.LoginManager
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayNetworkHandler::class)
class ChatMessageMixin {

    @Inject(method = ["onChatMessage"], at = [At("HEAD")], cancellable = true)
    private fun onChat(packet: ChatMessageC2SPacket, ci: CallbackInfo) {
        // @Suppress で「キャスト不要」という警告を消しつつ、安全にキャスト
        @Suppress("USELESS_CAST")
        val handler = this as Any as ServerPlayNetworkHandler
        val player = handler.player
        
        if (LoginManager.loginState[player.uuid] != true) {
            player.sendMessage(Text.literal("§6ログインしてください: /login <password>"), false)
            ci.cancel()
        }
    }

    @Inject(method = ["onCommandExecution"], at = [At("HEAD")], cancellable = true)
    private fun onCommand(packet: CommandExecutionC2SPacket, ci: CallbackInfo) {
        @Suppress("USELESS_CAST")
        val handler = this as Any as ServerPlayNetworkHandler
        val player = handler.player
        
        val command = packet.command()

        // loginコマンド以外かつ未ログインならブロック
        if (!command.startsWith("login") && LoginManager.loginState[player.uuid] != true) {
            player.sendMessage(Text.literal("§6ログインしてください: /login <password>"), false)
            ci.cancel()
        }
    }
}