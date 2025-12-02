package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation
import net.impossibleworld.anticheat.data.PlayerData
import kotlin.math.abs
import kotlin.math.floor

class RotationCheck(data: PlayerData) : Check(data, "Rotation (Consistency)") {

    // Переменные для GCD чека
    private var lastDeltaYaw = 0.0f
    private var lastDeltaPitch = 0.0f

    // Буфер для проверки чувствительности
    private var gcdBuffer = 0.0

    override fun handle(event: PacketReceiveEvent) {
        if (event.packetType != PacketType.Play.Client.PLAYER_ROTATION
            && event.packetType != PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
        ) {
            return
        }

        if (data.teleportTicks > 0) {
            updateLastRotation(event)
            return
        }

        var currentYaw = 0.0f
        var currentPitch = 0.0f

        if (event.packetType == PacketType.Play.Client.PLAYER_ROTATION) {
            val wrapper = WrapperPlayClientPlayerRotation(event)
            currentYaw = wrapper.yaw
            currentPitch = wrapper.pitch
        } else {
            val wrapper = WrapperPlayClientPlayerPositionAndRotation(event)
            currentYaw = wrapper.yaw
            currentPitch = wrapper.pitch
        }

        // 1. Impossible Pitch (Это оставляем, так как смотреть вниз головой = 100% чит)
        if (abs(currentPitch) > 90f) {
            fail("Pitch > 90 ($currentPitch)")
            return
        }

        if (data.lastYaw != null && data.lastPitch != null) {
            val deltaYaw = getDeltaYaw(currentYaw, data.lastYaw!!)
            val deltaPitch = abs(currentPitch - data.lastPitch!!)

            // 2. GCD / Sensitivity Check
            // Эта проверка ловит "AimAssist" и "Smooth KillAura".
            // Она проверяет, соответствуют ли движения мыши реальному оборудованию.
            // Работает только на малых углах (когда киллаура "ведет" цель).
            if (deltaPitch > 0 && deltaPitch < 30 && deltaYaw > 0 && deltaYaw < 30) {
                checkGCD(deltaPitch.toDouble(), lastDeltaPitch.toDouble())
            }

            // Обязательно обновляем прошлые значения
            lastDeltaYaw = deltaYaw
            lastDeltaPitch = deltaPitch
        }

        data.lastYaw = currentYaw
        data.lastPitch = currentPitch
    }

    private fun checkGCD(currentDelta: Double, lastDelta: Double) {
        val pitchGCD = getGcd(currentDelta, lastDelta)

        // Если GCD слишком маленький, значит движение сгенерировано скриптом
        if (pitchGCD < 0.009) {
            gcdBuffer += 0.5
            // Порог 12.0 достаточно высокий, чтобы избежать случайных срабатываний
            if (gcdBuffer > 12.0) {
                fail("KillAura/AimAssist detected (Bad GCD: ${String.format("%.4f", pitchGCD)})")
                gcdBuffer = 6.0
            }
        } else {
            // Если игрок двигает мышкой честно - буфер падает
            gcdBuffer = (gcdBuffer - 0.05).coerceAtLeast(0.0)
        }
    }

    private fun getDeltaYaw(yaw1: Float, yaw2: Float): Float {
        var difference = abs(yaw1 - yaw2) % 360
        if (difference > 180) {
            difference = 360 - difference
        }
        return difference
    }

    private fun getGcd(a: Double, b: Double): Double {
        if (a < b) return getGcd(b, a)
        return if (abs(b) < 0.0001) a else getGcd(b, a - floor(a / b) * b)
    }

    private fun updateLastRotation(event: PacketReceiveEvent) {
        if (event.packetType == PacketType.Play.Client.PLAYER_ROTATION) {
            val wrapper = WrapperPlayClientPlayerRotation(event)
            data.lastYaw = wrapper.yaw
            data.lastPitch = wrapper.pitch
        } else {
            val wrapper = WrapperPlayClientPlayerPositionAndRotation(event)
            data.lastYaw = wrapper.yaw
            data.lastPitch = wrapper.pitch
        }
        lastDeltaYaw = 0.0f
        lastDeltaPitch = 0.0f
    }
}