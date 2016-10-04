package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandBuild extends Command {

	public CommandBuild(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;

		if (!PermissionManager.getManager().hasPermission(p, PermissionType.BUILD_SERVER, true))
			return;

		if (p.getServer().getInfo() != BungeeCord.getInstance().getServerInfo("build")) {
			p.connect(BungeeCord.getInstance().getServerInfo("build"));
		} else {
			p.sendMessage(Main.getTranslationManager().translate("prefix",sender)+ Main.getTranslationManager().translate("command.build.alredythere",sender));
		}
	}

}
//command.build.alredythere - §cYou are allready on the §e%build §cserver!
