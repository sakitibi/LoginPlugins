plugins {
    kotlin("jvm") version "1.8.22"
    id("fabric-loom") version "1.2.8"
}

group = "login.plugins"
version = "3.2.0.2"

repositories {
    maven { url = uri("https://maven.fabricmc.net/") }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.4")
    mappings("net.fabricmc:yarn:1.19.4+build.1:v2")

    modImplementation("net.fabricmc:fabric-loader:0.18.4") // バージョンを少し上げると安定します
    
    // ここを修正：groupは "net.fabricmc.fabric-api" ですが、
    // 一般的な書き方は以下の通りです
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.87.2+1.19.4")

    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.5+kotlin.1.8.22")
}

kotlin {
    jvmToolchain(17)
}

loom {
    mixin {
        defaultRefmapName.set("loginplugins.refmap.json")
    }
}