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

    // Yarn 1.19.4 では player フィールドは playerEntity
    @Shadow
    lateinit var playerEntity: ServerPlayerEntity

    @Inject(
        method = ["onChatMessage"],
        at = [At("HEAD")],
        cancellable = true
    )
    private fun onChatMessage(
        packet: ChatMessageC2SPacket,
        ci: CallbackInfo
    ) {
        val player = playerEntity // playerEntity を player に alias
        val message = packet.chatMessage

        if (message.startsWith("/login ")) {
            val inputPassword = message.removePrefix("/login ").trim()

            if (inputPassword == LoginManager.CORRECT_PASSWORD) {
                LoginManager.loginState[player.uuid] = true
                player.sendMessage(Text.literal("ログイン成功"), false)
            } else {
                player.sendMessage(Text.literal("パスワードが違います"), false)
            }

            ci.cancel()
            return
        }

        if (LoginManager.loginState[player.uuid] != true) {
            player.sendMessage(Text.literal("ログインしてください: /login <password>"), false)
            ci.cancel()
        }
    }
}