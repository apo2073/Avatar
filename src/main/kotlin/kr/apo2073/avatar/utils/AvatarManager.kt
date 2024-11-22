package kr.apo2073.avatar.utils

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeSkinParts
import io.github.monun.tap.mojangapi.MojangAPI
import kr.apo2073.avatar.Avatar
import kr.apo2073.lib.Items.ItemBuilder
import kr.apo2073.lib.Plugins.txt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

object AvatarManager {
    val plugin=Avatar.instane
    val fakePlayers get() = fakeServer.entities.filter { it.bukkitEntity is Player } as List<FakeEntity<Player>>
    private val fakeServer=Avatar.fakeServer
    val invs= mutableMapOf<UUID, Inventory>()
    private fun setInvs(player: Player) {
        val config=ConfigManager(player)
        invs[player.uniqueId]=Bukkit.createInventory(null, 9*5, Component.text("\uEBBB\uBBBB")).apply {
            setItem(
                0, ItemBuilder(Material.PLAYER_HEAD)
                    .setItemName(
                        txt("플레이어 ").decorate(TextDecoration.BOLD)
                            .append(txt(player.name).color(NamedTextColor.GREEN))
                    ).build()
            )
            listOf(
                player.inventory.helmet to 3,
                player.inventory.chestplate to 4,
                player.inventory.leggings to 5,
                player.inventory.boots to 6,
                player.inventory.itemInOffHand to 8
            ).forEach { (item, slot) ->
                item?.let { this.setItem(slot, it) } 
                    ?: setItem(slot, ItemStack(Material.AIR))
            }
            for (index in player.inventory.contents.indices) {
                val item = player.inventory.contents[index] ?: ItemStack(Material.AIR)
                if (index in listOf(39, 38, 37, 36, 40)) continue
                val targetSlot = 18 + index
                if (targetSlot < 45) {
                    this.setItem(targetSlot, item)
                }
            }
        }
    }
    
    fun spawnAvatar(player: Player): FakeEntity<Player> {
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
            pose= Pose.SLEEPING
        }
        setInvs(player)
        return fakePlayer
    }
}
