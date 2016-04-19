package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandGList extends Command {
	public CommandGList(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ (Main.getTranslationManager().translate("command.glist.info", sender, Main.getDatenServer().getPlayerCount()))); //
	}
}
//command.glist.info - §7On all server there is a total of §e%s0 §7players online! [playerCOunt]