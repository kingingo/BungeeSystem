package eu.epicpvp.bungee.system.bs.consolencommand;

import net.md_5.bungee.api.CommandSender;

public interface CommandExecutor {
	public void onCommand(CommandSender sender,String line);
}
