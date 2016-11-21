package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.client.event.ServerMessageEvent;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandPerformance extends Command implements Listener {

	static {
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.performance.info", "§7BungeeCord: §b%s0 §7Players: §b%s1 §7AVG-Ping: §b%s2 §7Memory: §b%s3mb§7/§c%s4mb");
	}

	public CommandPerformance(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	public String getAvgPing() {
		int ping = 0;
		int count = 0;

		for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
			int pp = player.getPing();
			if (pp >= 0 && pp < 30_000) {
				ping += pp;
				count++;
			}
		}

		if (count == 0) {
			return "0";
		}

		return "" + (ping / count);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if (PermissionManager.getManager().hasPermission(p, PermissionType.LAG, true)) {
				if (args.length == 0) {
					Runtime run = Runtime.getRuntime();
					//"§7Bungeecord: §b%s0§7 Player Online: %s1§7 Avg-Ping:§b%s2 §7Ram:§b%bs3§7/§c%s4" [Bungeecord,Playersonline,Avg ping,Ram,Max ram]
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.performance.info", sender,Main.getInstance().getServerId(),BungeeCord.getInstance().getOnlineCount(),getAvgPing(),(run.totalMemory()-run.freeMemory()) / (1024*1024),run.maxMemory()/(1024*1024)));
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "performance", new DataBuffer().writeInt(Main.getDatenServer().getClient().getPlayerAndLoad(p.getName()).getPlayerId()));
				}
			}
		}
	}

	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("performance")) {
			int sender = e.getBuffer().readInt();
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(sender);
			Runtime run = Runtime.getRuntime();
			Main.getDatenServer().getClient().sendMessage(sender, Main.getTranslationManager().translate("prefix", player)+Main.getTranslationManager().translate("command.performance.info", player,Main.getInstance().getServerId(),BungeeCord.getInstance().getOnlineCount(),getAvgPing(),(run.totalMemory()-run.freeMemory()) / (1024*1024),run.maxMemory()/(1024*1024)));
		}
	}
}
//command.performance.information - §7Bungeecord: §b%s0§7 Player Online: %s1§7 Avg-Ping:§b%s2 §7Ram:§b%s3§7/§c%s4 [Bungeecord,Playersonline,Avg ping,Ram,Max ram]
