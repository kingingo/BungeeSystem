package eu.epicpvp.bungee.system.bs.commands;

import java.util.UUID;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.permission.Group;
import eu.epicpvp.bungee.system.permission.Permission;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.bungee.system.permission.PermissionPlayer;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import org.apache.commons.lang3.StringUtils;

public class CommandPermission extends Command implements Listener {

	public CommandPermission(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if (cs instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) cs;
			if (!PermissionManager.getManager().hasPermission(p, PermissionType.PERMISSION, true))
				return;
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("userInfo")) {
				UUID uuid = null;
				LoadedPlayer loadedPlayer = null;
				if (args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
					uuid = UUID.fromString(args[1]);
					loadedPlayer = Main.getDatenServer().getClient().getPlayerAndLoad(uuid);
				} else if (args[1].length() <= 16) {
					loadedPlayer = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
					uuid = loadedPlayer.getUUID();
				} else {
					cs.sendMessage("§cSpielername \"" + args[1] + "\" nicht valid");
					return;
				}
				PermissionPlayer pp = PermissionManager.getManager().getPlayer(uuid);
				cs.sendMessage("§aRechte von §b" + loadedPlayer.getName());
				cs.sendMessage("§aUUID: §b" + uuid);
				cs.sendMessage("§aPersönliche Berechtigungen:");
				for (Permission p : pp.getPermissions())
					cs.sendMessage("  §7- §b" + p.getPermission() + "§7-§b" + p.getGroup());
				cs.sendMessage("§aPersönliche negative Berechtigungen:");
				for (Permission p : pp.getNegativePermissions())
					cs.sendMessage("  §7- §b" + p.getPermission() + "§7-§b" + p.getGroup());
				cs.sendMessage("§aGruppenrechte:");
				for (Group g : pp.getGroups())
					if (g != null) {
						cs.sendMessage("  §6Gruppe §a" + g.getName() + "§7[§r" + g.getPrefix() + "§7] Importance: " + g.getImportance());
						for (Permission x : g.getPermissions())
							cs.sendMessage("  §7- §b" + x.getPermission() + "§7-§b" + x.getGroup());
						for (Permission x : g.getNegativePerms())
							cs.sendMessage("  §7- §b" + x.getPermission() + "§7-§b" + x.getGroup());
					}
				return;
			} else if (args[0].equalsIgnoreCase("groupInfo")) {
				Group g = PermissionManager.getManager().getGroup(args[1]);
				if (g == null) {
					cs.sendMessage("§cGruppe nicht gefunden.");
					return;
				}
				cs.sendMessage("§aGruppe: §e" + g.getName() + "§7[§r" + g.getPrefix() + "§7]");
				cs.sendMessage("§aVererbende Gruppen:");
				for (Group id : g.getInheritFrom()) {
					cs.sendMessage("  §7- " + id.getName());
				}
				cs.sendMessage("§aBerechtigungen:");
				for (Permission p : g.getPermissions()) {
					cs.sendMessage("  §7- " + p.getPermission() + "§7-§b" + p.getGroup());
				}
				cs.sendMessage("§aNegative Berechtigungen:");
				for (Permission p : g.getNegativePerms()) {
					cs.sendMessage("  §7- " + p.getPermission() + "§7-§b" + p.getGroup());
				}
				return;
			} else if (args[0].equalsIgnoreCase("groupReload")) {
				Group g = PermissionManager.getManager().getGroup(args[1]);
				if (g == null) {
					cs.sendMessage("§cGruppe nicht gefunden.");
					return;
				}
				PermissionManager.getManager().getGroups().remove(g);
				g = PermissionManager.getManager().addGroup(args[1]);
				g.initPerms();
				cs.sendMessage("§aGruppe neugeladen.");
				return;
			} else if (args[0].equalsIgnoreCase("addgroup")) {
				Group g = PermissionManager.getManager().getGroup(args[1]);
				if (g != null) {
					cs.sendMessage("§cDie Gruppe existiert bereits!");
					return;
				}
				PermissionManager.getManager().addGroup(args[1]);
				cs.sendMessage("§aGruppe erfolgreich hinzugefügt!");
				return;
			}
		}
		if (args.length >= 3) {
			if (args[0].equalsIgnoreCase("prefix")) {
				Group group = PermissionManager.getManager().getGroup(args[1]);
				if (group == null) {
					cs.sendMessage("§cGruppe nicht gefunden.");
					return;
				}
				String prefix = "";
				for (int i = 2; i < args.length; i++)
					prefix += " " + args[i];
				if (prefix.startsWith(" '") && prefix.endsWith("'")) {
					group.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix.replaceFirst(" '", "").substring(0, prefix.length() - 3)));
					String prefixToDisplay = group.getPrefix();
					String chatcolorDisplayed = "";
					if (ChatColor.stripColor(prefixToDisplay).trim().isEmpty()) {
						chatcolorDisplayed = " (ChatColor: " + prefixToDisplay.replace('§', '&') + " )";
					}
					cs.sendMessage("§aDas Prefix der Gruppe §6" + group.getName() + "§a wurde auf §6'§r" + prefixToDisplay + "§6'§a" + chatcolorDisplayed + " gesetzt.");
					return;
				} else {
					cs.sendMessage("§cBitte halte das Format ein!");
					return;
				}
			} else if (args[0].equalsIgnoreCase("importance")) {
				Group group = PermissionManager.getManager().getGroup(args[1]);
				if (group == null) {
					cs.sendMessage("§cGruppe nicht gefunden.");
					return;
				}
				if (!StringUtils.isNumeric(args[2])) {
					cs.sendMessage("§cBitte gebe eine gültige Zahl ein!");
					return;
				}
				group.setImportance(Integer.parseInt(args[2]));
				cs.sendMessage("§aDu hast die Importance der Gruppe §6" + group.getName() + "§a auf §6" + group.getImportance() + "§a gesetzt.");
			}
		}
		if (args.length == 4) {
			if (args[0].equalsIgnoreCase("user")) {
				UUID player = null;
				LoadedPlayer loadedPlayer = null;
				if (args[2].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
					player = UUID.fromString(args[2]);
					loadedPlayer = Main.getDatenServer().getClient().getPlayerAndLoad(player);
				} else if (args[2].length() <= 16) {
					loadedPlayer = Main.getDatenServer().getClient().getPlayerAndLoad(args[2]);
					player = loadedPlayer.getUUID();
				} else {
					cs.sendMessage("§cSpielername \"" + args[2] + "\" nicht valid.");
					return;
				}
				PermissionPlayer pp = PermissionManager.getManager().getPlayer(player);

				if (args[1].equalsIgnoreCase("addPerm")) {
					if (!pp.addPermission(args[3])) {
						cs.sendMessage("§cDie Berechtigung konnte dem Spieler §6" + loadedPlayer.getName() + "§c nicht hinzugefügt werden.");
						return;
					}
					cs.sendMessage("§aDie Berechtigung " + args[3] + " wurde dem Spieler §6" + loadedPlayer.getName() + "§a hinzugefügt.");
					return;
				} else if (args[1].equalsIgnoreCase("removePerm")) {
					if (!pp.removePermission(args[3])) {
						cs.sendMessage("§cDie Berechtigung konnte dem Spieler §6" + loadedPlayer.getName() + "§c nicht entfernt werden.");
						return;
					}
					cs.sendMessage("§aDie Berechtigung " + args[3] + " wurde dem Spieler §6" + loadedPlayer.getName() + "§a entfernt.");
					return;
				} else if (args[1].equalsIgnoreCase("addGroup")) {
					if (!pp.addGroup(args[3])) {
						cs.sendMessage("§cDie Gruppe " + args[3] + " konnte dem Spieler §6" + loadedPlayer.getName() + "§c nicht hinzugefügt werden.");
						return;
					}
					cs.sendMessage("§aDie Gruppe " + args[3] + " wurde dem Spieler §6" + loadedPlayer.getName() + "§a hinzugefügt.");
					return;
				} else if (args[1].equalsIgnoreCase("removeGroup")) {
					if (!pp.removeGroup(args[3])) {
						cs.sendMessage("§cDie Gruppe " + args[3] + " konnte dem Spieler §6" + loadedPlayer.getName() + "§c nicht entfernt werden.");
						return;
					}
					cs.sendMessage("§aDie Gruppe " + args[3] + " wurde dem Spieler §6" + loadedPlayer.getName() + "§a entfernt.");
					return;
				}
			} else if (args[0].equalsIgnoreCase("group")) {
				Group g = PermissionManager.getManager().getGroup(args[2]);
				if (g == null) {
					cs.sendMessage("§cDiese Gruppe existiert nicht.");
					return;
				}
				if (args[1].equalsIgnoreCase("addPerm")) {
					if (!g.addPermission(args[3])) {
						cs.sendMessage("§cDie Berechtigung konnte der Gruppe §6" + g.getName() + "§c nicht hinzugefügt werden.");
						return;
					}
					cs.sendMessage("§aDie Berechtigung §6" + args[3] + "§a wurde der Gruppe §6" + g.getName() + "§a hinzugefügt.");
					return;
				}
				if (args[1].equalsIgnoreCase("removePerm")) {
					if (!g.removePermission(args[3])) {
						cs.sendMessage("§cDie Berechtigung konnte der Gruppe §6" + g.getName() + "§c nicht entfernt werden.");
						return;
					}
					cs.sendMessage("§aDie Berechtigung §6" + args[3] + "§a wurde der Gruppe §6" + g.getName() + "§a entfernt.");
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
