package com.example

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtString
import net.minecraft.server.MinecraftServer
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.UUID
import com.example.mixin.PlayerPersistentData

object HttpServer : ModInitializer {
    lateinit var server: MinecraftServer

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register { minecraftServer ->
            server = minecraftServer
            startHttpServer()
        }
    }

    fun startHttpServer() {
        val httpServer = HttpServer.create(InetSocketAddress("0.0.0.0", 8888), 0)
        httpServer.createContext("/cachePayment") { exchange ->
            if (exchange.requestMethod == "POST") {
                val requestBody = exchange.requestBody.bufferedReader().readText()
                val json = Gson().fromJson(requestBody, JsonObject::class.java)

                val uuidStr = json["uuid"]?.asString
                val cacheValue = json["cache"]?.asString

                if (uuidStr != null && cacheValue != null) {
                    val playerUuid = UUID.fromString(uuidStr)
                    val player = server.playerManager.getPlayer(playerUuid)

                    if (player != null) {
                        val data = (player as PlayerPersistentData).loginplugin_getPersistentData()
                        data.put("paymentCache", NbtString.of(cacheValue))
                        (player as PlayerPersistentData).loginplugin_setPersistentData(data)

                        println("✅ NBT 保存完了: $uuidStr -> $cacheValue")
                        exchange.sendResponseHeaders(200, 0)
                        exchange.responseBody.use { it.write("Saved".toByteArray()) }
                    } else {
                        exchange.sendResponseHeaders(404, 0)
                        exchange.responseBody.use { it.write("Player not found".toByteArray()) }
                    }
                } else {
                    exchange.sendResponseHeaders(400, 0)
                    exchange.responseBody.use { it.write("Bad request".toByteArray()) }
                }
            } else {
                exchange.sendResponseHeaders(405, 0)
                exchange.responseBody.use { it.write("Method not allowed".toByteArray()) }
            }
        }
        httpServer.executor = null
        httpServer.start()
        println("🔌 HTTP サーバー起動: http://<サーバーIP>:8888/cachePayment")
    }
}
