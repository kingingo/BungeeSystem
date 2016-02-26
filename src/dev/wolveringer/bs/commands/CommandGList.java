package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Language.Language;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandGList extends Command {
	public CommandGList(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer){
			ProxiedPlayer p = (ProxiedPlayer) sender;
			p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_GLIST", Main.getDatenServer().getPlayerCount()));
		}else{
			System.out.println("[EpicPvP] Auf diesem Servern sind insgesamt "+BungeeCord.getInstance().getPlayers().size()+" Spieler Online! Auf dem Netzwerk sind es "+Main.getDatenServer().getPlayerCount()+" Spieler");
		}
	}
}
