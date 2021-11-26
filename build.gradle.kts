plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
}

group = "io.u11"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.javalin:javalin:4.1.1")
    implementation("net.dv8tion:JDA:4.3.0_346") {
        exclude(module = "opus-java")
    }
    implementation("org.spongepowered:configurate-hocon:4.1.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier.set("")
        // For docker, so I don't forget to update the version
        if (System.getProperty("io.u11.alerts2discord.ignoreversion").toBoolean()) {
            archiveVersion.set("")
        }
    }

    jar {
        enabled = false
    }

    application {
        mainClass.set("io.u11.alerts2discord.Alerts2DiscordKt")
    }
}
