package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import net.impossibleworld.anticheat.data.PlayerData
import net.impossibleworld.anticheat.utility.BanUtility
import net.impossibleworld.utility.IWUser.UserManager
import org.bukkit.Bukkit

abstract class Check(val data: PlayerData, val name: String) {

    var vl: Double = 0.0
    protected var buffer: Double = 0.0

    abstract fun handle(event: PacketReceiveEvent)

    protected fun fail(info: String) {
        if(data.isBanned) return

        vl++

        val alertMessage = "&#444444[&#ff3333I&#dd2222A&#aa0000C&#444444] &f${data.user.name} &7failed &#ff5555$name &8($info) &7VL: &#ffbb00${String.format("%.1f", vl)}"

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("ac.admin")) {
                UserManager.getUser(player).sendMessage(alertMessage)
            }
        }
        if (vl >= 15) {
            data.isBanned = true
            // Выполняем команду бана в основном потоке Bukkit (так безопаснее)
            val plugin = Bukkit.getPluginManager().getPlugin("AntiCheat")
            if(plugin != null) {
                Bukkit.getScheduler().runTask(plugin) { ->
                    val player = Bukkit.getPlayer(data.user.uuid)
                    if(player != null) {
                        BanUtility.runBanAnimation(plugin, player,name)
                    }
                }
            } else {
                println("ОШИБКА: Не найдено имя плагина для запуска анимации бана!")
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