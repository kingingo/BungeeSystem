package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandKicken extends Command implements Listener {

	public CommandKicken(String name) {
		super("bkick", null, name, "kick");
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if (!PermissionManager.getManager().hasPermission(player, PermissionType.KICKEN, true)) return;

			if (args.length < 1) {
				player.sendMessages(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.kick.help", sender));
			} else if (args.length == 1) {
				player.sendMessages(Main.getTranslationManager().translate("prefix", sender) + "§cDu musst einen Grund angeben.");
			} else {
				String targetPlayer = args[0];

				StringBuilder sb = new StringBuilder();
				for (int i = 1; i < args.length; i++) {
					sb.append(args[i]);
					sb.append(" ");
				}
				sb.setLength(sb.length() - 1);
				String grund = ChatColor.translateAlternateColorCodes('&', sb.toString());
				try {
					Main.getDatenServer().getClient().kickPlayer(Main.getDatenServer().getClient().getPlayerAndLoad(targetPlayer).getPlayerId(), "§cKick reason:§b " + grund);
				} catch (Exception e) {
					sender.sendMessage("Exception: " + e.getMessage());
				}
				Main.getDatenServer().teamMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.kick.teammessage", sender, targetPlayer, player.getName(), grund));
			}
		}
	}
}
//command.kick.help - /kick [Player] [Grund] §7| §aKick a player
//command.kick.teammessage - §e%s0§c was kicked by §e%s1§c. Reason: §e%s2
