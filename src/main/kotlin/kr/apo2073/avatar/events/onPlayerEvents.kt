package kr.apo2073.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import kr.apo2073.avatar.Avatar
import kr.apo2073.avatar.utils.AvatarManager.fakePlayers
import kr.apo2073.avatar.utils.AvatarManager.invs
import kr.apo2073.avatar.utils.AvatarManager.spawnAvatar
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class onPlayerEvents: Listener {
    private var plugin = Avatar.instane
    private var fakeServer=Avatar.fakeServer
    
    @EventHandler
    fun onQuitPlayerEvent(e: PlayerQuitEvent) {
        try {
            Avatar.fakeServer.removePlayer(e.player)
            val player=e.player
            val fakePlayer=spawnAvatar(player)
        } catch (e: Exception) {e.printStackTrace()}
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onUseUnknownEntity() {
        try {
            fakePlayers.find { it.bukkitEntity.entityId == entityId }?.let {
                player.openInventory(invs[it.bukkitEntity.uniqueId] ?: return)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        Avatar.fakeServer.addPlayer(e.player)
        val player=e.player
        val l=e.player.location
    }
}