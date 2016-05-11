package dev.wolveringer.booster;

import dev.wolveringer.BungeeUtil.Player;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CMD_BOOSTER extends Command{
	public CMD_BOOSTER() {
		super("booster");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof Player){
			GuiBoosterMenue m = new GuiBoosterMenue();
			m.setPlayer((Player) sender);
			m.openGui();
		}
	}
}
