package net.impossibleworld.anticheat.manager

import com.github.retrooper.packetevents.protocol.player.User
import net.impossibleworld.anticheat.data.PlayerData
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object PlayerDataManager {

    private val userMap = ConcurrentHashMap<UUID, PlayerData>()


    fun getData(user : User) : PlayerData? {
        val uuid = user.uuid

        return userMap.computeIfAbsent(uuid) {
            PlayerData(user)
        }
    }
    fun removeData(user : User) {
        userMap.remove(user.uuid)
    }
    fun removeData(uuid : UUID) {
        userMap.remove(uuid)
    }
}