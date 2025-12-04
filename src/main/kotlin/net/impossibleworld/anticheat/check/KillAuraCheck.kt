package net.impossibleworld.anticheat.check

import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import net.impossibleworld.anticheat.Main
import net.impossibleworld.anticheat.configuration.LanguageConfig
import net.impossibleworld.anticheat.data.PlayerData

class KillAuraCheck(data: PlayerData) : Check(data,"killAura") {

    private val cfgLang : LanguageConfig = Main.instance.getWorkConfig()
    private val mainCfg = Main.instance.mainCfg

    override fun handle(event: PacketReceiveEvent) {
        if(event.packetType == PacketType.Play.Client.INTERACT_ENTITY){
            val wrapper = WrapperPlayClientInteractEntity(event)

            if(wrapper.action == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {

                // No Swing
                val timeSinceSwing = System.currentTimeMillis() - data.lastSwingTime
                if(timeSinceSwing > 1500) {
                    if (increaseBuffer(1.0, 0.1, 3.0)) {
                        fail(cfgLang.getMessage("fails.noswing"))
                        event.isCancelled = true
                    }
                }

                // AutoBlock / ImpossibleActions
                if(data.isUsingItem) {
                    if (increaseBuffer(1.0, 0.1, 5.0)) {
                        fail(cfgLang.getMessage("fails.autoblock"))
                        event.isCancelled = true
                    }
                }

                val delay = System.currentTimeMillis() - data.lastFlyingTime
                if(delay < 5) {
                    increaseBuffer(1.0,0.1,10.0)
                    fail(cfgLang.getMessage("fails.hit_movement").replace("%delay%", delay.toString()))
                } else {
                    decreaseBuffer(0.1)
                }
            }
        }
    }
}