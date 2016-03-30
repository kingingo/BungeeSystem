package dev.wolveringer.bs.commands;


import java.util.UUID;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Permission.Group;
import me.kingingo.kBungeeCord.Permission.Permission;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionPlayer;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandPermission extends Command implements Listener {

	public CommandPermission(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if(cs instanceof ProxiedPlayer){
			ProxiedPlayer p = (ProxiedPlayer) cs;
			if(!PermissionManager.getManager().hasPermission(p, PermissionType.PERMISSION,true))
				return;
		}
		if(args.length == 2){
			if(args[0].equalsIgnoreCase("info")){
				UUID player = null;
				if(args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")){
					player = UUID.fromString(args[1]);
				}
				else if(args[1].length()<=16){
					player = Main.getDatenServer().getClient().getUUID(args[1]).getSync()[0].getUuid();
				}
				else{
					cs.sendMessage("§cPlayer \""+args[1]+"\" not found");
					return;
				}
				PermissionPlayer pp = PermissionManager.getManager().getPlayer(player);
				cs.sendMessage("§aPermissions for "+player);
				cs.sendMessage("§7Owned permissions:");
				for(Permission x : pp.getPermissions())
					cs.sendMessage("  §a"+x.getPermission()+"§7-§b"+x.getGroup());
				cs.sendMessage("§aGroup permissions:");
				for(Group g : pp.getGroups())
					if(g != null){
						cs.sendMessage("  §6Group §a"+g.getName()+"§7[§r"+g.getPrefix()+"§7]");
						for(Permission x : g.getPermissions())
							cs.sendMessage("  §a"+x.getPermission()+"§7-§b"+x.getGroup());
					}
				return;
			}
		}
		else if(args.length == 3){
			UUID player = null;
			if(args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")){
				player = UUID.fromString(args[1]);
			}
			else if(args[1].length()<=16){
				player = Main.getDatenServer().getClient().getUUID(args[1]).getSync()[0].getUuid();
			}
			else{
				cs.sendMessage("§cPlayer \""+args[1]+"\" not found");
				return;
			}
			PermissionPlayer pp = PermissionManager.getManager().getPlayer(player);
			
			if(args[0].equalsIgnoreCase("addPerms")){
				pp.addPermission(args[2]);
				cs.sendMessage("§aDie Permission "+args[2]+" wurde hinzugef§gt.");
				return;
			}
			else if(args[0].equalsIgnoreCase("removePerms")){
				pp.removePermission(args[2]);
				cs.sendMessage("§aDie Permission "+args[2]+" wurde removed.");
				return;
			}
			else if(args[0].equalsIgnoreCase("addGroup")){
				pp.addGroup(args[2]);
				cs.sendMessage("§aDie Gruppe "+args[2]+" wurde hinzugef§gt.");
				return;
			}
			else if(args[0].equalsIgnoreCase("removeGroup")){
				pp.removeGroup(args[2]);
				cs.sendMessage("§aDie Gruppe "+args[2]+" wurde removed.");
				return;
			}
		}
		cs.sendMessage("§7/perms addPerms <Player/UUID> <perm>");
		cs.sendMessage("§7/perms removePerms <Player/UUID> <perm>");
		cs.sendMessage("§7/perms addGroup <Player/UUID> <group>");
		cs.sendMessage("§7/perms removeGroup <Player/UUID> <group>");
	}
}
