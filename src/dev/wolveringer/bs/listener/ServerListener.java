package dev.wolveringer.bs.listener;

import dev.wolveringer.bs.Main;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener{
	@EventHandler
	public void a(PlayerDisconnectEvent e){
		System.out.println("Player disconnect. UUID: "+e.getPlayer().getUniqueId());
		Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()).unload();
		Main.getDatenServer().getClient().clearCacheForPlayer(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()));
	}
	@EventHandler
	public void a(ServerSwitchEvent e){
		Main.getDatenServer().getClient().getPlayer(e.getPlayer().getUniqueId()).setServerSync(e.getPlayer().getServer().getInfo().getName());
	}
}
