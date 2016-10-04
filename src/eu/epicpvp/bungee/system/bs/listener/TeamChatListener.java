package eu.epicpvp.bungee.system.bs.listener;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TeamChatListener implements Listener{
	@EventHandler
	public void a(ChatEvent e){
		if(e.getSender() instanceof ProxiedPlayer && e.getMessage().startsWith("~") && PermissionManager.getManager().hasPermission((ProxiedPlayer) e.getSender(), PermissionType.TEAM_MESSAGE,false)){
			String message = "§cTeamchat §8| §a"+((ProxiedPlayer)e.getSender()).getName()+" §8» §f"+e.getMessage().substring(1);
			Main.getDatenServer().teamMessage(message);
			e.setCancelled(true);
		}
	}
}
