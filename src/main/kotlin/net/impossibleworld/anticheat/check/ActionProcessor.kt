package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.DiggingAction
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem
import net.impossibleworld.anticheat.data.PlayerData

class ActionProcessor (data : PlayerData): Check(data, "ActionProcessor") {
    override fun handle(event: PacketReceiveEvent) {
       if(event.packetType == PacketType.Play.Client.USE_ITEM) {
           val wrapper = WrapperPlayClientUseItem(event)

           data.isUsingItem = true
       }
        if(event.packetType == PacketType.Play.Client.PLAYER_DIGGING) {
            val wrapper = WrapperPlayClientPlayerDigging(event)

            if(wrapper.action == DiggingAction.RELEASE_USE_ITEM ||
                wrapper.action == DiggingAction.DROP_ITEM ||
                wrapper.action == DiggingAction.DROP_ITEM_STACK
                ) {
                data.isUsingItem = false
            }
        }
        if(event.packetType == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            data.isUsingItem = false
        }
    }
}