package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandCreative extends Command {

	public CommandCreative(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		
		if (p.getServer().getInfo() != BungeeCord.getInstance().getServerInfo("creative")) {
	        p.connect(BungeeCord.getInstance().getServerInfo("creative"));  
	    }else{
	    	p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.creative.alredy", args)); //§cYou are allready on the §epvp §cserver!
	    }
	}

}

//command.pvp.alredy - §cYou are allready on the §epvp §cserver!