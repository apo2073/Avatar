package kr.apo2073.avatar

import io.github.monun.tap.fake.FakeEntityServer
import kr.apo2073.avatar.events.onPlayerEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Avatar : JavaPlugin() {
    companion object {
        lateinit var instance: Avatar
        lateinit var fakeServer: FakeEntityServer
    }
    
    override fun onEnable() {
        instance=this
        saveDefaultConfig()
        
        fakeServer=FakeEntityServer.create(this)
        server.scheduler.runTaskTimer(this, fakeServer::update, 0L, 1L)
        Bukkit.getOnlinePlayers().forEach { fakeServer.addPlayer(it) }
        
        server.pluginManager.registerEvents(onPlayerEvents(), this)
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().forEach { fakeServer.removePlayer(it) }
        fakeServer.shutdown()
    }
}
