package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.login.LoginManager;
import eu.epicpvp.bungee.system.bs.servermanager.ServerManager;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandHub extends Command {

	public CommandHub(String... name) {
		super("hub", null, name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)){
			sender.sendMessage("§cYou are not a player!");
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) sender;

		if (!LoginManager.getManager().isLoggedIn(p))
			return;

		if (!p.getServer().getInfo().getName().startsWith("hub") && !p.getServer().getInfo().getName().startsWith("premium") && !p.getServer().getInfo().getName().startsWith("login")) {
			if (PermissionManager.getManager().hasPermission(p, PermissionType.PREMIUM_LOBBY, false))
				p.connect(ServerManager.getManager().nextPremiumLobby());
			else
				p.connect(ServerManager.getManager().nextLobby());
		} else {
			p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.hub.alredy", sender) );
		}
	}

}
//command.hub.alredy - §cYou are allready on a hub server!
