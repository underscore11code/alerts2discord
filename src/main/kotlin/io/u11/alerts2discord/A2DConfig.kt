package io.u11.alerts2discord

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class A2DConfig(
    val discordToken: String = "TOKEN",
    val configs: Map<String, AlertConfig> = mapOf("default" to
        AlertConfig("CHANNEL_ID", "An alert fired!", "An alert is resolved!")
    )
) {
    val defaultConfig get() = configs["default"] ?: throw IllegalStateException("Could not find default config. This shouldn't be possible.")

    fun token() = System.getenv("A2D_TOKEN") ?: discordToken
}

@ConfigSerializable
data class AlertConfig(
    val discordChannelId: String,
    val messageFire: String,
    val messageResolve: String
) {
    private constructor() : this("null", "null", "null") // I love configurate, but it doesn't love me back unless this is here.
}
