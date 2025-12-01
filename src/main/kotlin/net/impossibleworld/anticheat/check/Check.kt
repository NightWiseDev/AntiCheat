package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import net.impossibleworld.anticheat.data.PlayerData
import org.bukkit.Bukkit

abstract class Check(val data: PlayerData, val name: String) {

    var vl: Double = 0.0
    protected var buffer: Double = 0.0

    abstract fun handle(event: PacketReceiveEvent)

    protected fun fail(info: String) {
        vl++

        val alertMessage = "§c[AC] Игрок ${data.user.name} провалил $name ($info) VL: ${String.format("%.1f", vl)}"
        val banMessage = "§4[AC] Игрок ${data.user.name} был забанен за читы ($name)."

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("ac.admin")) {
                player.sendMessage(alertMessage)

                if (vl >= 15) {
                    player.sendMessage("§c--> БАН! (VL > 15)")
                }
            }
        }
        if (vl >= 15) {
            // Выполняем команду бана в основном потоке Bukkit (так безопаснее)
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("AntiCheat")!!) { ->
                // Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban ${data.user.name} Cheating ($name)")
                println(banMessage) // Пока просто пишем в консоль
            }
            // Сбрасываем VL, чтобы не спамить банами
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