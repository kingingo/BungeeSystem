package eu.epicpvp.bungee.system.bs.listener;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.ClientVersion;
import dev.wolveringer.BungeeUtil.ClientVersion.BigClientVersion;
import dev.wolveringer.arrays.CachedArrayList;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.information.InformationManager;
import eu.epicpvp.bungee.system.bs.message.MessageManager;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.client.PacketHandleErrorException;
import dev.wolveringer.dataserver.player.LanguageType;
import dev.wolveringer.dataserver.player.Setting;
import dev.wolveringer.dataserver.protocoll.packets.PacketInChangePlayerSettings;
import dev.wolveringer.dataserver.protocoll.packets.PacketVersion;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoinListener implements Listener {
	static {
		if(Main.getTranslationManager() != null)
			Main.getTranslationManager().translate("proxy.join.full", "§cThe server is full!\n§6If you want to join everytime then You can buy a rank in ouer shop.");
	}

	private CachedArrayList<Object> connections = new CachedArrayList<>(1, TimeUnit.SECONDS);

	@EventHandler
	public void a(PreLoginEvent e) {
		if(!Main.getDatenServer().isActive()){
			e.setCancelReason("§cCant connect to §eServer-Chef§c. Protocoll version: §a" + PacketVersion.PROTOCOLL_VERSION + "\nPlease try again in 10-30 seconds");
			e.setCancelled(true);
			return;
		}
		if (!Main.loaded) {
			e.setCancelled(true);
			e.setCancelReason("§cBungeecord isnt fully loaded");
			return;
		}
		if (connections.size() >= 5) {
			e.setCancelled(true);
			e.setCancelReason("§cTo many people logging in.");
			return;
		}

		ClientVersion version = ClientVersion.fromProtocoll(e.getConnection().getVersion());
		String name = e.getConnection().getName();
		if((version.getBigVersion() != BigClientVersion.v1_8 && version.getBigVersion() != BigClientVersion.v1_9 && version != ClientVersion.v1_10_0)
				|| version == ClientVersion.v1_9_1 || version == ClientVersion.v1_9_2 || version == ClientVersion.v1_9_3){
			e.setCancelled(true);
			e.setCancelReason("§cYour minecraft versions issnt supportted. Please use 1.8.X, 1.9.0, 1.9.4 or 1.10");
			System.out.println("Player "+ name +" try to connect with an outdated version ("+e.getConnection().getVersion()+")");
			return;
		}
		if (name.length() < 3 || name.length() > 16) {
			e.setCancelled(true);
			e.setCancelReason("§cInvalid name length!");
			return;
		}
		for (char c : name.toCharArray()) {
			if ((('0' > c) || (c > '9')) && (('a' > c) || (c > 'z')) && (('A' > c) || (c > 'Z')) && (c != '_')){
				e.setCancelled(true);
				e.setCancelReason("§cInvalid characters in name!");
				return;
			}
		}
		connections.add(new Object());

		e.registerIntent(Main.getInstance());
		try {
			ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> {
				long start = System.currentTimeMillis();
				try {
					/*
					 * Clean up old cache
					 */
					if (Main.getDatenServer().getClient().getPlayer(name) != null)
						Main.getDatenServer().getClient().clearCacheForPlayer(Main.getDatenServer().getClient().getPlayer(name));
					if (e.getConnection().getUniqueId() != null)
						if (Main.getDatenServer().getClient().getPlayer(e.getConnection().getUniqueId()) != null)
							Main.getDatenServer().getClient().clearCacheForPlayer(Main.getDatenServer().getClient().getPlayer(e.getConnection().getUniqueId()));

					LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(name);
					System.out.println("Connect: Real name: " + name + " Player: " + player.getName() + " UUID: " + player.getUUID());
					if (Main.getDatenServer().getClient().getPlayer(name) != null)
						Main.getDatenServer().getClient().clearCacheForPlayer(Main.getDatenServer().getClient().getPlayer(name));
					/*//TODO cant get playerId before premium login
					try{
						LoadedPlayer playerUUID = Main.getDatenServer().getClient().getPlayerAndLoad(UUID.fromString(e.getConnection().getUUID()));
						if(playerUUID != null && !playerUUID.getName().equalsIgnoreCase(e.getConnection().getName())){
							if(playerUUID.isOnlineSync()){
								player.setName(player.getName()+"_old_player_overwridden_by_"+e.getConnection().getName());
								playerUUID.setName(e.getConnection().getName());
								player = playerUUID;
							}
						}
					}catch(Exception ex){
						ex.printStackTrace();
					}
					*/

					if(Main.getDatenServer().getPlayerCount() > Integer.parseInt(InformationManager.getManager().getInfo("maxPlayers"))){
						if(!PermissionManager.getManager().hasPermission(player.getPlayerId(), "proxy.join.full")){
							e.getConnection().disconnect(Main.getTranslationManager().translate("proxy.join.full",player));
							return;
						}
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
						ex.printStackTrace();
						e.setCancelled(true);
						e.setCancelReason("§cAn error happened while joining.\n§cWe cant check your premium state.\nTry again in 10-30 seconds");
						return;
					}

					/*
					List<BanEntity> entries = player.getBanStats(e.getConnection().getAddress().getHostString(),1).getSync();
					if (entries.size() > 0 && entries.get(0).isActive()) {
						BanEntity response = entries.get(0);
						String time;
						if (response.isTempBanned()) {
							time = getDurationBreakdown(response.getEnd() - System.currentTimeMillis());
						} else {
							time = "§cPermanent";
						}
						e.setCancelled(true);
						e.setCancelReason(Main.getTranslationManager().translate("event.join.kickBan", player, new Object[] { time, response.getReson(),response.getLevel() }));
						return;
					}
					*/
					if ("true".equalsIgnoreCase(InformationManager.getManager().getInfo("whitelistActive")) && !PermissionManager.getManager().hasPermission(player.getPlayerId(), "epicpvp.whitelist.bypass")) {
						String message = InformationManager.getManager().getInfo("whitelistMessage"); //
						if (message == null)
							message = "§cWhitelist is active!";
						e.setCancelled(true);
						e.setCancelReason(message);
						return;
					}
					player.setIp(e.getConnection().getAddress().getAddress().getHostAddress());
				} catch (Throwable t) {
					t.printStackTrace();
					e.setCancelled(true);
					if (t.getMessage() != null && t.getMessage().toLowerCase().contains("timeout")) {
						e.setCancelReason("§cCant connect to §eServer-Chef§c. Protocoll version: §a" + PacketVersion.PROTOCOLL_VERSION + "\nPlease try again in 10-30 seconds");
					} else
						e.setCancelReason("§cAn error happened while joining.\nTry again in 10-30 seconds");
				} finally {
					e.completeIntent(Main.getInstance());
				}
				long end = System.currentTimeMillis();
				if (end - start > 500)
					System.out.println("LoginEvent for player " + name + " needed more than 500ms (" + (end - start) + ")");
			});
		} catch (Throwable t) {
			e.completeIntent(Main.getInstance());
			throw t;
		}
	}

	@EventHandler
	public void a(PostLoginEvent e) {
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName());
		if(Main.getDatenServer().getPlayers() != null)
			Main.getDatenServer().getPlayers().add(e.getPlayer().getName());
		if (!player.getUUID().equals(e.getPlayer().getUniqueId())) {
			if (player.getUUID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + e.getPlayer().getName()).getBytes()))) {
				System.out.println("Switching players uuid (cracked uuid to premium) (But premium is set!)");
				Main.getDatenServer().getClient().writePacket(new PacketInChangePlayerSettings(player.getPlayerId(), Setting.UUID, e.getPlayer().getUniqueId().toString())).getSync();
				Main.getDatenServer().getClient().clearCacheForPlayer(player);
				player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName());
				System.out.println("New uuid: " + player.getUUID());
			}
		}
		LanguageType lang = player.getLanguageSync();
		PermissionManager.getManager().loadPlayer(e.getPlayer().getUniqueId());
		if (e.getPlayer().getPendingConnection().isOnlineMode())
			MessageManager.getmanager(lang).playTitles(e.getPlayer());
	}

	public static String getDurationBreakdown(long millis) {
		return getDurationBreakdown(millis, "now");
	}
	public static String getDurationBreakdown(long millis,String no) {
		return getDurationBreakdown(millis, no, new HashMap<>());
	}
	public static String getDurationBreakdown(long millis,String no,HashMap<TimeUnit, String> mapping) {
		return getDurationBreakdown(millis, no, new HashMap<>(), mapping);
	}
	public static String getDurationBreakdown(long millis,String no,HashMap<TimeUnit, String> plural,HashMap<TimeUnit, String> mapping) {
		if (millis < 0) {
			return "millis<0";
		}
		if (millis == 0)
			return no;
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
			sb.append(" "+mapping.getOrDefault(TimeUnit.DAYS, "day")+"" + (days == 1 ? "" : plural.getOrDefault(TimeUnit.DAYS, "s")) + " ");
		}
		if (hours > 0) {
			sb.append(hours);
			sb.append(" "+mapping.getOrDefault(TimeUnit.HOURS, "hour")+"" + (hours == 1 ? "" : plural.getOrDefault(TimeUnit.HOURS, "s")) + " ");
		}
		if (minutes > 0) {
			sb.append(minutes);
			sb.append(" "+mapping.getOrDefault(TimeUnit.MINUTES, "minute")+"" + (minutes == 1 ? "" : plural.getOrDefault(TimeUnit.MINUTES, "s")) + " ");
		}
		if (seconds > 0) {
			sb.append(seconds);
			sb.append(" "+mapping.getOrDefault(TimeUnit.SECONDS, "second")+"" + (seconds == 1 ? "" : plural.getOrDefault(TimeUnit.SECONDS, "s")) + "");
		}
		return (sb.toString());
	}
}
//event.join.kickBan - §cYou were banned %s0 §cfrom the Network! \n§3Reason: §c%s1 \n \n§aYou can write an unban-request at §ewww.EpicPvP.org [length,reson]
