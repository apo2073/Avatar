package kr.apo2073.avatar.utils

import kr.apo2073.avatar.Avatar
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException

class ConfigManager(private var player: Player) {
    private var plugin=Avatar.instance
    private var file: File
    private lateinit var config: YamlConfiguration
    init {
        file = getFilePath()
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
    }

    private fun getFilePath(): File {
        file=File(plugin.dataFolder, "avatar/${player.uniqueId}.yml")
        config=YamlConfiguration.loadConfiguration(file)
        return file
    }
    fun setValue(path: String, value: Any?) {
        try {
            config.set(path, value)
            config.save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun getValue(path: String): Any = config.get(path) ?: "NULL"
    fun getStringList(path: String): MutableList<String> = config.getStringList(path)
    fun getSection(path: String): ConfigurationSection? = config.getConfigurationSection(path)
    fun remove() { if (file.exists()) file.delete() }
}