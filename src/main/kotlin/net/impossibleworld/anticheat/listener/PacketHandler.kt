package net.impossibleworld.anticheat.listener

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.UserDisconnectEvent
import com.github.retrooper.packetevents.event.UserLoginEvent
import net.impossibleworld.anticheat.data.PlayerData
import net.impossibleworld.anticheat.manager.PlayerDataManager

class PacketHandler : PacketListener {

    override fun onPacketReceive(event: PacketReceiveEvent) {
        val user = event.user ?: return
        val data = PlayerDataManager.getData(user) ?: return

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
}