package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import net.impossibleworld.anticheat.data.PlayerData

class KillAuraCheck(data: PlayerData) : Check(data,"killAura") {
    override fun handle(event: PacketReceiveEvent) {
        if(event.packetType == PacketType.Play.Client.INTERACT_ENTITY){
            val wrapper = WrapperPlayClientInteractEntity(event)

            if(wrapper.action == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                val delay = System.currentTimeMillis() - data.lastFlyingTime
                if(delay < 5) {
                    increaseBuffer(1.0,0.1,10.0)
                    fail("[KillAura] Удар до движения (Delay: $delay)")
                } else {
                    decreaseBuffer(0.1)
                }
            }
        }
    }
}