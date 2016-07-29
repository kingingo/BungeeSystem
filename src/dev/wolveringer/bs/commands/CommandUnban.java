package dev.wolveringer.bs.commands;

import java.util.List;
import java.util.UUID;

import dev.wolveringer.ban.BanServerMessageListener;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.Callback;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.ban.BanEntity;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.thread.ThreadFactory;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
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
			List<BanEntity> bans = (List<BanEntity>) player.getBanStats("undefined", 1).getSync();
			if(bans.size() == 0 || !bans.get(0).isActive()){
				cs.sendMessage("§cThis player isnt banned!");
				return;
			}
			
			ThreadFactory.getFactory().createThread(()->{
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
				BanServerMessageListener.getInstance().movePlayer(player, true).getAsync(new Callback<Boolean>() { //Kick/Move player
					@Override
					public void call(Boolean obj, Throwable exception) {
						if(exception != null || obj == false){
							if(BungeeCord.getInstance().getPlayer(player.getName()) != null)
								BungeeCord.getInstance().getPlayer(player.getName()).disconnect("§aUnbanned!"); 
							else
								player.kickPlayer("§aUnbanned!");
						}
					}
				},2000);
			}).start();
			
			player.banPlayer("undefined", "bungeecord", "system", UUID.nameUUIDFromBytes("system".getBytes()), -2, System.currentTimeMillis(), cs.getName());
			cs.sendMessage("§aPlayer unbanned!");
			return;
		}
		cs.sendMessage("§cUsage: §6/unban <player>");
	}
	
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis()-1461589132212L);
	}
}
