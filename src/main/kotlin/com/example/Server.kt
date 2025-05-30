package com.example

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object Server : ModInitializer {
    private val client = OkHttpClient()
    val url = System.getenv("RELAY_SERVER_URL") ?: "http://localhost:3004/api/send-email"
    private var tickCounter = 0
    private const val TICKS_PER_SEND = 100 // 約5秒ごと（1tick=50ms想定）

    // latestEmailがセットされると中継サーバーへ送信が走る
    var latestEmail: String? = null
        set(value) {
            field = value
            value?.let {
                println("📧 latestEmailセット: $it → 中継サーバーへ送信開始")
                sendEmailToRelayServer(it)
            }
        }

    override fun onInitialize() {
        // サーバーのTick終了ごとに呼ばれるイベント登録
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { server ->
            tickCounter++
            if (tickCounter >= TICKS_PER_SEND) {
                tickCounter = 0
                sendDataForAllPlayers(server)
            }
        })
    }

    private fun sendDataForAllPlayers(server: MinecraftServer) {
        val email = latestEmail
        if (email == null) {
            println("⚠️ latestEmailが未設定のため、プレイヤーデータ送信をスキップ")
            return
        }

        server.playerManager.playerList.forEach { player ->
            sendPlayerData(email, player)
        }
    }

    private fun sendPlayerData(email: String, player: ServerPlayerEntity) {
        val playerData = JSONObject().apply {
            put("hp", player.health)
            put("maxHp", player.maxHealth)
            put("food", player.hungerManager.foodLevel)
            put("xp", player.experienceLevel)
            put("uuid", player.uuidAsString)
        }

        val data = JSONObject().apply {
            put("email", email)
            put("playerName", player.name.string)
            put("data", playerData)
        }

        val requestBody = data.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url) // ← 中継サーバーのURLに変更必須
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ プレイヤーデータ送信失敗 (${player.name.string}): ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                println("✅ プレイヤーデータ送信成功 (${player.name.string}): ${response.code}")
                response.close()
            }
        })
    }

    /**
     * 中継サーバーにメールアドレスを送信する
     */
    private fun sendEmailToRelayServer(email: String) {
        val json = JSONObject().put("email", email).toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url) // ← 中継サーバーURLに変更必須
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ email送信失敗: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                println("✅ email送信成功: ${response.code} $body")
                response.close()

                if (response.isSuccessful) {
                    onEmailSentSuccess(email)
                } else {
                    println("⚠️ 中継サーバーからのエラー: $body")
                }
            }
        })
    }

    /**
     * email送信成功時の処理（必要に応じて実装）
     */
    private fun onEmailSentSuccess(email: String) {
        println("🎉 Email送信成功イベント発火: $email")
        // 例：フラグ立てやログ保存など
    }
}
