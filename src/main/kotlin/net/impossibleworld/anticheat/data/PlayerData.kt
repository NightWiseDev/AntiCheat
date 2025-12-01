package net.impossibleworld.anticheat.data

import com.github.retrooper.packetevents.protocol.player.User
import net.impossibleworld.anticheat.check.Check
import net.impossibleworld.anticheat.check.RotationCheck

class PlayerData (val user: User){

    val checks: List<Check> = listOf(RotationCheck(this))

    // Очки нарушений (Violation Level)
    private var impossiblePitchVL : Int = 0

    public var teleportTicks : Int = 0

    var lastYaw : Float? = null
    var lastPitch : Float? = null

    public fun getImpossiblePitch() : Int {
        return this.impossiblePitchVL
    }
    public fun addImpossiblePitch() {
        this.impossiblePitchVL++
    }
    fun tick() {
        if(teleportTicks > 0) teleportTicks--
        checks.forEach { it.decay() }
    }
}