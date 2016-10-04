package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.servermanager.ServerManager;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
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
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.delserver.help", sender));
				return;
			}

			if (args.length == 1) {
				ServerManager.getManager().delServer(args[0]);
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.delserver.done", sender,args[0]));
			}
		}
	}
}
//command.delserver.help - delserver [Server] §7| §aDevele a server
//command.delserver.done - §aThe Server §e %s0§a was removed! [server]
