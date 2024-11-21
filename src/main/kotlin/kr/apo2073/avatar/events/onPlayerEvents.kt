package kr.apo2073.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import io.github.monun.tap.fake.FakeSkinParts
import io.github.monun.tap.mojangapi.MojangAPI
import kr.apo2073.avatar.Avatar
import kr.apo2073.avatar.utils.AvatarManager.fakePlayers
import kr.apo2073.avatar.utils.AvatarManager.invs
import kr.apo2073.avatar.utils.AvatarManager.setInvs
import org.bukkit.entity.Pose
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class onPlayerEvents: Listener {
    private var plugin = Avatar.instane
    private var fakeServer=Avatar.fakeServer
    
    @EventHandler
    fun onQuitPlayerEvent(e: PlayerQuitEvent) {
        Avatar.fakeServer.removePlayer(e.player)
        val player=e.player
        val uuid = MojangAPI.fetchProfile(player.name)!!.uuid()
        val profiles = MojangAPI.fetchSkinProfile(uuid)!!
            .profileProperties().toSet()
        val fakePlayer=fakeServer.spawnPlayer(
            player.location,
            player.name,
            profiles,
            FakeSkinParts(0b1111111)
        )
        fakePlayer.updateMetadata {
            pose=Pose.SLEEPING
            setGravity(false)
        }
        setInvs(player)
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onUseUnknownEntity() {
        try {
            fakePlayers.find { it.bukkitEntity.entityId == entityId }?.let {
                player.openInventory(invs[it.bukkitEntity.uniqueId]!!)
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