package eu.epicpvp.bungee.system.bs.commands;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.teamspeak.GuiTeamspeak;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandTeamspeak extends Command{
	public CommandTeamspeak() {
		super("teamspeak",null,"ts3","ts");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof Player){
			new GuiTeamspeak().setPlayer((Player) sender).openGui();
		}
	}
}
