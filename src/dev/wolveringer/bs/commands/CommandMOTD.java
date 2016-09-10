package dev.wolveringer.bs.commands;

import java.util.Arrays;

import dev.wolveringer.bs.information.InformationManager;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandMOTD extends Command {

	public CommandMOTD(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if (PermissionManager.getManager().hasPermission(cs, PermissionType.MOTD, true)) {
			if (args.length < 1) {
				cs.sendMessage("§cWrong Syntax:");
				cs.sendMessage("§a/motd set <line> <message>");
				cs.sendMessage("§a/motd get");
				return;
			}
			if (args[0].equalsIgnoreCase("set")) {
				Integer line = -1;
				line = Integer.parseInt(args[1]);
				if (line < 1 || line > 2) {
					cs.sendMessage("§cLine out of bounds!");
				}

				String message = "";
				for (String s : Arrays.copyOfRange(args, 2, args.length))
					message += s + " ";

				InformationManager.getManager().setInfo("motd" + line, message);
				cs.sendMessage("§aDu hast den MOTD erfolgrich gesetzt.");
			} else if (args[0].equalsIgnoreCase("get")) {
				cs.sendMessage("§aMotd:");
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', InformationManager.getManager().getInfo("motd1")));
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', InformationManager.getManager().getInfo("motd2")));
			}
		}
	}

}