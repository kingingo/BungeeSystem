package dev.wolveringer.bs.commands;

import java.util.Arrays;

import dev.wolveringer.bs.information.InformationManager;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandMOTD extends Command {

	public CommandMOTD(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if (!(cs instanceof ProxiedPlayer))
			return;
		ProxiedPlayer player = (ProxiedPlayer) cs;
		if (PermissionManager.getManager().hasPermission(player, PermissionType.MOTD, true)) {
			if (args.length < 1) {
				cs.sendMessage("§cWrong Syntax:");
				cs.sendMessage("§a/motd set <line> <message>");
				return;
			}
			if (args[0].equalsIgnoreCase("set")) {
				Integer line = -1;
				line = Integer.parseInt(args[1]);
				if (line < 1 || line > 2) {
					cs.sendMessage("§cLine out of bounds!");
				}
				
				String message = "";
				for(String s :  Arrays.copyOfRange(args, 2, args.length))
					message+=s+" ";
				
				InformationManager.getManager().setInfo("motd" + line,message);
				cs.sendMessage("§cDu hast den MOTD erfolgrich gesetzt.");
			} else if (args[0].equalsIgnoreCase("get")) {
				cs.sendMessage("§aMotd:");
				cs.sendMessage(InformationManager.getManager().getInfo("motd1"));
				cs.sendMessage(InformationManager.getManager().getInfo("motd2"));
			}
		}
	}

}