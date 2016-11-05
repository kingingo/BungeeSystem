package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.listener.PlayerJoinListener;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandAntibot extends Command {

	public CommandAntibot() {
		super("antibots");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!PermissionManager.getManager().hasPermission(sender, PermissionType.ANTIBOTS, true)) {
			return;
		}
		if (args.length == 0) {
			sender.sendMessage("§eAntiBot-System");
			sender.sendMessage("§c/antibots togglelog §7- §6Schaltet das AntiBots-Logging an/aus.");
			sender.sendMessage("§c/antibots reloadfiles §7- §6Lädt die Antibot-Dateien erneut.");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("togglelog")) {
				PlayerJoinListener.setAntibotLog(!PlayerJoinListener.isAntibotLog());
				if (PlayerJoinListener.isAntibotLog()) {
					sender.sendMessage("§6Antibot-Logging ist nun §aangeschaltet§6.");
				} else {
					sender.sendMessage("§6Antibot-Logging ist nun §causgeschaltet§6.");
				}
			} else if (args[0].equalsIgnoreCase("reloadfiles")) {
				PlayerJoinListener.getInstance().reloadFiles();
				sender.sendMessage("§cAntibot-Dateien neugeladen");
			} else if (args[0].equalsIgnoreCase("vhostblockage")) {
				PlayerJoinListener.vhostBlockage = !PlayerJoinListener.vhostBlockage;
				if (PlayerJoinListener.vhostBlockage) {
					sender.sendMessage("§6Antibot-vhostBlockage ist nun §aangeschaltet§6.");
				} else {
					sender.sendMessage("§6Antibot-vhostBlockage ist nun §causgeschaltet§6.");
				}
			}
		}
	}
}
