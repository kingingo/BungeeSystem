package eu.epicpvp.bungee.system.bs.listener;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.consolencommand.CommandExecutor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.ConsoleCommandSender;

public class ConsoleTeamMessageListener implements CommandExecutor{

	@Override
	public void onCommand(CommandSender sender, String line) {
		if(line.startsWith("~")) {
			if(sender instanceof ConsoleCommandSender){
				String message = "§3[Teamchat] §6"+sender.getName()+": §5"+line.substring(1);
				Main.getDatenServer().teamMessage(message);
			} else {
				((ProxiedPlayer) sender).chat("///iAmAnotherCommandThatDoesNotExistAndShouldNeverExistKappa");
			}
		}
	}

}
