package dev.wolveringer.bs.commands;

import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
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
			p.sendMessage(Language.getText(p, "PREFIX") + Language.getText(p, "BG_YOU_ARE_NOW_ON", "Build"));
		}
	}

}
