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
        if(data.teleportTicks > 0) {
            updateLastRotation(event)
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
            fail("Pitch > 90 ($currentPitch)")
            event.isCancelled = true
            return
        }
        if(data.lastYaw != null && data.lastPitch != null) {
            val deltaYaw = getDeltaYaw(currentYaw, data.lastYaw!!)

            val invalid = deltaYaw > 60f
            if(invalid) {
                // Плохое поведение
                // добавим в буфер 1.0. Если накопиться больше 5-ти то фейлим
                if(increaseBuffer(1.0,0.05,5.0)) {
                    // Это способ сбить киллауру у твари
                    event.isCancelled = true
                }
            } else {
                // Хорошее поведение
                decreaseBuffer(0.05)
            }
        }
        data.lastYaw = currentYaw
        data.lastPitch = currentPitch
    }
    private fun getDeltaYaw(yaw1: Float, yaw2: Float): Float {
        var difference = abs(yaw1 - yaw2) % 360
        if(difference > 180) {
            difference = 360 - difference
        }
        return difference
    }
    private fun updateLastRotation(event : PacketReceiveEvent) {
        if(event.packetType == PacketType.Play.Client.PLAYER_ROTATION) {
            val wrapper = WrapperPlayClientPlayerRotation(event)
            data.lastYaw = wrapper.yaw
            data.lastPitch = wrapper.pitch
        } else {
            val wrapper = WrapperPlayClientPlayerPositionAndRotation(event)
            data.lastYaw = wrapper.yaw
            data.lastPitch = wrapper.pitch
        }
    }

}