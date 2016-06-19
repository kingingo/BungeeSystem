package dev.wolveringer.bs.commands;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.slotmachine.RoulettGui;
import dev.wolveringer.slotmachine.RoulettHistory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandRoulett extends Command {

	public CommandRoulett() {
		super("roulett");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if (cs instanceof Player) {
			if(args.length == 1 && PermissionManager.getManager().hasPermission(cs, "roulet.toggle") && args[0].equalsIgnoreCase("toggle")){
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
			}
			RoulettGui gui = new RoulettGui();
			gui.setPlayer((Player) cs);
			gui.open();
		}
	}

}
