package dev.wolveringer.bs.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.commands.CommandWhitelist;
import dev.wolveringer.bs.information.InformationManager;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.message.MessageManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.client.PacketHandleErrorException;
import dev.wolveringer.dataserver.ban.BanEntity;
import dev.wolveringer.dataserver.player.LanguageType;
import dev.wolveringer.dataserver.player.Setting;
import dev.wolveringer.dataserver.protocoll.packets.PacketInChangePlayerSettings;
import dev.wolveringer.dataserver.protocoll.packets.PacketVersion;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoinListener implements Listener {
	private CachedArrayList<UUID> connections = new CachedArrayList<>(1, TimeUnit.SECONDS);

	@EventHandler
	public void a(PreLoginEvent e) {
		if(!Main.loaded){
			e.setCancelled(true);
			e.setCancelReason("§cBungeecord isnt fully loaded");
		}
		if (connections.size() >= 5) {
			e.setCancelled(true);
			e.setCancelReason("§cTo many people logging in.");
			return;
		}
		connections.add(UUID.randomUUID());

		long start = System.currentTimeMillis();
		try {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getConnection().getName());

			UUID nameUUID = player.getUUID();
			System.out.println("Connect: Real name: " + e.getConnection().getName() + " Player: " + player.getName() + " UUID: " + player.getUUID());
			System.out.println("Player loaded");
			try {
				if (player.isPremiumSync()) {
					e.getConnection().setUniqueId(player.getUUID());
					e.getConnection().setOnlineMode(true);
					System.out.println("Player premium");
				} else {
					e.getConnection().setUniqueId(player.getUUID());
					e.getConnection().setOnlineMode(false);
					System.out.println("Player cracked");
				}

			} catch (PacketHandleErrorException ex) {
				for (dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus.Error er : ex.getErrors())
					System.out.println(er.getId() + ":" + er.getMessage());
				ex.printStackTrace();
				e.setCancelled(true);
				e.setCancelReason("§cAn error happened while joining.\n§cWe cant check your premium state.\nTry again in 10-30 seconds");
				return;
			}
			BanEntity response = player.getBanStats(e.getConnection().getVirtualHost().getHostString()).getSync();
			if (response != null && response.isActive()) {
				String time;
				if (response.isTempBanned()) {
					time = getDurationBreakdown(response.getEnd() - System.currentTimeMillis());
				} else {
					time = "§cPermanent";
				}
				e.setCancelled(true);
				e.setCancelReason(Main.getTranslationManager().translate("event.join.kickBan", player,new Object[] { time, response.getReson() }));;
			}
			if("true".equalsIgnoreCase(InformationManager.getManager().getInfo("whitelistActive")) && !PermissionManager.getManager().hasPermission(player.getPlayerId(),"epicpvp.whitelist.bypass")){
				String message = InformationManager.getManager().getInfo("whitelistMessage"); //
				if(message == null)
					message = "§cWhitelist is active!";
				e.setCancelled(true);
				e.setCancelReason(message);
				return;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			e.setCancelled(true);
			if(ex.getMessage() != null && ex.getMessage().toLowerCase().contains("timeout")){
				e.setCancelReason("§cCant connect to §eServer-Chef§c. Protocoll version: §a"+PacketVersion.PROTOCOLL_VERSION+"\nPlease try again in 10-30 seconds");
			}
			else
				e.setCancelReason("§cAn error happened while joining.\nTry again in 10-30 seconds");
		}
		long end = System.currentTimeMillis();
		if (end - start > 500)
			System.out.println("LoginEvent for player " + e.getConnection().getName() + " needed more than 500ms (" + (end - start) + ")");
	}

	@EventHandler
	public void a(PostLoginEvent e) {
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName());
		if(!player.getUUID().equals(e.getPlayer().getUniqueId())){
			if(player.getUUID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:"+e.getPlayer().getName()).getBytes()))){
				System.out.println("Switching players uuid (cracked uuid to premium) (But premium is set!)");
				Main.getDatenServer().getClient().writePacket(new PacketInChangePlayerSettings(player.getPlayerId(), Setting.UUID, e.getPlayer().getUniqueId().toString())).getSync();
				Main.getDatenServer().getClient().clearCacheForPlayer(player);
				player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName());
				System.out.println("New uuid: "+player.getUUID());
			}
		}
		LanguageType lang = player.getLanguageSync();
		PermissionManager.getManager().loadPlayer(e.getPlayer().getUniqueId());
		if (e.getPlayer().getPendingConnection().isOnlineMode())
			MessageManager.getmanager(lang).playTitles(e.getPlayer());
	}

	private CachedArrayList<ProxiedPlayer> inQueue = new CachedArrayList<>(5, TimeUnit.SECONDS);
	@EventHandler
	public void a(ServerConnectEvent e) {
		if ((e.getPlayer().getServer() == null && !inQueue.contains(e.getPlayer())) || e.getTarget().getName().equalsIgnoreCase("hub")) {
			Queue<String> joinQueue;
			inQueue.add(e.getPlayer());
			if (e.getPlayer().getPendingConnection().isOnlineMode() || LoginManager.getManager().isLoggedIn(e.getPlayer())) {
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(e.getPlayer().getUniqueId());
				MessageManager.getmanager(player.getLanguageSync());
				if (PermissionManager.getManager().hasPermission(e.getPlayer(), PermissionType.PREMIUM_LOBBY, false))
					joinQueue = ServerManager.getManager().buildPremiumQueue();
				else
					joinQueue = ServerManager.getManager().buildLobbyQueue();
			} else {
				joinQueue =  ServerManager.getManager().buildLoginQueue();
			}
			e.setTarget(BungeeCord.getInstance().getServerInfo(((LinkedList<String>)joinQueue).removeFirst()));
			((UserConnection)e.getPlayer()).setServerJoinQueue(joinQueue);
		}
	}

	public static String getDurationBreakdown(long millis) {
		if (millis < 0) {
			return "millis<0";
		}
		if(millis == 0)
			return "now";
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		StringBuilder sb = new StringBuilder(64);
		if (days > 0) {
			sb.append(days);
			sb.append(" day" + (days == 1 ? "" : "s") + " ");
		}
		if (hours > 0) {
			sb.append(hours);
			sb.append(" hour" + (hours == 1 ? "" : "s") + " ");
		}
		if (minutes > 0) {
			sb.append(minutes);
			sb.append(" minute" + (minutes == 1 ? "" : "s") + " ");
		}
		if (seconds > 0) {
			sb.append(seconds);
			sb.append(" second" + (seconds == 1 ? "" : "s") + "");
		}
		return (sb.toString());
	}
}
//event.join.kickBan - §cYou were banned %s0 §cfrom the Network! \n§3Reason: §c%s1 \n \n§aYou can write an unban-request at §ewww.EpicPvP.org [length,reson]
