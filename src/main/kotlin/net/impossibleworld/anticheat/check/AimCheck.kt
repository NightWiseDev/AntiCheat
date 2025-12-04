package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import net.impossibleworld.anticheat.Main
import net.impossibleworld.anticheat.configuration.LanguageConfig
import net.impossibleworld.anticheat.data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.GameMode

class AimCheck(data: PlayerData) : Check(data, "AimCheck (Raytrace)") {

    private val cfgLang : LanguageConfig = Main.instance.getWorkConfig()


    override fun handle(event: PacketReceiveEvent) {
        if (event.packetType != PacketType.Play.Client.INTERACT_ENTITY) return

        val wrapper = WrapperPlayClientInteractEntity(event)
        if (wrapper.action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return

        val plugin = Bukkit.getPluginManager().getPlugin("AntiCheat") ?: return
        Bukkit.getScheduler().runTask(plugin, { ->
            val player = Bukkit.getPlayer(data.user.uuid) ?: return@runTask

            // ОПТИМИЗАЦИЯ: Ищем сущность только вокруг игрока
            val target = player.getNearbyEntities(10.0, 10.0, 10.0)
                .firstOrNull { it.entityId == wrapper.entityId } ?: return@runTask

            // 1. Получаем реальный хитбокс
            val boundingBox = target.boundingBox

            // 2. Расширяем хитбокс (Lag Compensation Lite)
            // 0.4 - это хороший баланс. Если будут жалобы на кики при высоком пинге, поставь 0.5.
            val expandedBox = boundingBox.clone().expand(0.4)

            val eyeLocation = player.eyeLocation
            val direction = eyeLocation.direction

            // 3. Raytrace
            // Проверяем до 10 блоков, чтобы точно достать до цели
            val rayTrace = expandedBox.rayTrace(eyeLocation.toVector(), direction, 10.0)

            if (rayTrace == null) {
                // Игрок смотрит мимо даже расширенного хитбокса
                // 8.0 - довольно строгий порог буфера, но для Raytrace это нормально
                if (increaseBuffer(1.0, 0.1, 8.0)) {
                    fail(cfgLang.getMessage("fails.rayTracing"))
                }
            } else {
                // Игрок попал. Проверяем Reach.

                // Считаем точную точку попадания луча в хитбокс
                val exactDistance = rayTrace.hitPosition.distance(eyeLocation.toVector())

                // Лимиты: 3.0 (ванилла) + 0.8 (пинг/лаги) = 3.8
                // В Креативе дальность ~5.0
                val maxReach = if (player.gameMode == GameMode.CREATIVE) 5.5 else 3.8

                if (exactDistance > maxReach) {
                    if (increaseBuffer(1.0, 0.2, 10.0)) {
                        var message = cfgLang.getMessage("fails.reach")
                        message = message.replace("%maxReach%", maxReach.toString())
                        message = message.replace("%exactDistance%", String.format("%.2f",exactDistance.toString()))
                        fail(message)
                    }
                } else {
                    decreaseBuffer(0.1)
                }
            }
        })

    }
}