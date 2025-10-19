plugins {
    kotlin("jvm") version "1.8.22"
    id("fabric-loom") version "1.2.8"
    application
}

group = "login.plugins"
version = "3.1.0.0"

repositories {
    maven { url = uri("https://maven.fabricmc.net/") }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.4")
    mappings("net.fabricmc:yarn:1.19.4+build.1:v2") // 明示的にv2つけると安定します
    modImplementation("net.fabricmc:fabric-loader:0.14.21")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.87.2+1.19.4") // 最新のAPIにしてください
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.example.paymentrelay.MainKt")
}

loom {
    mixin {
        defaultRefmapName.set("loginplugin.refmap.json")
    }
}
