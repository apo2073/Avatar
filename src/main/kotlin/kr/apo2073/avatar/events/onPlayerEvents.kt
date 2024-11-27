package kr.apo2073.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import kr.apo2073.avatar.Avatar
import kr.apo2073.avatar.utils.AvatarManager.fakePlayers
import kr.apo2073.avatar.utils.AvatarManager.invs
import kr.apo2073.avatar.utils.AvatarManager.spawnAvatar
import kr.apo2073.avatar.utils.ConfigManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class onPlayerEvents: Listener {
    @EventHandler
    fun onQuitPlayerEvent(e: PlayerQuitEvent) {
        try {
            val player = e.player
            val fakePlayer = spawnAvatar(player)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @EventHandler
    fun InteractFakePlayer(e: PlayerUseUnknownEntityEvent) {
        try {
            val player=e.player
            val entityId=e.entityId
            val fakeEntity = fakePlayers.find { it.bukkitEntity.entityId == entityId } ?: return
            fakeEntity.let {
                player.openInventory(invs[it.bukkitEntity.uniqueId] ?: return)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        Avatar.fakeServer.addPlayer(e.player)
        val player = e.player
        try {
            fakePlayers.find {
                it.bukkitEntity.uniqueId
                    .toString() == ConfigManager(player).getValue("fakePlayerUUID")
            }.apply {
                val inv   = invs[this?.bukkitEntity?.uniqueId] ?: return

                player.inventory.apply {
                    helmet = inv.getItem(3) ?: ItemStack(Material.AIR)
                    chestplate = inv.getItem(4) ?: ItemStack(Material.AIR)
                    leggings = inv.getItem(5) ?: ItemStack(Material.AIR)
                    boots = inv.getItem(6) ?: ItemStack(Material.AIR)
                    setItemInOffHand(inv.getItem(8))
                }

                for (index in inv.contents.indices) {
                    if (index >= inv.size) continue
                    val item = inv.contents[index] ?: ItemStack(Material.AIR)
                    if (index in listOf(0, 3, 4, 5, 6, 8)) continue
                    val targetSlot = index - 18
                    if (targetSlot in 0..53) {
                        player.inventory.setItem(targetSlot, item)
                    }
                }
                this?.remove() ?: return
            }?.remove()
            fakePlayers.filter { it.bukkitEntity.uniqueId!=ConfigManager(player).getValue("fakePlayerUUID") }.forEach { it.remove() }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }
    
    @EventHandler
    fun onClick(e:InventoryClickEvent) {
        if (e.view.title != "\uEBBB\uBBBB") return
        if (e.slot==0) e.isCancelled=true
        val player=e.whoClicked as Player
        val owner=Bukkit.getPlayer(e.inventory.getItem(0)!!.itemMeta.displayName.removeRange(0, 9)) ?: return
        if (owner.isOnline) {
            e.isCancelled=true
            player.closeInventory()
        }
        val uuid = UUID.fromString(e.inventory.getItem(0)!!.lore!!.first())
        val config=ConfigManager(Bukkit.getPlayer(uuid) ?: return)
        config.apply {
            IntRange(0, e.inventory.size)
                .forEach { setValue("inv.$it", e.inventory.contents[it] ?: ItemStack(Material.AIR)) }
        }
        
        synchronized(invs) {
            invs[uuid]=e.inventory
        }
    } 
}