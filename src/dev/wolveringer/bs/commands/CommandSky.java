package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Language.Language;
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
	    	p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Language.getText(p, "BG_YOU_ARE_NOW_ON", "SkyBlock"));
	    }
	}

}
