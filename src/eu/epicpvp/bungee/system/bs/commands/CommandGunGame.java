package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandGunGame extends Command {

	public CommandGunGame(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;

		if (p.getServer().getInfo() != BungeeCord.getInstance().getServerInfo("gungame")) {
	        p.connect(BungeeCord.getInstance().getServerInfo("gungame"));
	    }else{
	    	p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.gungame.alredy", args)); //§cYou are allready on the §epvp §cserver!
	    }
	}

}

//command.pvp.alredy - §cYou are allready on the §epvp §cserver!
