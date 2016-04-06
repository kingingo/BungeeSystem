package dev.wolveringer.bs.commands;

import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.ban.BanEntity;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandBanInfo extends Command{
	
	public CommandBanInfo() {
		super("baninfo");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_INFO,true)) return;
		
		if(args.length == 1){
			cs.sendMessage("§aLoading player...");
			LoadedPlayer player = null;

			if (args[0].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
				player = Main.getDatenServer().getClient().getPlayerAndLoad(UUID.fromString(args[0]));
			else {
				if (args[0].length() > 16) {
					cs.sendMessage("§cPlayer name cant be longer than 16.");
					return;
				}
				player = Main.getDatenServer().getClient().getPlayerAndLoad(args[0]);
			}
			BanEntity baned = player.getBanStats("system").getSync();
			cs.sendMessage("");
			cs.sendMessage("§6Baninformations for the player §e"+player.getName());
			if(baned.isActive())
				if(baned.isTempBanned()){
					cs.sendMessage("§6This player is §ntemporary§6 banned from this network.");
					cs.sendMessage("§6Ban expire in "+PlayerJoinListener.getDurationBreakdown(baned.getEnd()-System.currentTimeMillis()));
					cs.sendMessage("§6Level: §e"+baned.getLevel());
					cs.sendMessage("§6Reson: "+baned.getReson());
				}
				else
				{
					cs.sendMessage("§cThis player is §npermernatly§v banned from the network.");
					cs.sendMessage("§6Level: §e"+baned.getLevel());
					cs.sendMessage("§6Reson: §e"+baned.getReson());
				}
			else
				cs.sendMessage("§aThis player isnt banned ;)");
			return;
		}
		cs.sendMessage("§6/baninfo <Username/UUID>");
	}
	
}
