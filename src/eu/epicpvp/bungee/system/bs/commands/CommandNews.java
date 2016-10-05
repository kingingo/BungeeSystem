package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.message.MessageManager;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandNews extends Command {

	public CommandNews(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer))
			return;
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (PermissionManager.getManager().hasPermission(player, PermissionType.MOTD, true)) {
			if (args.length == 0) {
				player.sendMessage(Main.getTranslationManager().translate("prefix", sender) + "/news list");
				player.sendMessage(Main.getTranslationManager().translate("prefix", sender) + "/news remove [LANGUAGE] [B/T] [ID]");
				player.sendMessage(Main.getTranslationManager().translate("prefix", sender) + "/news add [LANGUAGE] [B/T] [NEWS]");
			} else {
				if (args[0].equalsIgnoreCase("list")) {
					for (LanguageType t : LanguageType.values()) {
						player.sendMessage("§aNews for language: " + t);
						int id = 0;
						player.sendMessage("  §aBroadcasts: ");
						for (String s : MessageManager.getmanager(t).getMessages()) {
							player.sendMessage(new ComponentBuilder("   §7- §r" + ChatColor.translateAlternateColorCodes('&', s)).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("§cClick to delete").create())).event(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/news remove " + t.getShortName() + " B " + id)).create());
							id++;
						}
						id = 0;
						player.sendMessage("  §aTitles: ");
						for (String s : MessageManager.getmanager(t).getTitles()) {
							player.sendMessage(new ComponentBuilder("   §7- §r" + ChatColor.translateAlternateColorCodes('&', s)).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("§cClick to delete").create())).event(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/news remove " + t.getShortName() + " T " + id)).create());
							id++;
						}
					}
				} else if (args[0].equalsIgnoreCase("remove")) {
					if (args[2].toLowerCase().startsWith("b"))
						args[2] = "BROADCAST";
					else if (args[2].toLowerCase().startsWith("t"))
						args[2] = "TITLE";
					else {
						player.sendMessage("§cMessage Type not found");
						return;
					}
					LanguageType lang = LanguageType.getLanguageFromName(args[1]);
					if (lang == null) {
						player.sendMessage("§cLanguage not found");
						return;
					}
					MessageManager manager = MessageManager.getmanager(lang);
					String m = manager.removeMessage(Integer.parseInt(args[3]), args[2]);
					player.sendMessage("§aMessage " + m + " §adeleted");
				} else if (args[0].equalsIgnoreCase("add")) {
					if (args[2].toLowerCase().startsWith("b"))
						args[2] = "BROADCAST";
					else if (args[2].toLowerCase().startsWith("t"))
						args[2] = "TITLE";
					else {
						player.sendMessage("§cMessage Type not found");
						return;
					}
					LanguageType lang = LanguageType.getLanguageFromName(args[1]);
					if (lang == null) {
						player.sendMessage("§cLanguage not found");
						return;
					}
					MessageManager manager = MessageManager.getmanager(lang);

					String message = "";
					for (int i = 3; i < args.length; i++)
						message += args[i] + " ";
					manager.addMessage(message, args[2]);
					player.sendMessage("§aMessage addded");
				}
			}
		}
	}
}
