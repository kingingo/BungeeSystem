package dev.wolveringer.report.commands;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.report.gui.GuiPlayerMenue;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CMD_Report extends Command{
	public CMD_Report() {
		super("report");
	}
	
	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!(cs instanceof Player)){
			cs.sendMessage("§cYou arent an instance of a Player!");
			return;
		}
		Player p = (Player) cs;
		//new PlayerSarchMenue(p).open();
		GuiPlayerMenue menue = new GuiPlayerMenue();
		menue.setPlayer(p);
		menue.openGui();
	}
}
