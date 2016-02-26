package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.login.LoginManager;
import me.kingingo.kBungeeCord.Language.Language;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandHub extends Command {

	public CommandHub(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		
		if(!LoginManager.getManager().isLoggedIn(p)) return;
		
		if (p.getServer().getInfo() != BungeeCord.getInstance().getServerInfo("hub1")) {
	        p.connect(BungeeCord.getInstance().getServerInfo("hub1"));  
	    }else{
	    	p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_YOU_ARE_NOW_ON","Hub"));
	    }
	}

}
