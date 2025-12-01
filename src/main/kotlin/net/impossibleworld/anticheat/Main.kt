package net.impossibleworld.anticheat

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import net.impossibleworld.anticheat.listener.PacketHandler
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {

    companion object {
        lateinit var instance: Main
        private set
    }
    override fun onLoad() {
        instance = this

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
}
