package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandgPing extends Command {

	public CommandgPing() {
		super("gping");
	}

	public String getAvgPing() {
		int ping = 0;
		int count = 0;

		for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
			ping += player.getPing();
			count++;
		}

		if (count == 0) {
			return "0";
		}

		return "" + (ping / count);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;

		if (args.length == 0) {
			p.sendMessage(Language.getText(p, "PREFIX") + "Player-Ping: §e" + p.getPing() + " §7Avg-Ping: §e" + getAvgPing() + "§7 Bungee-TPS: §8undef");
		} else {
			if (PermissionManager.getManager().hasPermission(p, PermissionType.LAG, false)) {
				if (BungeeCord.getInstance().getPlayer(args[0]) != null) {
					p.sendMessage(Language.getText(p, "PREFIX") + "Player-Ping: §e" + BungeeCord.getInstance().getPlayer(args[0]).getPing() + " §7Avg-Ping:§e" + getAvgPing() + "§7 Bungee-TPS: §8undef");
				} else {
					p.sendMessage(Language.getText(p, "PREFIX") + Language.getText(p, "BG_WHEREIS_SEARCH", args[0]));
				}
			}
		}
	}
}
