package com.example.mixin

import com.example.auth.LoginManager
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayNetworkHandler::class)
abstract class ChatMessageMixin {

    @Inject(method = ["onChatMessage"], at = [At("HEAD")], cancellable = true)
    private fun onChatMessage(packet: ChatMessageC2SPacket, ci: CallbackInfo) {

        val handler = this as ServerPlayNetworkHandlerAccessor
        val player = handler.getPlayer()

        val message = packet.chatMessage()

        if (message.startsWith("/login ")) {
            val inputPassword = message.removePrefix("/login ").trim()

            if (inputPassword == LoginManager.CORRECT_PASSWORD) {
                LoginManager.loginState[player.uuid] = true
                player.sendMessage(Text.literal("ログインに成功しました"), false)
            } else {
                player.sendMessage(Text.literal("パスワードが間違っています"), false)
            }

            ci.cancel()
            return
        }

        if (LoginManager.loginState[player.uuid] != true) {
            player.sendMessage(
                Text.literal("チャットするにはログインしてください: /login <password>"),
                false
            )
            ci.cancel()
        }
    }
}