package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import net.impossibleworld.anticheat.Main
import net.impossibleworld.anticheat.configuration.LanguageConfig
import net.impossibleworld.anticheat.data.PlayerData
import net.impossibleworld.anticheat.utility.BanUtility
import net.impossibleworld.anticheat.utility.HexUtil
import org.bukkit.Bukkit

abstract class Check(val data: PlayerData, val name: String) {

    private val cfgLang : LanguageConfig = Main.instance.getWorkConfig()
    private val mainCfg = Main.instance.mainCfg

    var vl: Double = 0.0
    protected var buffer: Double = 0.0

    abstract fun handle(event: PacketReceiveEvent)

    protected fun fail(info: String) {
        if(data.isBanned) return

        vl++

        var alertMessage = cfgLang.getMessage("messages.alert")
        alertMessage = alertMessage.replace("%username%", data.user.name)
        alertMessage = alertMessage.replace("%fail_name%",name)
        alertMessage = alertMessage.replace("%info%",info)
        alertMessage = alertMessage.replace("%current_vl%",String.format("%.1f", vl))
        if(vl >= mainCfg.getConfig().getInt("settings.protectionAlertsVL")) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(mainCfg.getConfig().getString("settings.alert_permission")!!)) {
                    player.sendMessage(HexUtil.translate(alertMessage))
                }
            }
        }
        if (vl >= mainCfg.getConfig().getInt("settings.maxVL")) {
            data.isBanned = true
            val plugin = Bukkit.getPluginManager().getPlugin("AntiCheat")
            if(plugin != null) {
                Bukkit.getScheduler().runTask(plugin) { ->
                    val player = Bukkit.getPlayer(data.user.uuid)
                    if (player != null) {
                        BanUtility.runBanAnimation(plugin, player, name)
                    }
                }
            }
            vl = 0.0
        }
    }

    protected fun increaseBuffer(increase: Double, decrease: Double, threshold: Double): Boolean {
        buffer += increase
        if (buffer >= threshold) {
            buffer = threshold * 0.75
            return true
        }
        return false
    }

    protected fun decreaseBuffer(amount: Double) {
        buffer = (buffer - amount).coerceAtLeast(0.0)
    }

    fun decay() {
        if (vl > 0) vl -= 0.05 // Уменьшаем VL медленно со временем
    }
}