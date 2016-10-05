package eu.epicpvp.bungee.system.bs.commands;

import java.util.HashSet;
import java.util.Set;

import eu.epicpvp.bungee.system.bs.Bootstrap;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenclient.client.debug.Debugger;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.command.ConsoleCommandSender;

public class CommandBDebug extends Command implements TabExecutor {

	public CommandBDebug(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ConsoleCommandSender || (sender instanceof ProxiedPlayer && PermissionManager.getManager().hasPermission(((ProxiedPlayer) sender), "epicpvp.bungee.debug", false))) {
			if (args.length == 0) {
				sender.sendMessage("§6Sets debug logging state of the bungee you are on");
				if (Debugger.isEnabled() && Debugger.getFilter() == null)
					sender.sendMessage("§4/" + getName() + " on");
				else
					sender.sendMessage("§c/" + getName() + " on");
				if (Debugger.isEnabled() && Debugger.getFilter() != null)
					sender.sendMessage("§4/" + getName() + " filtered §7(Filters spammy messages)");
				else
					sender.sendMessage("§c/" + getName() + " filtered §7(Filters spammy messages)");
				if (!Debugger.isEnabled())
					sender.sendMessage("§4/" + getName() + " off");
				else
					sender.sendMessage("§c/" + getName() + " off");
			} else {
				switch (args[0].toLowerCase()) {
					case "on":
						Debugger.setFilter(null);
						Debugger.setEnabled(true);
						sender.sendMessage("§6Debugger is now §aon§6.");
						break;
					case "filtered":
						Debugger.setFilter(Bootstrap.DEBUGGER_FILTER);
						Debugger.setEnabled(true);
						sender.sendMessage("§6Debugger is now §efiltered§6. §7(Filters spammy messages)");
						break;
					case "off":
						Debugger.setFilter(null);
						Debugger.setEnabled(false);
						sender.sendMessage("§6Debugger is now §coff§6.");
						break;
					default:
						sender.sendMessage("§4Unknown state " + args[0]);
						break;
				}
			}
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		Set<String> result = new HashSet<>();
		if (args.length == 1 && (sender instanceof ConsoleCommandSender || (sender instanceof ProxiedPlayer && PermissionManager.getManager().hasPermission(((ProxiedPlayer) sender), "epicpvp.bungee.debug", false)))) {
			String arg = args[0].toLowerCase();
			if ("on".startsWith(arg)) {
				result.add("on");
			}
			if ("off".startsWith(arg)) {
				result.add("off");
			}
			if ("filtered".startsWith(arg)) {
				result.add("filtered");
			}
		}
		return result;
	}
}
