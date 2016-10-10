package eu.epicpvp.bungee.system.bs.listener;

import eu.epicpvp.bungee.system.bs.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;

public class CommandConsoleTeamMessage extends Command {
	
	public CommandConsoleTeamMessage() {
		super("~");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			String message = "§cTeamchat §8| §a~" + sender.getName() + " §8» §f" + String.join(" ", (CharSequence[]) args).substring(1);
			Main.getDatenServer().teamMessage(message);
		} else {
			((ProxiedPlayer) sender).chat("///iAmAnotherCommandThatDoesNotExistAndShouldNeverExistKappa");
		}
	}
}
