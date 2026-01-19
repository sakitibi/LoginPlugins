package com.example.util

import net.minecraft.nbt.NbtString
import net.minecraft.server.network.ServerPlayerEntity
import com.example.mixin.PlayerPersistentData

fun ServerPlayerEntity.setPaymentCache(value: String) {
    val data = (this as PlayerPersistentData).loginplugin_getPersistentData()
    data.put("paymentCache", NbtString.of(value))
}

fun ServerPlayerEntity.getPaymentCache(): String? {
    val data = (this as PlayerPersistentData).loginplugin_getPersistentData()
    return data.getString("paymentCache")
}
