package dev.wolveringer.bs.commands;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import dev.wolveringer.bs.Main;
import dev.wolveringer.permission.Group;
import dev.wolveringer.permission.Permission;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.permission.PermissionPlayer;
import dev.wolveringer.bukkit.permissions.PermissionType;
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
					name = Main.getDatenServer().getClient().getPlayerAndLoad(uuid).getName();
				}
				else if(args[1].length()<=16){
					uuid = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]).getUUID();
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
						cs.sendMessage("  §6Group §a"+g.getName()+"§7[§r"+g.getPrefix()+"§7] Importance: "+g.getImportance());
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
			else if(args[0].equalsIgnoreCase("groupReload")){
				Group g = PermissionManager.getManager().getGroup(args[1]);
				if(g != null){
					PermissionManager.getManager().getGroups().remove(g);
					g = PermissionManager.getManager().addGroup(args[1]);
					g.initPerms();
					cs.sendMessage("§aGroup reloaded");
				}
				else
				{
					cs.sendMessage("§cGroup not found.");
				}
				return;
			}
			else if(args[0].equalsIgnoreCase("addgroup")){
				Group g = PermissionManager.getManager().getGroup(args[1]);
				if(g != null){
					cs.sendMessage("§cGroup aredy exists!");
					return;
				}
				PermissionManager.getManager().addGroup(args[1]);
				cs.sendMessage("§aGroup sucessfuly added!");
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
			}else if(args[0].equalsIgnoreCase("importance")){
				Group group = PermissionManager.getManager().getGroup(args[1]);
				if(group == null){
					cs.sendMessage("§cGroup not found!");
					return;
				}
				if(!StringUtils.isNumeric(args[2])){
					cs.sendMessage("§cInvalid number!");
					return;
				}
				group.setImportance(Integer.parseInt(args[2]));
				cs.sendMessage("§cYou set the importance to "+group.getImportance());
			}
		}
		if(args.length == 4){
			if(args[0].equalsIgnoreCase("user")){
				UUID player = null;
				if(args[2].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")){
					player = UUID.fromString(args[2]);
				}
				else if(args[2].length()<=16){
					player = Main.getDatenServer().getClient().getPlayerAndLoad(args[2]).getUUID();
				}
				else{
					cs.sendMessage("§cPlayer \""+args[2]+"\" not found");
					return;
				}
				PermissionPlayer pp = PermissionManager.getManager().getPlayer(player);
				
				if(args[1].equalsIgnoreCase("addPerm")){
					if(!pp.addPermission(args[3])){
						cs.sendMessage("§cDie Permission konnte nicht hinzugefügt werden.");
						return;
					}
					cs.sendMessage("§aDie Permission "+args[3]+" wurde hinzugefügt.");
					return;
				}
				else if(args[1].equalsIgnoreCase("removePerm")){
					if(!pp.removePermission(args[3])){
						cs.sendMessage("§cDie Permission konnte nicht entfernt werden.");
						return;
					}
					cs.sendMessage("§aDie Permission "+args[3]+" wurde removed.");
					return;
				}
				else if(args[1].equalsIgnoreCase("addGroup")){
					if(!pp.addGroup(args[3])){
						cs.sendMessage("§cDie gruppe "+args[3]+" konnte nicht hinzugefügt werden.");
						return;
					}
					cs.sendMessage("§aDie Gruppe "+args[3]+" wurde hinzugefügt.");
					return;
				}
				else if(args[1].equalsIgnoreCase("removeGroup")){
					if(!pp.removeGroup(args[3])){
						cs.sendMessage("§cDie Gruppe "+args[3]+" konnte nicht entfernt werden.");
						return;
					}
					cs.sendMessage("§aDie Gruppe "+args[3]+" wurde removed.");
					return;
				}
			}
			else if(args[0].equalsIgnoreCase("group")){
				Group g = PermissionManager.getManager().getGroup(args[2]);
				if(g == null){
					cs.sendMessage("§cThis group does not exist.");
					return;
				}
				if(args[1].equalsIgnoreCase("addPerm")){
					if(!g.addPermission(args[3])){
						cs.sendMessage("§cDie Permission konnte nicht hinzugefügt werden.");
						return;
					}
					cs.sendMessage("§aDie Permission "+args[3]+" wurde hinzugefügt.");
					return;
				}
				if(args[1].equalsIgnoreCase("removePerm")){
					if(!g.removePermission(args[3])){
						cs.sendMessage("§cDie Permission konnte nicht entfernt werden.");
						return;
					}
					cs.sendMessage("§aDie Permission "+args[3]+" wurde removed.");
					return;
				}
			}
		}
		cs.sendMessage("§7/perm prefix <Group> '<Prefix>'");
		cs.sendMessage("§7/perm addGroup <Name>");
		cs.sendMessage("§7/perm importance <Group> <importance>");
		cs.sendMessage("§7/perm groupInfo <Group>");
		cs.sendMessage("§7/perm groupReload <Group>");
		cs.sendMessage("§7/perm userInfo <Player/UUID>");
		
		cs.sendMessage("§7/perm group addPerm <group> <permission>");
		cs.sendMessage("§7/perm group removePerm <group> <permission>");
		
		cs.sendMessage("§7/perm user addGroup <Player/UUID> <group>");
		cs.sendMessage("§7/perm user removeGroup <Player/UUID> <group>");
		
		cs.sendMessage("§7/perm user addPerm <Player/UUID> <perm>");
		cs.sendMessage("§7/perm user removePerm <Player/UUID> <perm>");
	}
}
