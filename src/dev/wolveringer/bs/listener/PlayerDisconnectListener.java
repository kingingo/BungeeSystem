package dev.wolveringer.bs.listener;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerDisconnectListener implements Listener{
	@EventHandler
	public void a(PlayerDisconnectEvent e){
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName());
		player.setServerSync(null);
	}
}
