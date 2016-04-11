package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
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
		p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ Main.getTranslationManager().translate("command.gping.info", sender,p.getPing(),getAvgPing()));
	}
}
//command.gping.info - Player-Ping: §e%s0 §7Avg-Ping: §e%s1  [playerPing,AvgPing]