package eu.epicpvp.bungee.system.booster;

import org.apache.commons.lang3.StringUtils;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.StatsKey;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CMD_BOOSTER extends Command{
	public CMD_BOOSTER() {
		super("booster");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!Main.getDatenServer().isActive()){
			sender.sendMessage("§cDatenserver is offline. Try again in 10 seconds.");
			return;
		}
		if(args.length != 0){
			if(PermissionManager.getManager().hasPermission(sender, "booster.admin")){
				if(args.length > 0)
					if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove")){
						boolean isNumber = args.length >= 2 && StringUtils.isNumeric(args[1]);
						if(!isNumber){
							sender.sendMessage("§c'"+(args.length >= 2 ? args[1] : "null")+"' isnt a number!");
							return;
						}
						LoadedPlayer player = null;
						if(args.length >= 3)
							player = Main.getDatenServer().getClient().getPlayerAndLoad(args[2]);
						else
							player = Main.getDatenServer().getClient().getPlayer(sender.getName());
						Action action = Action.valueOf(args[0].toUpperCase());
						player.setStats(new PacketInStatsEdit.EditStats(GameType.BOOSTER, action, StatsKey.BOOSTER_TIME, Integer.parseInt(args[1])));
						sender.sendMessage("§aDu hast die Boosterzeit von §e"+player.getName()+" §a"+(args[0].equalsIgnoreCase("set")?"auf":"um")+" "+args[1]+" "+(args[0].equalsIgnoreCase("set") ? "gesetzt.":args[0].equalsIgnoreCase("add")?"erhöht.":"reduziert."));
						return;
					}
			}
			sender.sendMessage("§a/booster <add|set|remove> <time (in ms)> [player]");
			return;
		}
		if(sender instanceof Player){
			GuiBoosterMenue m = new GuiBoosterMenue();
			m.setPlayer((Player) sender);
			m.openGui();
		}
	}
}
