package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.LoadedPlayer;
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

public class CommandWhereIs extends Command implements Listener {

	public CommandWhereIs(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		
		if (args.length==0) {
			p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.whereis.message", sender,new String[]{p.getName(),p.getServer().getInfo().getName(),Main.getInstance().getServerId()}));
			return;
		}
		
		if (PermissionManager.getManager().hasPermission(p, PermissionType.WHERE_IS,true)) {
			if (args.length == 1) {
				String name = args[0];
				if(BungeeCord.getInstance().getPlayer(name) != null){
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.whereis.message", sender,new String[]{name,BungeeCord.getInstance().getPlayer(name).getServer().getInfo().getName(),Main.getInstance().getServerId()}));
				}else{
					p.sendMessage("§cSarching for the player...");
					LoadedPlayer tp = Main.getDatenServer().getClient().getPlayerAndLoad(name);
					if(tp.getServer().getSync() != null)
						Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "whereis", new DataBuffer().writeInt(Main.getDatenServer().getClient().getPlayerAndLoad(p.getName()).getPlayerId()).writeString(name));
					else
						p.sendMessage("§cPlayer isnt online on thin network!");
				}
			}
		}
	}
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("whereis")){
			int player = e.getBuffer().readInt();
			String user = e.getBuffer().readString();
			if(BungeeCord.getInstance().getPlayer(user) != null){
				ProxiedPlayer p = BungeeCord.getInstance().getPlayer(user);
				Main.getDatenServer().getClient().sendMessage(player, Main.getTranslationManager().translate("prefix")+Main.getTranslationManager().translate("command.whereis.message", new String[]{p.getName(),p.getServer().getInfo().getName(),Main.getInstance().getServerId()}));
			}
		}
	}

}
//command.whereis.message - §a%s0§7 is currently on server §e%s1§7 and BungeeCord §e%s2