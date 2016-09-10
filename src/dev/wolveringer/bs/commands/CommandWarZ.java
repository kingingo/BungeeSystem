package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandWarZ extends Command{

	public CommandWarZ(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] arg1) {
		ProxiedPlayer p = (ProxiedPlayer) sender;

		if (!PermissionManager.getManager().hasPermission(p, "command.server.warz", true))
			return;

		if (p.getServer().getInfo() != BungeeCord.getInstance().getServerInfo("warz")) {
			p.connect(BungeeCord.getInstance().getServerInfo("warz"));
		} else {
			p.sendMessage(Main.getTranslationManager().translate("prefix",sender)+ Main.getTranslationManager().translate("command.server.warz",sender));
		}
	}
	
	
}
