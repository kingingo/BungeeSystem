package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandBroad extends Command implements Listener {

	public CommandBroad(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if (PermissionManager.getManager().hasPermission(p, PermissionType.BROADCAST, true)) {
			if (args.length == 0) {

				sender.sendMessage(Main.getTranslationManager().translate("prefix",sender)+ Main.getTranslationManager().translate("command.broad.nomessage",sender));
			} else {
				StringBuilder builder = new StringBuilder();
				if (args[0].startsWith("&h")) {
					args[0] = args[0].substring(2, args[0].length());
				} else {
					builder.append(Main.getTranslationManager().translate("prefix",sender));
				}

				for (String s : args) {
					builder.append(ChatColor.translateAlternateColorCodes('&', s));
					builder.append(" ");
				}
				String message = builder.substring(0, builder.length() - 1);
				Main.getDatenServer().getClient().broadcastMessage(null, message);
			}
		}
	}
}
//command.broad.nomessage - §cYou have to write a message!
