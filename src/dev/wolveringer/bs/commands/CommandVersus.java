package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Language.Language;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandVersus extends Command {
	public CommandVersus(String... name) {
		super("versus",null,name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;

		if (p.getServer().getInfo() != BungeeCord.getInstance().getServerInfo("versus")) {
			p.connect(BungeeCord.getInstance().getServerInfo("versus"));
		} else {
			p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.versus.alredy", sender));
		}
	}

}
//command.versus.alredy - §cYou are allready on the §eversus §cserver!