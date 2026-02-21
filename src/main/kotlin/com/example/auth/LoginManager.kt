package com.example.auth

import java.util.*

object LoginManager {
    val loginState: MutableMap<UUID, Boolean> = mutableMapOf()
    const val CORRECT_PASSWORD = "SKNewRoles"
}