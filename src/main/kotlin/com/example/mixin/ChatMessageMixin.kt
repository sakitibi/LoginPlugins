package com.example.mixin

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.*

@Mixin(ServerPlayNetworkHandler::class)
class ChatMessageMixin {

    companion object {
        // 仮のログイン状態マップ（本番はファイル保存やデータベースが望ましい）
        private val loginState: MutableMap<UUID, Boolean> = mutableMapOf()
        private const val CORRECT_PASSWORD = "SKNewRoles" // 本来は安全に管理する
    }

    @Inject(method = ["onChatMessage"], at = [At("HEAD")])
    fun onChatMessage(packet: ChatMessageC2SPacket, ci: CallbackInfo) {
        val handler = this as ServerPlayNetworkHandler
        val player: ServerPlayerEntity = handler.player
        val message = packet.chatMessage

        // チャットが "/login パスワード" の形式かどうかチェック
        if (message.startsWith("/login ")) {
            val inputPassword = message.removePrefix("/login ").trim()

            if (inputPassword == CORRECT_PASSWORD) {
                loginState[player.uuid] = true
                player.sendMessage(Text.literal("✅ ログインに成功しました"), false)
            } else {
                player.sendMessage(Text.literal("❌ パスワードが間違っています"), false)
            }

            // チャット内容を全体に送信しないようにキャンセル（オプション）
            ci.cancel()
        } else {
            // ログインしていない場合はチャット禁止（例）
            if (loginState[player.uuid] != true) {
                player.sendMessage(Text.literal("🔒 チャットするにはログインしてください: /login <password> <email>"), false)
                ci.cancel()
            }
        }
    }
}
