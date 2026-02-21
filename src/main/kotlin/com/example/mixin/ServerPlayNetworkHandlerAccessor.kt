package com.example.mixin

import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(ServerPlayNetworkHandler::class)
interface ServerPlayNetworkHandlerAccessor {

    @Accessor("player")
    fun getPlayer(): ServerPlayerEntity
}