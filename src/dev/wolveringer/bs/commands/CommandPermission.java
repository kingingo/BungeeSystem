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
			if(args[0].equalsIgnoreCase("userInfo")){
				UUID uuid = null;
				String name = null;
				if(args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")){
					uuid = UUID.fromString(args[1]);
					name = Main.getDatenServer().getClient().getPlayer(uuid).getName();
				}
				else if(args[1].length()<=16){
					uuid = Main.getDatenServer().getClient().getUUID(args[1]).getSync()[0].getUuid();
				}
				else{
					cs.sendMessage("§cPlayer \""+args[1]+"\" not found");
					return;
				}
				PermissionPlayer pp = PermissionManager.getManager().getPlayer(uuid);
				cs.sendMessage("§aPermissions for §b"+name);
				cs.sendMessage("§aUUID: §b"+uuid);
				cs.sendMessage("§aOwned permissions:");
				for(Permission x : pp.getPermissions())
					cs.sendMessage("  §7- §b"+x.getPermission()+"§7-§b"+x.getGroup());
				cs.sendMessage("§aOwned negative permissions:");
				for(Permission x : pp.getNegativePermissions())
					cs.sendMessage("  §7- §b"+x.getPermission()+"§7-§b"+x.getGroup());
				cs.sendMessage("§aGroup permissions:");
				for(Group g : pp.getGroups())
					if(g != null){
						cs.sendMessage("  §6Group §a"+g.getName()+"§7[§r"+g.getPrefix()+"§7]");
						for(Permission x : g.getPermissions())
							cs.sendMessage("  §7- §b"+x.getPermission()+"§7-§b"+x.getGroup());
						for(Permission x : g.getNegativePerms())
							cs.sendMessage("  §7- §b"+x.getPermission()+"§7-§b"+x.getGroup());
					}
				return;
			}
			else if(args[0].equalsIgnoreCase("groupInfo")){
				Group g = PermissionManager.getManager().getGroup(args[1]);
				if(g != null){
					cs.sendMessage("§aGroup: §e"+g.getName()+"§7[§r"+g.getPrefix()+"§7]");
					cs.sendMessage("§aGroup instances:");
					for(Group id : g.getInstances()){
						cs.sendMessage("  §7- "+id.getName());
					}
					cs.sendMessage("§aPermissions:");
					for(Permission p : g.getPermissions()){
						cs.sendMessage("  §7- "+p.getPermission());
					}
					cs.sendMessage("§aNegative Permissions:");
					for(Permission p : g.getNegativePerms()){
						cs.sendMessage("  §7- "+p.getPermission());
					}
				}
				else
				{
					cs.sendMessage("§cGroup not found.");
				}
				return;
			}
		}
		if(args.length >= 3){
			if(args[0].equalsIgnoreCase("prefix")){
				Group group = PermissionManager.getManager().getGroup(args[1]);
				if(group == null){
					cs.sendMessage("§cGroup not found!");
					return;
				}
				String prefix = "";
				for(int i = 2;i<args.length;i++)
					prefix+=" "+args[i];
				if(prefix.startsWith(" '") && prefix.endsWith("'")){
					group.setPrefix(prefix.replaceFirst(" '", "").substring(0, prefix.length()-3).replaceAll("&", "§"));
					cs.sendMessage("§aPrefix gesetzt auf '"+group.getPrefix()+"'");
					return;
				}
				else{
					cs.sendMessage("§cBitte halte das Format ein!");
					return;
				}
			}
		}
		if(args.length == 3){
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
		cs.sendMessage("§7/perms prefix <Group> '<Prefix>'");
		cs.sendMessage("§7/perms addPerms <Player/UUID> <perm>");
		cs.sendMessage("§7/perms removePerms <Player/UUID> <perm>");
		cs.sendMessage("§7/perms addGroup <Player/UUID> <group>");
		cs.sendMessage("§7/perms removeGroup <Player/UUID> <group>");
		cs.sendMessage("§7/perms userInfo <Player/UUID>");
		cs.sendMessage("§7/perms groupInfo <Group>");
	}
}
