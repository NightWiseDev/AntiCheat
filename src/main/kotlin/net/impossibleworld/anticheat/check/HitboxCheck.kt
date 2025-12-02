package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import net.impossibleworld.anticheat.data.PlayerData

class HitboxCheck(data: PlayerData) : Check(data,"HitBoxCheck"){
    override fun handle(event: PacketReceiveEvent) {
    }


}