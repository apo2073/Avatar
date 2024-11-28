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
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

object AvatarManager {
    private val plugin=Avatar.instance
    val fakePlayers get() = fakeServer.entities.filter { it.bukkitEntity is Player } as List<FakeEntity<Player>>
    private val fakeServer=Avatar.fakeServer
    val ENABLE_VIEW_ARMOR= plugin.config.getBoolean("enable-view-armor")
    val ENABLE_GUI_BARRIER= plugin.config.getBoolean("enable-gui-barrier")
    val invs= mutableMapOf<UUID, Inventory>()

    init {
        plugin.reloadConfig()
    }
    
    private fun fakePlayerGenerator(player: Player): FakeEntity<Player> {
        val uuid = MojangAPI.fetchProfile(player.name)!!.uuid()
        val profiles = MojangAPI.fetchSkinProfile(uuid)!!
            .profileProperties().toSet()
        return fakeServer.spawnPlayer(
            player.location,
            plugin.config
                .getString("avatar-name", "{name}").toString()
                .replace("{name}", player.name).replace("&", "§"),
            profiles,
            FakeSkinParts(plugin.config.getInt("skin-parts", 0b1111111)) //0b1111111
        )
    }
    fun spawnAvatar(player: Player): FakeEntity<Player> {
        val fakePlayer= fakePlayerGenerator(player).apply {
            updateMetadata {
                this.pose= Pose.SLEEPING
                if (ENABLE_VIEW_ARMOR) {
                    this.inventory.helmet=player.inventory.helmet
                    this.inventory.chestplate=player.inventory.chestplate
                    this.inventory.leggings=player.inventory.leggings
                    this.inventory.boots=player.inventory.boots
                    this.inventory.setItemInOffHand(player.inventory.itemInOffHand)
                    this.inventory.setItemInMainHand(player.inventory.itemInMainHand)
                }
            }
        }
        val config = ConfigManager(player)
        invs[fakePlayer.bukkitEntity.uniqueId]= Bukkit.createInventory(
            null, 9*6, Component.text("\uEBBB\uBBBB").color(NamedTextColor.WHITE)
        ).apply {
            setItem(
                0, ItemBuilder(Material.PLAYER_HEAD)
                    .setItemName(
                        txt("플레이어 ").decorate(TextDecoration.BOLD)
                            .append(txt(player.name).color(NamedTextColor.GREEN))
                    ).setOwner(player.name)
                    //.setLore(arrayListOf(fakePlayer.bukkitEntity.uniqueId.toString()))
                    .addItemFlag(ItemFlag.HIDE_ITEM_SPECIFICS)
                    .build()
            )
            listOf(
                player.inventory.helmet to 3,
                player.inventory.chestplate to 4,
                player.inventory.leggings to 5,
                player.inventory.boots to 6,
                player.inventory.itemInOffHand to 8
            ).forEach { (item, slot) ->
                item?.let { this.setItem(slot, it) } ?: setItem(slot, ItemStack(Material.AIR))
            }
            for (index in player.inventory.contents.indices) {
                val item = player.inventory.contents[index] ?: ItemStack(Material.AIR)
                if (index in listOf(39, 38, 37, 36, 40)) continue
                val targetSlot = 18 + index
                if (targetSlot>=54) continue
                this.setItem(targetSlot, item)
            }
        }
        config.apply { 
            setValue("fakePlayerUUID", fakePlayer.bukkitEntity.uniqueId.toString())
            val inv= invs[fakePlayer.bukkitEntity.uniqueId] ?: return@apply 
            IntRange(0, inv.size-1)
                .forEach { setValue("inv.$it", inv.contents[it]) }
        }
        return fakePlayer
    }
}
