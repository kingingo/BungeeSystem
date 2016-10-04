package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandTBuild extends Command {

	public CommandTBuild(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		
		if(!PermissionManager.getManager().hasPermission(p, PermissionType.TBUILD_SERVER,true))return;
		
		if (p.getServer().getInfo() != BungeeCord.getInstance().getServerInfo("tbuild")) {
	        p.connect(BungeeCord.getInstance().getServerInfo("tbuild"));  
	    }else{
	    	p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.tbuild.alredy", sender));
	    }
	}

}
//command.tbuild.alredy - §cYou are allready on the §etest-build §cserver!