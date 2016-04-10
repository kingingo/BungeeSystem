package dev.wolveringer.bs.commands;

import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.player.LanguageType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
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
					//"§7BG: §b{INPUT0}§7 Online:§b{INPUT2} §7Player-Online:§b{INPUT3}§7 Avg-Ping:§b{INPUT4} §7TPS:§b{INPUT5} §7Ram:§b{INPUT6} MB"
					p.sendMessage(Language.getText(p, "PREFIX") + Language.getText(p, "BG_PERFORMANCE", new String[] { Main.getInstance().getServerId(),"§cundef", "-1", String.valueOf(BungeeCord.getInstance().getOnlineCount()), getAvgPing(), "undef", String.valueOf(run.maxMemory() / 1048576L) }));
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "performance", new DataBuffer().writeUUID(p.getUniqueId()));
				}
			}
		}
	}

	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("performance")) {
			UUID sender = e.getBuffer().readUUID();
			Runtime run = Runtime.getRuntime();
			Main.getDatenServer().getClient().sendMessage(sender, Language.getText(LanguageType.ENGLISH,"PREFIX") + Language.getText(LanguageType.ENGLISH,"BG_PERFORMANCE", new String[] { Main.getInstance().getServerId(),"§cundef", "-1", String.valueOf(BungeeCord.getInstance().getOnlineCount()), getAvgPing(), "undef", String.valueOf(run.maxMemory() / 1048576L) }));
		}
	}
}
