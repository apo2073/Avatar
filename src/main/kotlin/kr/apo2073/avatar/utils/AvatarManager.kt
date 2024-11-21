package kr.apo2073.avatar.utils

import io.github.monun.tap.fake.FakeEntity
import kr.apo2073.avatar.Avatar
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.UUID

object AvatarManager {
    val plugin=Avatar.instane
    val fakePlayers get() = fakeServer.entities.filter { it.bukkitEntity is Player } as List<FakeEntity<Player>>
    private val fakeServer=Avatar.fakeServer
    val invs= mutableMapOf<UUID, Inventory>()
    fun setInvs(player: Player) {
        val config=ConfigManager(player)
        invs[player.uniqueId]=Bukkit.createInventory(null, 9*5,
            Component.text("${player.name}님의 인벤토리")).apply {
            //Array(45) {if (it<45) player.inventory.contents[it] else ItemStack(Material.AIR)}
        }
    }
}
