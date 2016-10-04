package eu.epicpvp.bungee.system.bs.listener;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.consolencommand.CommandExecutor;
import net.md_5.bungee.api.CommandSender;

public class ConsoleTeamMessageListener implements CommandExecutor{

	@Override
	public void onCommand(CommandSender sender, String line) {
		if(line.startsWith("~")){
			String message = "§3[Teamchat] §6"+sender.getName()+": §5"+line.substring(1);
			Main.getDatenServer().teamMessage(message);
		}
	}

}
