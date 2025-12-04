package net.impossibleworld.anticheat.configuration

import net.impossibleworld.anticheat.utility.HexUtil
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.logging.Level

class EnglishConfig(private val plugin: JavaPlugin) : LanguageConfig{

    private val fileName = "en.yml"
    private var configFile: File? = null
    private var fileConfiguration: FileConfiguration? = null

    init {
        saveDefaultConfig()
    }

    override fun reloadConfig() {
        if (configFile == null) {
            configFile = File(plugin.dataFolder, fileName)
        }

        // Загружаем конфигурацию
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile!!)

        // Проверяем, есть ли дефолтный файл внутри .jar для сравнения (чтобы подгрузить новые ключи)
        val defConfigStream = plugin.getResource(fileName)
        if (defConfigStream != null) {
            val defConfig = YamlConfiguration.loadConfiguration(
                InputStreamReader(defConfigStream, StandardCharsets.UTF_8)
            )
            fileConfiguration?.setDefaults(defConfig)
        }
    }

    fun getConfig(): FileConfiguration {
        if (fileConfiguration == null) {
            reloadConfig()
        }
        return fileConfiguration!!
    }

    fun saveConfig() {
        if (fileConfiguration == null || configFile == null) {
            return
        }
        try {
            getConfig().save(configFile!!)
        } catch (ex: IOException) {
            plugin.logger.log(Level.SEVERE, "Could not save config to $configFile", ex)
        }
    }

    fun saveDefaultConfig() {
        if (configFile == null) {
            configFile = File(plugin.dataFolder, fileName)
        }
        if (!configFile!!.exists()) {
            // saveResource создает файл, беря его из jar.
            // false означает "не перезаписывать, если уже есть"
            plugin.saveResource(fileName, false)
        }
    }

    override fun getMessage(path: String): String {
        val rawMessage = getConfig().getString(path) ?: return path
        return rawMessage
    }

    override fun getMessageList(path: String): List<String> {
        val rawList = getConfig().getStringList(path)
        return rawList
    }
}