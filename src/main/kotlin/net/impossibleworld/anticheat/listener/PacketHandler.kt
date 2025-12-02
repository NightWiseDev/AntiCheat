package net.impossibleworld.anticheat.listener

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.event.UserDisconnectEvent
import com.github.retrooper.packetevents.event.UserLoginEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon
import net.impossibleworld.anticheat.manager.PlayerDataManager

class PacketHandler : PacketListener {

    override fun onPacketSend(event: PacketSendEvent) {
        val user = event.user ?: return

        // Если сервер телепортирует игрока
        if (event.packetType == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            val data = PlayerDataManager.getData(user)
            data?.teleportTicks = 10
        }
    }

    override fun onPacketReceive(event: PacketReceiveEvent) {
        val user = event.user ?: return
        val data = PlayerDataManager.getData(user) ?: return

        if (isMovement(event.packetType)) {
            data.tick()
            data.lastFlyingTime= System.currentTimeMillis()
        }

        data.checks.forEach { check ->
            check.handle(event)
        }
    }

    override fun onUserDisconnect(event: UserDisconnectEvent) {
        val user = event.user ?: return
        PlayerDataManager.removeData(user)
    }

    override fun onUserLogin(event: UserLoginEvent) {
        val user = event.user ?: return
        PlayerDataManager.getData(user)
    }

    // Хелпер для определения пакетов движения
    private fun isMovement(type: PacketTypeCommon): Boolean {
        return type == PacketType.Play.Client.PLAYER_ROTATION ||
                type == PacketType.Play.Client.PLAYER_POSITION ||
                type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
    }
}