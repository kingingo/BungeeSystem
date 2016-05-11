package dev.wolveringer.bs.listener;

import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerKickListener implements Listener{
	@EventHandler
	public void onServerKickEvent(ServerKickEvent ev) {
		if (ev.getKickReason().contains("Es joinen grad zu viele Spieler bitte versuch es später erneut") || ev.getKickedFrom().getName().startsWith("login")) {
			ev.getPlayer().disconnect(ev.getKickReason());
			return;
		}
		if (ev.getKickReason().contains("Wartungsmodus")) {
			ev.getPlayer().disconnect(ev.getKickReasonComponent());
			return;
		}
		
		ServerInfo kickedFrom = null;
		if (ev.getPlayer().getServer() != null) {
			kickedFrom = ev.getPlayer().getServer().getInfo();
		} else if (BungeeCord.getInstance().getReconnectHandler() != null) {
			kickedFrom = BungeeCord.getInstance().getReconnectHandler().getServer(ev.getPlayer());
		} else {
			kickedFrom = AbstractReconnectHandler.getForcedHost(ev.getPlayer().getPendingConnection());
			if (kickedFrom == null) {
				kickedFrom = ProxyServer.getInstance().getServerInfo(ev.getPlayer().getPendingConnection().getListener().getDefaultServer());
			}
		}

		ev.setCancelled(true);

		if (!LoginManager.getManager().isLoggedIn(ev.getPlayer())) {
			ev.setCancelServer(ServerManager.getManager().nextLoginLobby());
			ev.getPlayer().sendMessage("§c[Login-Fallback] Server: " + ev.getCancelServer().getName());
			ev.getPlayer().sendMessage("§c[Login-Fallback] " + BaseComponent.toLegacyText(ev.getKickReasonComponent()));
		} else {
			ev.setCancelServer(ServerManager.getManager().nextLobby());
			ev.getPlayer().sendMessage("§c[Fallback] Server: " + ev.getCancelServer().getName());
			ev.getPlayer().sendMessage("§c[Fallback] " + BaseComponent.toLegacyText(ev.getKickReasonComponent()));
		}
	}
}
