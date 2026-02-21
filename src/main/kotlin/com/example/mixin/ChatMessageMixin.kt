package com.example.mixin

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.*

@Mixin(ServerPlayNetworkHandler::class)
abstract class ChatMessageMixin {

    @Shadow
    lateinit var player: ServerPlayerEntity

    companion object {
        private val loginState: MutableMap<UUID, Boolean> = mutableMapOf()
        private const val CORRECT_PASSWORD = "SKNewRoles"
    }

    @Inject(method = ["onChatMessage"], at = [At("HEAD")], cancellable = true)
    private fun onChatMessage(packet: ChatMessageC2SPacket, ci: CallbackInfo) {
        val message = packet.chatMessage

        if (message.startsWith("/login ")) {
            val inputPassword = message.removePrefix("/login ").trim()

            if (inputPassword == CORRECT_PASSWORD) {
                loginState[player.uuid] = true
                player.sendMessage(Text.literal("✅ ログインに成功しました"), false)
            } else {
                player.sendMessage(Text.literal("❌ パスワードが間違っています"), false)
            }

            ci.cancel()
            return
        }

        if (loginState[player.uuid] != true) {
            player.sendMessage(
                Text.literal("🔒 チャットするにはログインしてください: /login <password> <email>"),
                false
            )
            ci.cancel()
        }
    }
}