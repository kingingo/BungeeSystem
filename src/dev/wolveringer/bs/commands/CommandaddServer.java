package dev.wolveringer.bs.commands;


import dev.wolveringer.bs.servermanager.ServerManager;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandaddServer extends Command implements Listener {

	public CommandaddServer(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if (PermissionManager.getManager().hasPermission(p, PermissionType.ADD_SERVER,true)) {
			String name;
			String adress;
			int port;

			if (args.length <= 2) {
				p.sendMessage(Language.getText(p, "PREFIX")+"/addserver [Server] [IP-Adresse] [Port]");
				return;
			}

			if (args.length == 3) {
				name = args[0];
				adress = args[1];
				port = Integer.valueOf(args[2]);
				ServerManager.getManager().addServer(name, adress, port);
				p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_ADD_SERVER",name));
			}
		}
	}
}
