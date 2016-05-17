package dev.wolveringer.bs.listener;

import java.util.concurrent.TimeUnit;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener{
	@EventHandler
	public void a(PlayerDisconnectEvent e){
		System.out.println("Player disconnect. UUID: "+e.getPlayer().getUniqueId());
		disconnectPlayer(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()));
		BungeeCord.getInstance().getScheduler().schedule(Main.getInstance(), ()->{
			if(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()) != null)
				if(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()).getServer().getSync() == null) //Player still disconnected
					Main.getDatenServer().getClient().clearCacheForPlayer(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()));
		}, 500, TimeUnit.MILLISECONDS);
	}
	
	public void disconnectPlayer(LoadedPlayer player){
		while (player != null) {
			try{
				player.setServerSync(null); //disconnect
				player = null;
			}catch(Exception e){
				e.getMessage();
			}
		}
	}
	
	@EventHandler
	public void a(ServerSwitchEvent e){
		Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getUniqueId()).setServerSync(e.getPlayer().getServer().getInfo().getName());
	}
}
