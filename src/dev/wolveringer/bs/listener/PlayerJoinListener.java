package dev.wolveringer.bs.listener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataclient.protocoll.packets.PacketInBanStats.BanEntity;
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
		if(connections.size()>=5){
			e.setCancelled(true);
			e.setCancelReason("§cTo many people logging in.");
			return;
		}
		connections.add(UUID.randomUUID());
		
		long start = System.currentTimeMillis();
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(e.getConnection().getName());

		if (player.isPremiumSync()) {
			e.getConnection().setOnlineMode(true);
			e.getConnection().setUniqueId(player.getUUID());
		} else {
			e.getConnection().setOnlineMode(false);
			e.getConnection().setUniqueId(player.getUUID());
		}

		BanEntity response = player.getBanStats(e.getConnection().getVirtualHost().getHostString()).getSync();
		if (response != null && response.isActive()) {
			String time;
			if (response.isTempBanned()) {
				time = getDurationBreakdown(System.currentTimeMillis() - response.getEnd());
			} else {
				time = "§cPermanent";
			}
			e.setCancelled(true);
			e.setCancelReason(Language.getText("BG_BAN_DISCONNECT", new Object[] { time, response.getReson() }));
		}
		long end = System.currentTimeMillis();
		if (end - start > 200)
			System.out.println("LoginEvent for player " + e.getConnection().getName() + " needed more than 200ms (" + (end - start) + ")");
	}
	
	@EventHandler
	public void a(PostLoginEvent e){
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(e.getPlayer().getUniqueId());
		Language.updateLanguage(e.getPlayer(), player.getLanguageSync());
	}
	
	@EventHandler
	public void a(ServerConnectEvent e){
		if(e.getPlayer().getServer() == null){
			if(e.getPlayer().getPendingConnection().isOnlineMode() || LoginManager.getManager().isLoggedIn(e.getPlayer())){
				if(PermissionManager.getManager().hasPermission(e.getPlayer(), PermissionType.PREMIUM_LOBBY, false))
					e.setTarget(ServerManager.getManager().nextPremiumLobby());
				else
					e.setTarget(ServerManager.getManager().nextLobby());
			}
			else {
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
		sb.append(days);
		sb.append(" Days ");
		sb.append(hours);
		sb.append(" Hours ");
		sb.append(minutes);
		sb.append(" Minutes ");
		sb.append(seconds);
		sb.append(" Seconds");

		return (sb.toString());
	}
}
