package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandSky extends Command {

	public CommandSky(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;

		if (p.getServer().getInfo() != BungeeCord.getInstance().getServerInfo("sky")) {
	        p.connect(BungeeCord.getInstance().getServerInfo("sky"));
	    }else{
	    	p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.sky.alredy", sender));
	    }
	}

}
//command.sky.alredy - §cYou are allready on the §esky §cserver!
