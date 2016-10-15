package eu.epicpvp.bungee.system.bs.commands;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.guild.gui.GuiPlayerGildeOverview;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandGilde extends Command {

	public CommandGilde() {
		super("clan");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cYou are not allowed to take part in the clan system!");
			return;
		}
		Player cs = (Player) sender;
		GuiPlayerGildeOverview gui = new GuiPlayerGildeOverview(cs);
		gui.setPlayer(cs);
		gui.openGui();
	}
}
