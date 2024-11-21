package kr.apo2073.avatar

import io.github.monun.tap.fake.FakeEntityServer
import kr.apo2073.avatar.events.onPlayerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

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
    }
}
