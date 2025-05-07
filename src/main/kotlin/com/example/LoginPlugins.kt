package com.example

import net.fabricmc.api.ModInitializer
import com.example.command.RegisterCommand

object LoginPlugins : ModInitializer {
    override fun onInitialize() {
        RegisterCommand.register()
    }
}