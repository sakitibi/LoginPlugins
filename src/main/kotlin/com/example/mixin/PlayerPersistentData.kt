package com.example.mixin

import net.minecraft.nbt.NbtCompound

interface PlayerPersistentData {
    fun loginplugin_getPersistentData(): NbtCompound
    fun loginplugin_setPersistentData(nbt: NbtCompound)
}
