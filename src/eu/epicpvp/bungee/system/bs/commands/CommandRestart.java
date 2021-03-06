package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.UtilBungeeCord;
import eu.epicpvp.bungee.system.bs.client.event.ServerMessageEvent;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandRestart extends Command implements Listener {

	private boolean restart = false;

	public CommandRestart(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (sender instanceof ProxiedPlayer) {
				ProxiedPlayer p = (ProxiedPlayer) sender;
				if (PermissionManager.getManager().hasPermission(p, PermissionType.RESTART, true)) {
					restart();
				}
			} else {
				restart();
			}
		} else if(args.length == 1){
			if (args[0].equalsIgnoreCase("all")) {
				if (sender instanceof ProxiedPlayer) {
					ProxiedPlayer p = (ProxiedPlayer) sender;
					if (PermissionManager.getManager().hasPermission(p, PermissionType.RESTART, true)) {
						Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "bcrestart", new DataBuffer());
						restart();
					}
				} else {
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "bcrestart", new DataBuffer());
					restart();
				}
			}
		}
	}

	public void restart() {
		if(restart)
			return;
		restart = true;
		UtilBungeeCord.restart();
	}

	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("bcrestart") && !restart) {
			restart();
		}
	}

}
