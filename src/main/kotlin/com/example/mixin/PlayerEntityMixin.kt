package com.example.mixin

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ServerPlayerEntity::class)
class PlayerEntityMixin : PlayerPersistentData {
    @Unique
    private var loginplugin_data: NbtCompound = NbtCompound()

    override fun loginplugin_getPersistentData(): NbtCompound = loginplugin_data

    override fun loginplugin_setPersistentData(nbt: NbtCompound) {
        loginplugin_data = nbt
    }

    @Inject(method = ["writeCustomDataToNbt"], at = [At("RETURN")])
    fun onWriteNbt(nbt: NbtCompound, ci: CallbackInfo) {
        nbt.put("LoginPluginData", loginplugin_data)
    }

    @Inject(method = ["readCustomDataFromNbt"], at = [At("RETURN")])
    fun onReadNbt(nbt: NbtCompound, ci: CallbackInfo) {
        if (nbt.contains("LoginPluginData")) {
            loginplugin_data = nbt.getCompound("LoginPluginData")
        }
    }
}
