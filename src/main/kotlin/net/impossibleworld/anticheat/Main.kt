package net.impossibleworld.anticheat

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import net.impossibleworld.anticheat.configuration.EnglishConfig
import net.impossibleworld.anticheat.configuration.LanguageConfig
import net.impossibleworld.anticheat.configuration.MainConfig
import net.impossibleworld.anticheat.configuration.RussianConfig
import net.impossibleworld.anticheat.listener.PacketHandler
import org.bukkit.plugin.java.JavaPlugin

public class Main : JavaPlugin() {

    public val debug = true

    companion object {
        lateinit var instance: Main
            private set
    }

    lateinit var engCfg: EnglishConfig
    lateinit var ruCfg: RussianConfig
    lateinit var mainCfg: MainConfig

    override fun onLoad() {
        instance = this

        mainCfg = MainConfig(this)
        engCfg = EnglishConfig(this)
        ruCfg = RussianConfig(this)

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings
            .checkForUpdates(false)

        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        PacketEvents.getAPI().init()

        PacketEvents.getAPI().eventManager.registerListener(PacketHandler(), PacketListenerPriority.LOWEST)
    }

    override fun onDisable() {
        PacketEvents.getAPI().terminate()
    }

    fun getWorkConfig(): LanguageConfig {
        return if (engCfg.getConfig().getBoolean("work")) {
            engCfg
        } else {
            ruCfg
        }
    }
}
