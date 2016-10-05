package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.servermanager.ServerManager;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandaddServer extends Command implements Listener {

	public CommandaddServer(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (PermissionManager.getManager().hasPermission(sender, PermissionType.ADD_SERVER,true)) {
			String name;
			String adress;
			int port;

			if (args.length <= 2) {
				sender.sendMessage(Main.getTranslationManager().translate("prefix",sender)+Main.getTranslationManager().translate("command.addserver.help",sender));
				return;
			}

			if (args.length == 3) {
				name = args[0];
				adress = args[1];
				port = Integer.valueOf(args[2]);
				ServerManager.getManager().addServer(name, adress, port);
				sender.sendMessage(Main.getTranslationManager().translate("prefix",sender)+Main.getTranslationManager().translate("command.addserver.success",sender,name));
			}
		}
	}
}
/*

command.addserver.help - /addserver [Server] [IP-Adresse] [Port] - []
command.addserver.success - §aYou have added the server %s0 [Servername]


*/
