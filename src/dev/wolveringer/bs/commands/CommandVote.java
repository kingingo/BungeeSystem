package dev.wolveringer.bs.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandVote extends Command {
	
	public CommandVote(String name) {
		super(name);
	}

	ProxiedPlayer p;
	@Override
	public void execute(CommandSender sender, String[] args) {
		p = (ProxiedPlayer) sender;
		p.sendMessage("§7-----------------------------------------------------");
		p.sendMessage("§cWie votest Du für denn Server? ");
		p.sendMessage(" §b1. §6Klicke auf den Link:§c http://vote.ClashMC.eu");
		p.sendMessage(" §b2. §6Gebe dein Namen und den Code ein. ");
		p.sendMessage(" §b3. §6Klicke auf 'Bewerten' .");
		p.sendMessage(" §b4. §6Warte ca 2-5 Minuten ");
		p.sendMessage(" §b5. §6Bekomme deine Belohnung!");
		p.sendMessage("§7-----------------------------------------------------");
	}

}
