package dev.wolveringer.bs.commands;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import dev.wolveringer.bs.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;

public class CommandGList extends Command {
	public CommandGList(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ (Main.getTranslationManager().translate("command.glist.info", sender, Main.getDatenServer().getPlayerCount()))); //
		if (sender instanceof ConsoleCommandSender) {
			sender.sendMessage("Online on this bungee: " + ProxyServer.getInstance().getOnlineCount());
			List<String> players = ProxyServer.getInstance().getPlayers().stream()
					.filter(Objects::nonNull)
					.map(ProxiedPlayer::getName)
					.collect(Collectors.toList());
			sender.sendMessage("List: " + players);
		}
	}
}
//command.glist.info - §7On all server there is a total of §e%s0 §7players online! [playerCOunt]
