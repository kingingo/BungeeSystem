package dev.wolveringer.bs.listener;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TeamChatListener implements Listener{
	@EventHandler
	public void a(ChatEvent e){
		if(e.getSender() instanceof ProxiedPlayer && e.getMessage().startsWith("~") && PermissionManager.getManager().hasPermission((ProxiedPlayer) e.getSender(), PermissionType.TEAM_MESSAGE,false)){
			String message = "§3[Teamchat] §6"+((ProxiedPlayer)e.getSender()).getName()+": §5"+e.getMessage().substring(1);
			Main.getDatenServer().teamMessage(message);
			e.setCancelled(true);
		}
	}
}
