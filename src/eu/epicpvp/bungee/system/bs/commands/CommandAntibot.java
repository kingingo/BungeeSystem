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
			sender.sendMessage("§c/antibots logincd [Sekunden]§7- §6Setzt den LoginCooldown.");
			sender.sendMessage("§c/antibots reg §7- §6Zeigt das unregistriertenlimit.");
			sender.sendMessage("§c/antibots regmax <Anzahl>§7- §6Setzt max Anzahl an unregistrierten Verbindungen in regtime.");
			sender.sendMessage("§c/antibots regtime <Sekunden>§7- §6Setzt die Zeit in der regmax unregistrierte Spieler joinen können.");
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
			} else if (args[0].equalsIgnoreCase("logincd")) {
				sender.sendMessage("§6Antibot - login cooldown is at §e" + PlayerJoinListener.getInstance().getLoginHubLeavePenaltySeconds() + "§6s");
			} else if (args[0].equalsIgnoreCase("reg")) {
				sender.sendMessage("§6Antibot - register limit is at §e" + PlayerJoinListener.getInstance().getRegisterMax() + "§6 / §e " + PlayerJoinListener.getInstance().getRegisterTimeSeconds() + "§6s");
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("logincd")) {
				try {
					int i = Integer.parseInt(args[1]);
					PlayerJoinListener.getInstance().setLoginHubLeavePenaltySeconds(i);
					sender.sendMessage("§6Antibot - login cooldown is now at §e" + PlayerJoinListener.getInstance().getLoginHubLeavePenaltySeconds() + "§6s");
				} catch (NumberFormatException e) {
					e.printStackTrace();
					sender.sendMessage("§6Das ist keine gültige Zahl.");
				}
			} else if (args[0].equalsIgnoreCase("regmax")) {
				try {
					int i = Integer.parseInt(args[1]);
					PlayerJoinListener.getInstance().setRegisterMax(i);
					sender.sendMessage("§6Antibot - register limit is now at §e" + PlayerJoinListener.getInstance().getRegisterMax() + "§6 / §e " + PlayerJoinListener.getInstance().getRegisterTimeSeconds() + "§6s");
				} catch (NumberFormatException e) {
					e.printStackTrace();
					sender.sendMessage("§6Das ist keine gültige Zahl.");
				}
			} else if (args[0].equalsIgnoreCase("regtime")) {
				try {
					int i = Integer.parseInt(args[1]);
					PlayerJoinListener.getInstance().setRegisterTimeSeconds(i);
					sender.sendMessage("§6Antibot - register limit is now at §e" + PlayerJoinListener.getInstance().getRegisterMax() + "§6 / §e " + PlayerJoinListener.getInstance().getRegisterTimeSeconds() + "§6s");
				} catch (NumberFormatException e) {
					e.printStackTrace();
					sender.sendMessage("§6Das ist keine gültige Zahl.");
				}
			}
		}
	}
}
