package net.impossibleworld.anticheat.utility

import net.impossibleworld.anticheat.Main
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.cos
import kotlin.math.sin

object BanUtility {

    fun runBanAnimation(plugin: Plugin, player: Player, reason: String) {
        player.allowFlight = true
        player.isFlying = true

        val startLocation = player.location
        var ticks = 0

        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) {
                    this.cancel()
                    return
                }

                val currentLocation = player.location
                val newLocation = currentLocation.add(0.0, 0.15, 0.0)
                newLocation.yaw += 10f
                newLocation.pitch = 90f
                player.teleport(newLocation)

                spawnHelix(player.location)

                if (ticks >= 60) {
                    this.cancel()

                    // Эффекты взрыва
                    player.world.spawnParticle(Particle.EXPLOSION_HUGE, player.location, 1)
                    player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                    player.world.playSound(player.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f)

                    val command = Main.instance.mainCfg.getMessage("settings.ban_command")
                        .replace("%player%", player.name)
                        .replace("%reason%", reason)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
                    if (Main.instance.mainCfg.getConfig().getBoolean("settings.broadcast_ban")) {
                        Bukkit.broadcastMessage(
                            HexUtil.translate(
                                Main.instance.getWorkConfig().getMessage("messages.broadcast_ban_message")
                            )
                        )
                    }
                }
                ticks++
            }
        }.runTaskTimer(plugin, 0L, 1L) // Запускаем каждый тик
    }

    private fun spawnHelix(loc: Location) {
        val radius = 1.2
        for (y in 0..5 step 1) { // Несколько слоев частиц
            val angle = (System.currentTimeMillis() / 1000.0) * 2.0 + y // Вращение
            val x = cos(angle) * radius
            val z = sin(angle) * radius

            loc.world?.spawnParticle(Particle.FLAME, loc.clone().add(x, -1.0 + (y * 0.5), z), 0, 0.0, 0.0, 0.0)
            loc.world?.spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(-x, -1.0 + (y * 0.5), -z), 0, 0.0, 0.0, 0.0)
        }
    }
}