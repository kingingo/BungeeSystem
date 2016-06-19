package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandClearChat extends Command implements Listener {

	public CommandClearChat(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if (PermissionManager.getManager().hasPermission(p, PermissionType.CLEAR_CHAT,true)) {
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "chatclear", new DataBuffer().writeString(p.getServer().getInfo().getName()));
			for(ProxiedPlayer p1 : p.getServer().getInfo().getPlayers()){
				if(!PermissionManager.getManager().hasPermission(p1, PermissionType.CLEAR_CHAT,false)){
					for(int i = 0;i<100;i++)
						p1.sendMessage("");
				}
				p1.sendMessage(Main.getTranslationManager().translate("prefix",p1)+ Main.getTranslationManager().translate("command.chat.cleared",p1));
			}
		}
	}
	
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("chatclear")){
			for(ProxiedPlayer player : BungeeCord.getInstance().getServers().get(e.getBuffer().readString()).getPlayers()){
				if(!PermissionManager.getManager().hasPermission(player, PermissionType.CLEAR_CHAT,false)){
					for(int i = 0;i<100;i++)
						player.sendMessage("");
				}
				player.sendMessage(Main.getTranslationManager().translate("prefix",player)+ Main.getTranslationManager().translate("command.chat.cleared",player));
			}
		}
	}
}

//command.chat.cleared - §aThe chat is cleared now.