package io.u11.alerts2discord.util;

import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File

class Config<T>(private val type: Class<T>, private val file: File) {
    private val loader: HoconConfigurationLoader
    private var configNode: CommentedConfigurationNode
    var config: T

    init {
        if (type.getAnnotation(ConfigSerializable::class.java) == null) {
            throw IllegalStateException("Config objects have to be annotated with ConfigSerializable!")
        }

        this.loader = HoconConfigurationLoader.builder()
            .defaultOptions {
                it.shouldCopyDefaults(true)
            }.file(this.file)
            .build()
        this.configNode = this.loader.load()
        this.config = this.configNode.get(this.type) ?: throw IllegalStateException("Null Config")
    }

    fun load() {
        this.configNode = this.loader.load()
        this.config = this.configNode.get(this.type) ?: throw IllegalStateException("Null Config")
    }

    fun save() {
        this.configNode.set(this.config)
        this.loader.save(this.configNode)
    }

    operator fun invoke() = config
}
