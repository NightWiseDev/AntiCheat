package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import net.impossibleworld.anticheat.data.PlayerData
import org.bukkit.Bukkit

abstract class Check(val data: PlayerData, val name: String) {

    // У каждой проверке (чека) есть свой уровень нарушений. (VL)
    var vl: Double = 0.0

    // Буфферино копит в себе накопления о подозрениях
    protected var buffer: Double = 0.0

    // Этот метод должен быть реализован в каждой проверке по своему
    abstract fun handle(event: PacketReceiveEvent)

    protected fun fail(info: String, maxVl: Double) {
        vl++
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("ac.admin")) {
                player.sendMessage("Игрок $data.${player.name} провалил ($info) VL: $vl")
            }
            if (vl >= maxVl) {
                for (player in Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("ac.admin")) {
                        player.sendMessage("Игрок $data.${player.name} провалил ($info) VL: максимальный бань его нахуй")
                    }
                }
            }
        }
    }
    fun decay() {
        if(vl > 0) vl -= 0.5
    }
}