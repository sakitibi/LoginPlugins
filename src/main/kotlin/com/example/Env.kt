package com.example

import java.util.*

object Env {
    private val props = Properties()

    init {
        val stream = this::class.java.classLoader.getResourceAsStream(".env")
        stream?.use {
            props.load(it)
        }
    }

    fun get(key: String): String? = props.getProperty(key)
}
