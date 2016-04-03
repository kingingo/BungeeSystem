package dev.wolveringer.bs.listener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.message.MessageManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.client.LanguageType;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.client.PacketHandleErrorException;
import dev.wolveringer.dataserver.ban.BanEntity;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoinListener implements Listener {
	private CachedArrayList<UUID> connections = new CachedArrayList<>(1, TimeUnit.SECONDS);

	@EventHandler
	public void a(PreLoginEvent e) {
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
			try {
				player = Main.getDatenServer().getClient().getPlayerAndLoad(player.getUUID());
				UUID uuidUUID = player.getUUID();
				if (nameUUID != uuidUUID) {
					Main.getDatenServer().getClient().clearCacheForPlayer(player);
					System.out.println("Cleaing up old UUID");
					player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getConnection().getName());
				}
				System.out.println("Connect: Real name: " + e.getConnection().getName() + " Player: " + player.getName() + " UUID: " + player.getUUID());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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
				System.out.println("XXX");
				ex.printStackTrace();
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
				e.setCancelReason(Language.getText(e.getConnection(), "BG_BAN_DISCONNECT", new Object[] { time, response.getReson() }));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			e.setCancelled(true);
			e.setCancelReason("§cAn error happened while joining.\nTry again in 10-30 seconds");
		}
		long end = System.currentTimeMillis();
		if (end - start > 200)
			System.out.println("LoginEvent for player " + e.getConnection().getName() + " needed more than 200ms (" + (end - start) + ")");
	}

	@EventHandler
	public void a(PostLoginEvent e) {
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getUniqueId());
		LanguageType lang = player.getLanguageSync();
		Language.updateLanguage(e.getPlayer(), lang);
		PermissionManager.getManager().loadPlayer(e.getPlayer().getUniqueId());
		if (e.getPlayer().getPendingConnection().isOnlineMode())
			MessageManager.getmanager(lang).playTitles(e.getPlayer());
	}

	@EventHandler
	public void a(ServerConnectEvent e) {
		if (e.getPlayer().getServer() == null || e.getTarget().getName().equalsIgnoreCase("hub")) {
			if (e.getPlayer().getPendingConnection().isOnlineMode() || LoginManager.getManager().isLoggedIn(e.getPlayer())) {
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(e.getPlayer().getUniqueId());
				MessageManager.getmanager(player.getLanguageSync());
				if (PermissionManager.getManager().hasPermission(e.getPlayer(), PermissionType.PREMIUM_LOBBY, false))
					e.setTarget(ServerManager.getManager().nextPremiumLobby());
				else
					e.setTarget(ServerManager.getManager().nextLobby());
			} else {
				e.setTarget(ServerManager.getManager().nextLoginLobby());
			}
		}
	}

	public static String getDurationBreakdown(long millis) {
		if (millis < 0) {
			throw new IllegalArgumentException("Duration must be greater than zero!");
		}

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
