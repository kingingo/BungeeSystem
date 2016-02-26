package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.servermanager.ServerManager;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommanddelServer extends Command {

	public CommanddelServer(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if (PermissionManager.getManager().hasPermission(p, PermissionType.DEL_SERVER,true)) {
			if (args.length == 0) {
				p.sendMessage(Language.getText(p, "PREFIX")+"/delserver [Server]");
				return;
			}

			if (args.length == 1) {
				String name = args[0];
				ServerManager.getManager().delServer(args[0]);
				p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_DEL_SERVER", name));
			}
		}
	}
}
