package kr.apo2073.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import kr.apo2073.avatar.Avatar
import kr.apo2073.avatar.utils.AvatarManager.ENABLE_VIEW_ARMOR
import kr.apo2073.avatar.utils.AvatarManager.fakePlayers
import kr.apo2073.avatar.utils.AvatarManager.invs
import kr.apo2073.avatar.utils.AvatarManager.spawnAvatar
import kr.apo2073.avatar.utils.ConfigManager
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
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
            fakePlayer.rotate(player.yaw, player.pitch)
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
                player.playSound(Sound.sound(org.bukkit.Sound.BLOCK_CHEST_OPEN, Sound.Source.PLAYER, 1f, 1f))
                
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
    fun onClick(e: InventoryClickEvent) {
        val inventory = e.inventory
        val player = e.whoClicked as? Player ?: return

        if (e.clickedInventory?.type == InventoryType.PLAYER) {
            if (!e.isShiftClick) return
            e.isCancelled = true
        }
        if (!e.view.title.contains("\uEBBB\uBBBB")) return
        if (e.slot == 0) e.isCancelled = true
        if (e.slot in 9..17 || e.slot in listOf(1, 2, 7)) e.isCancelled = true
        if ((e.currentItem ?: return).type == Material.BARRIER) {
            e.isCancelled = true
            return
        }

        val ownerItem = inventory.getItem(0) ?: return
        val ownerName = ownerItem.itemMeta.displayName.removeRange(0, 9)
        val owner = Bukkit.getPlayer(ownerName) ?: return

        if (owner.isOnline) {
            e.isCancelled = true
            player.closeInventory()
            return
        }
        val uuidString = ConfigManager(owner).getValue("fakePlayerUUID").toString()
        val uuid = runCatching { UUID.fromString(uuidString) }.getOrNull() ?: return

        synchronized(invs) {
            invs[uuid] = inventory
        }

        val config = ConfigManager(owner)
        val inv=invs[uuid] ?: return
        IntRange(0, inv.size-1).forEach {
            config.setValue("inv.$it", inv.contents[it])
        }

        player.sendMessage(uuid.toString())
        if (!ENABLE_VIEW_ARMOR) return
        fakePlayers.find { it.bukkitEntity.uniqueId == uuid }?.updateEquipment {
            helmet=inventory.getItem(3) ?: ItemStack(Material.AIR)
            chestplate=inventory.getItem(4) ?: ItemStack(Material.AIR)
            leggings=inventory.getItem(5) ?: ItemStack(Material.AIR)
            boots=inventory.getItem(6) ?: ItemStack(Material.AIR)
            setItemInOffHand(inventory.getItem(8) ?: ItemStack(Material.AIR))
            setItemInMainHand(inventory.getItem(18) ?: ItemStack(Material.AIR))
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        if (!event.view.title.contains("\uEBBB\uBBBB")) return
        event.player.playSound(Sound.sound(org.bukkit.Sound.BLOCK_CHEST_CLOSE, Sound.Source.PLAYER, 1f, 1f))
    }
}