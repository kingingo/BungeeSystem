package dev.wolveringer.bs.listener;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.information.InformationManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PingListener implements Listener {
	@EventHandler
	public void a(ProxyPingEvent e) {
		if (InformationManager.getManager() == null) {
			return;
		}
		String[] extraData = e.getConnection().getVirtualHost() != null && e.getConnection().getVirtualHost().getHostName() != null ? e.getConnection().getVirtualHost().getHostName().split("-", 2) : new String[0];
		if (extraData.length > 1) {
			if (extraData[1].equalsIgnoreCase("bungeeOnly")) {
				e.getResponse().setDescription(ChatColor.translateAlternateColorCodes('&', InformationManager.getManager().getInfo("motd1")) + "\n" + ChatColor.translateAlternateColorCodes('&', InformationManager.getManager().getInfo("motd2")));
				return;
			}
		}
		Players player = new Players(Integer.parseInt(InformationManager.getManager().getInfo("maxPlayers")), Main.getDatenServer().getPlayerCount(), new ServerPing.PlayerInfo[0]);
		e.getResponse().setPlayers(player);
		e.getResponse().setDescription(ChatColor.translateAlternateColorCodes('&', InformationManager.getManager().getInfo("motd1")) + "\n" + ChatColor.translateAlternateColorCodes('&', InformationManager.getManager().getInfo("motd2")));
	}
}
