package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation
import net.impossibleworld.anticheat.data.PlayerData
import kotlin.math.abs

class RotationCheck(data: PlayerData) : Check(data,"Rotation") {
    override fun handle(event: PacketReceiveEvent) {
       if(event.packetType != PacketType.Play.Client.PLAYER_ROTATION
           && event.packetType != PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
           return
       }
        var currentYaw = 0.0f
        var currentPitch = 0.0f

        if(event.packetType == PacketType.Play.Client.PLAYER_ROTATION) {
            val wrapper = WrapperPlayClientPlayerRotation(event)
            currentYaw = wrapper.yaw
            currentPitch = wrapper.pitch
        } else {
            val wrapper = WrapperPlayClientPlayerPositionAndRotation(event)
            currentYaw = wrapper.yaw
            currentPitch = wrapper.pitch
        }
        if(abs(currentPitch) > 90f) {
            fail("Pitch > 90", 1.0)
            event.isCancelled = true
            return
        }
        if(data.lastYaw != null && data.lastPitch != null) {
            val deltaYaw = getDeltaYaw(currentYaw, data.lastPitch!!)

            if(deltaYaw > 50f) {
                fail("Speed $deltaYaw",10.0)
            }
        }
    }
    private fun getDeltaYaw(yaw1: Float, yaw2: Float): Float {
        var difference = abs(yaw1 - yaw2) % 360
        if(difference > 180) {
            difference = 360 - difference
        }
        return difference
    }

}