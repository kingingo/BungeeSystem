package eu.epicpvp.bungee.system.bs.commands;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandMoney extends Command{ //TODO improve
	public CommandMoney() {
		super("gmoney");
	}

	public void execute(CommandSender cs, String[] args) {
		if(args.length == 0){
			if(cs instanceof Player){
				LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(cs.getName());
				cs.sendMessage("§aCoins: §e"+lplayer.getCoinsSync());
				cs.sendMessage("§aGems: §e"+lplayer.getGemsSync());
			}
		}
	}
}
