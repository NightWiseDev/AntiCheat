package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import net.impossibleworld.anticheat.data.PlayerData
import org.bukkit.Bukkit

class HitboxCheck(data: PlayerData) : Check(data,"HitBoxCheck"){
    override fun handle(event: PacketReceiveEvent) {
        if(event.packetType != PacketType.Play.Client.INTERACT_ENTITY) return

        val wrapper = WrapperPlayClientInteractEntity(event)
        if(wrapper.action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return

        val plugin = Bukkit.getPluginManager().getPlugin("AntiCheat") ?: return
        Bukkit.getScheduler().runTask(plugin, { ->
            val player = Bukkit.getPlayer(data.user.uuid) ?: return@runTask

            // ОПТИМИЗАЦИЯ: Ищем сущность только вокруг игрока
            val target = player.getNearbyEntities(10.0, 10.0, 10.0)
                .firstOrNull { it.entityId == wrapper.entityId } ?: return@runTask

            // 1. Получаем реальный хитбокс
            val boundingBox = target.boundingBox

            val expansion = 0.35
            val expandedBox = boundingBox.clone().expand(expansion)

            val eyeLocation = player.eyeLocation
            val direction = eyeLocation.direction

            val rayTrace = expandedBox.rayTrace(eyeLocation.toVector(),direction,10.0)
            if(rayTrace == null) {
                if(increaseBuffer(1.0,0.1,7.0)) {
                    fail("Пропущенный хитбокс с большим отрывом > $expansion")
                }
            } else {
                decreaseBuffer(0.1)
            }
        })
    }
}