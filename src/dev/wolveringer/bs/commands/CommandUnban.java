package dev.wolveringer.bs.commands;

import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandUnban extends Command{
	public CommandUnban() {
		super("unban");
	}
	
	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!PermissionManager.getManager().hasPermission(cs, PermissionType.UNBAN,true))return;
		
		if(args.length == 1){
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(args[0]);
			if(!player.getBanStats("undefined").getSync().isActive()){
				cs.sendMessage("§cThis player isnt banned!");
			}
			player.banPlayer("undefined", "unban", "system", UUID.nameUUIDFromBytes("system".getBytes()), -2, System.currentTimeMillis(), "§cunbanned");
			cs.sendMessage("§aPlayer unbanned!");
			return;
		}
		cs.sendMessage("§cUsage: §6/unban <player>");
	}
}
