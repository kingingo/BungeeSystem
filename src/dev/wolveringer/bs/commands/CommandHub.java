package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
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
			p.sendMessage(Language.getText(p, "PREFIX") + Language.getText(p, "BG_YOU_ARE_NOW_ON", "Hub"));
		}
	}

}
