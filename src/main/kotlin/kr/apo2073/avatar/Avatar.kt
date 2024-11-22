package kr.apo2073.avatar

import io.github.monun.tap.fake.FakeEntityServer
import kr.apo2073.avatar.events.onPlayerEvents
import kr.apo2073.avatar.utils.AvatarManager.spawnAvatar
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.contracts.Returns

class Avatar : JavaPlugin() {
    companion object {
        lateinit var instane: Avatar
        lateinit var fakeServer: FakeEntityServer
    }
    
    override fun onEnable() {
        instane=this
        
        fakeServer=FakeEntityServer.create(this)
        server.scheduler.runTaskTimer(this, fakeServer::update, 0L, 1L)
        
        server.pluginManager.registerEvents(onPlayerEvents(), this)
        
        getCommand("av")?.setExecutor { sender, _, _, args -> 
            if (sender !is Player) false
            val fakePlayer=spawnAvatar(sender as Player)
            true
        }
    }
}
