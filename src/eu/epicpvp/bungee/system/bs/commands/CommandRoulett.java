package eu.epicpvp.bungee.system.bs.commands;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.bungee.system.slotmachine.RoulettGui;
import eu.epicpvp.bungee.system.slotmachine.RoulettHistory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandRoulett extends Command {

	public CommandRoulett() {
		super("roulette");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if (cs instanceof Player) {
			if(args.length == 1 && PermissionManager.getManager().hasPermission(cs, "roulette.toggle") && args[0].equalsIgnoreCase("toggle")){
				if(RoulettHistory.active)
					cs.sendMessage("§cDu hast das roulett ausgestellt.");
				else
					cs.sendMessage("§aDu hast das roulett angestellt.");
				RoulettHistory.active = !RoulettHistory.active;
				Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "toogle_roulett", new DataBuffer().writeBoolean(RoulettHistory.active));
				return;
			}
			if(!RoulettHistory.active){
				cs.sendMessage("§cRoulett is currently disabled!");
				return;
			}
			RoulettGui gui = new RoulettGui();
			gui.setPlayer((Player) cs);
			gui.open();
		}
	}

}
