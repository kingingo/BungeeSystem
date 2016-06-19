package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandPerformance extends Command implements Listener {

	public CommandPerformance(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	public String getAvgPing() {
		int ping = 0;
		int count = 0;

		for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
			ping += player.getPing();
			count++;
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
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.performance.information", sender,Main.getInstance().getServerId(),BungeeCord.getInstance().getOnlineCount(),getAvgPing(),((run.totalMemory()-run.freeMemory()) / 1048576L),run.maxMemory()));
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "performance", new DataBuffer().writeInt(Main.getDatenServer().getClient().getPlayerAndLoad(p.getName()).getPlayerId()));
				}
			}
		}
	}

	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("performance")) {
			int sender = e.getBuffer().readInt();
			Runtime run = Runtime.getRuntime();
			Main.getDatenServer().getClient().sendMessage(sender, Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.performance.information", sender,Main.getInstance().getServerId(),BungeeCord.getInstance().getOnlineCount(),getAvgPing(),((run.totalMemory()-run.freeMemory()) / 1048576L),run.maxMemory()));
		}
	}
}
//command.performance.information - §7Bungeecord: §b%s0§7 Player Online: %s1§7 Avg-Ping:§b%s2 §7Ram:§b%s3§7/§c%s4 [Bungeecord,Playersonline,Avg ping,Ram,Max ram]