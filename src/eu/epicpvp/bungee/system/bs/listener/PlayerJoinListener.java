package eu.epicpvp.bungee.system.bs.listener;

import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.wolveringer.BungeeUtil.ClientVersion;
import dev.wolveringer.BungeeUtil.ClientVersion.BigClientVersion;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.information.InformationManager;
import eu.epicpvp.bungee.system.bs.listener.util.IpData;
import eu.epicpvp.bungee.system.bs.listener.util.Rate;
import eu.epicpvp.bungee.system.bs.message.MessageManager;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.dataserver.protocoll.packets.PacketInChangePlayerSettings;
import eu.epicpvp.dataserver.protocoll.packets.PacketOutPacketStatus;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.client.PacketHandleErrorException;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.datenserver.definitions.dataserver.player.Setting;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoinListener implements Listener {

	static {
		if (Main.getTranslationManager() != null) {
			Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "proxy.join.full", "§cDer Server ist voll!\n§aWen du joinen möchtest, wenn der Server voll ist, kauf einen Rang in unserem Shop.");
			Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "proxy.join.full", "§cThe server is full!\n§aIf you want to join if the server is full, buy a rank in our shop.");
		}
	}

	@Getter
	private static boolean attackMode = false;
	private Rate attackDetectionRate = new Rate(30, TimeUnit.SECONDS);
	private Rate filteredRate = new Rate(10, TimeUnit.SECONDS);
	private Rate allowRate = new Rate(1, TimeUnit.SECONDS);
	private Rate overallConnectionRate = new Rate(2, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	private Cache<String, IpData> ipDatas =
			CacheBuilder.newBuilder()
					.expireAfterWrite(2, TimeUnit.HOURS)
					.build();

	@EventHandler
	@SneakyThrows(ExecutionException.class)
	public void onPreLogin(PreLoginEvent e) {
		if (!Main.getDatenServer().isActive()) {
			e.setCancelReason("§cCan't connect to §eserver-chef§c.\n§aPlease try again in 10-30 seconds.");
			e.setCancelled(true);
			return;
		}
		if (!Main.loaded) {
			e.setCancelled(true);
			e.setCancelReason("§cStill setting up bungeecord.\n§aPlease try again in 10-20 seconds.");
			return;
		}

		int versionNumber = e.getConnection().getVersion();
		ClientVersion version = ClientVersion.fromProtocoll(versionNumber);
		String name = e.getConnection().getName();
		String ip = e.getConnection().getAddress().getAddress().getHostAddress();
		if (version == null || version == ClientVersion.v1_9_1 || version == ClientVersion.v1_9_2 || version == ClientVersion.v1_9_3
				|| (version.getBigVersion() != BigClientVersion.v1_8 && version.getBigVersion() != BigClientVersion.v1_9 && version != ClientVersion.v1_10_0)) {
			e.setCancelled(true);
			e.setCancelReason("§cYour minecraft version is not supported. Please use 1.8.X, 1.9.0, 1.9.4 or 1.10.X");
			System.out.println("Player " + name + " tried to connect with an unsupported version (" + versionNumber + ") with ip " + ip);
			return;
		}
		if (name.length() < 3 || name.length() > 16) {
			e.setCancelled(true);
			e.setCancelReason("§cInvalid name length!");
			return;
		}
		for (char c : name.toCharArray()) {
			if ((('0' > c) || (c > '9')) && (('a' > c) || (c > 'z')) && (('A' > c) || (c > 'Z')) && (c != '_')) {
				e.setCancelled(true);
				e.setCancelReason("§cInvalid characters in name!");
				return;
			}
		}

		//Antibots ip-based rate-Limit
		IpData data = ipDatas.get(ip, () -> new IpData(ip));
		Cache<String, Integer> nameJoinCounter = data.getNameJoinCounter();
		String lowerCaseName = name.toLowerCase();
		Integer joins = nameJoinCounter.getIfPresent(lowerCaseName);
		Rate differentNameJoinRate = data.getDifferentNameJoinRate();
		if (joins == null) {
			nameJoinCounter.put(lowerCaseName, 1);
			differentNameJoinRate.eventTriggered();
		} else {
			nameJoinCounter.put(lowerCaseName, joins + 1);
		}
		int differentNameJoins2H = differentNameJoinRate.getOccurredEventsInMaxTime();
		int differentNameJoins1M = differentNameJoinRate.getOccurredEventsInTime(1, TimeUnit.MINUTES);
		if (differentNameJoins2H >= 4) {
			e.setCancelled(true);
			filteredRate.eventTriggered();
			e.setCancelReason("§cSuspicious ip.\n§aBitte melde dich auf unserem Teamspeak, falls dies ein Fehler sein sollte.");
			System.out.println("§4§l[AntiBot] " + (attackMode ? "§6[AttackMode] " : "") + "§7" + ip + " tried to join with " + differentNameJoins2H + " different names in 2h, latest name: " + name);
			return;
		} else if (attackDetectionRate.getOccurredEventsInMaxTime() != 0 && differentNameJoins1M >= 2) {
			e.setCancelled(true);
			filteredRate.eventTriggered();
			e.setCancelReason("§cSuspicious ip.\n§c§lWichtig:\n§aBitte versuche dich über deine IP nur mit einem Namen innerhalb von 5 Minuten zu verbinden.\n§aBitte melde dich auf unserem Teamspeak, falls dies ein Fehler sein sollte.\n§8Diese Restriktion ist nur während einer Joinbot-Attacke aktiv.");
			System.out.println("§4§l[AntiBot] §6[AttackMode] §7" + ip + " tried to join with " + differentNameJoins1M + " different names in 1M, latest name: " + name);
			return;
		}
		if (filteredRate.getOccurredEventsInMaxTime() > 5) {
			enableAttackMode();
		}

//		Rate loginHubLeaveRate = data.getLoginHubLeaveRate();
//		int occurredEventsInTime = loginHubLeaveRate.getOccurredEventsInTime(20, TimeUnit.SECONDS);
//		if (occurredEventsInTime >= 4) {
//			e.setCancelled(true);
//			e.setCancelReason("§cSuspicious behaviour.\n§aBitte melde dich auf unserem Teamspeak, falls dies ein Fehler sein sollte.");
//			System.out.println("[AntiBot] " + ip + " tried to rejoin after leaving loginhub " + occurredEventsInTime + " times in 20sec (kicks count twice), latest name: " + name);
//		}
//		loginHubLeaveRate.eventTriggered();

		//Antibots overall rate-Limit
		double averagePerSecond = overallConnectionRate.getAveragePerSecondOnMaxTime();
		overallConnectionRate.eventTriggered();
		if (allowRate.getOccurredEventsInMaxTime() > 2 && averagePerSecond >= 5) {
			enableAttackMode();
			e.setCancelled(true);
			e.setCancelReason("§cAktuell versuchen zu viele Spieler sich gleichzeitig anzumelden.\n§aVersuche es bitte in ein paar Sekunden erneut.");
			System.out.println("§4§l[AntiBot] " + (attackMode ? "§6[AttackMode] " : "") + "§7Cancelled login of " + ip + " / " + name + " because global rate limit being at " + averagePerSecond + " joins/sec in average over the last 2 sec");
			return;
		}
		if (attackMode && attackDetectionRate.getOccurredEventsInMaxTime() == 0) {
			attackMode = false;
			System.out.println("------------------------------");
			System.out.println("§4§l[AntiBot] §fAttack mode disabled");
			System.out.println("------------------------------");
		}
		allowRate.eventTriggered();

		//lets do stuff async
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
					System.out.println("Connect: Name: " + name + " Player: " + player.getName() + " PlayerID: " + player.getPlayerId());
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

					if (Main.getDatenServer().getPlayerCount() > Integer.parseInt(InformationManager.getManager().getInfo("maxPlayers"))) {
						if (!PermissionManager.getManager().hasPermission(player.getPlayerId(), "proxy.join.full")) {
							e.getConnection().disconnect(Main.getTranslationManager().translate("proxy.join.full", player));
							return;
						}
					}

					try {
						if (player.isPremiumSync()) {
							e.getConnection().setUniqueId(player.getUUID());
							e.getConnection().setOnlineMode(true);
//							System.out.println("Player premium");
						} else {
							e.getConnection().setUniqueId(player.getUUID());
							e.getConnection().setOnlineMode(false);
//							System.out.println("Player cracked");
						}
					} catch (PacketHandleErrorException ex) {
						for (PacketOutPacketStatus.Error er : ex.getErrors())
							System.out.println(er.getId() + ":" + er.getMessage());
						ex.printStackTrace();
						e.setCancelled(true);
						e.setCancelReason("§cAn error happened while joining.\n§cWe couldn't check your premium state.\nTry again in 10-30 seconds");
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
						String message = InformationManager.getManager().getInfo("whitelistMessage");
						if (message == null)
							message = "§cWhitelist is active!";
						e.setCancelled(true);
						e.setCancelReason(message);
						return;
					}
					player.setIp(ip);
				} catch (Throwable t) {
					t.printStackTrace();
					e.setCancelled(true);
					if (t.getMessage() != null && t.getMessage().toLowerCase().contains("timeout")) {
						e.setCancelReason("§cCan't connect to §eserver-chef§c.\nPlease try again in 10-30 seconds");
					} else
						e.setCancelReason("§cAn error happened while joining.\nTry again in 10-30 seconds.\n§7" + t.getClass().getSimpleName() + ": " + t.getMessage());
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

	public void enableAttackMode() {
		if (attackDetectionRate.getOccurredEventsInTime(20, TimeUnit.SECONDS) == 0) {
			attackMode = true;
			if (attackDetectionRate.getOccurredEventsInMaxTime() == 0) {
				System.out.println("-----------------------------");
				System.out.println("§4§l[AntiBot] §fAttack mode enabled");
				System.out.println("-----------------------------");
			}
			attackDetectionRate.eventTriggered();
		}
	}

	@EventHandler
	public void onPostLogin(PostLoginEvent e) {
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName());
		if (Main.getDatenServer().getPlayers() != null)
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
			MessageManager.getManager(lang).playTitles(e.getPlayer());
	}

//	@EventHandler
//	public void a(ServerMessageEvent e) {
//		String inviter;
//		if (e.getChannel().equalsIgnoreCase("antijoinbot")) {
//			byte action = e.getBuffer().readByte();
//			if (action == 0) {
//				this.server = BungeeCord.getInstance().getServerInfo(e.getBuffer().readString());
//			} else if (action == 1) {
//				this.active = e.getBuffer().readBoolean();
//			} else if (action == 2) {
//				this.connectionsLimit = e.getBuffer().readInt();
//				this.connections = new CachedArrayList(this.time = e.getBuffer().readInt(), TimeUnit.MILLISECONDS);
//			} else if (action == 3) {
//				this.connections.add(new Object());
//			} else if (action == 4) {
//				inviter = e.getBuffer().readString();
//				for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
//					invite((Player) p, inviter);
//				}
//			}
//		}
//	}

//
//	@EventHandler
//	public void onKick(ServerKickEvent event) {
//		if (event.getKickedFrom().getName().startsWith("login")) {
//			IpData data = ipDatas.getIfPresent(event.getPlayer().getPendingConnection().getAddress().getAddress().getHostAddress());
//			if (data != null) {
//				data.getLoginHubLeaveRate().eventTriggered();
//			}
//		}
//	}
//
//	@EventHandler
//	public void onLeave(PlayerDisconnectEvent event) {
//		ProxiedPlayer plr = event.getPlayer();
//		if (joinAttempt.getIfPresent(plr.getName().toLowerCase()) == null && plr.getServer().getInfo().getName().startsWith("login")) {
//			IpData data = ipDatas.getIfPresent(plr.getPendingConnection().getAddress().getAddress().getHostAddress());
//			if (data != null) {
//				data.getLoginHubLeaveRate().eventTriggered();
//			}
//		}
//	}

	public static String getDurationBreakdown(long millis) {
		return getDurationBreakdown(millis, "now");
	}

	public static String getDurationBreakdown(long millis, String no) {
		return getDurationBreakdown(millis, no, new EnumMap<>(TimeUnit.class));
	}

	public static String getDurationBreakdown(long millis, String no, EnumMap<TimeUnit, String> mapping) {
		return getDurationBreakdown(millis, no, new EnumMap<>(TimeUnit.class), mapping);
	}

	public static String getDurationBreakdown(long millis, String no, EnumMap<TimeUnit, String> plural, EnumMap<TimeUnit, String> mapping) {
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
			sb.append(' ').append(days).append(' ').append(mapping.getOrDefault(TimeUnit.DAYS, "day"));
			if (days != 1) {
				sb.append(plural.getOrDefault(TimeUnit.DAYS, "s"));
			}
		}
		if (hours > 0) {
			sb.append(' ').append(hours).append(' ').append(mapping.getOrDefault(TimeUnit.HOURS, "hour"));
			if (hours != 1) {
				sb.append(plural.getOrDefault(TimeUnit.HOURS, "s"));
			}
		}
		if (minutes > 0) {
			sb.append(' ').append(minutes).append(' ').append(mapping.getOrDefault(TimeUnit.MINUTES, "minute"));
			if (minutes != 1) {
				sb.append(plural.getOrDefault(TimeUnit.MINUTES, "s"));
			}
		}
		if (seconds > 0) {
			sb.append(' ').append(seconds).append(' ').append(mapping.getOrDefault(TimeUnit.SECONDS, "second"));
			if (seconds != 1) {
				sb.append(plural.getOrDefault(TimeUnit.SECONDS, "s"));
			}
		}
		return sb.substring(1);
	}
}
//event.join.kickBan - §cYou were banned %s0 §cfrom the Network! \n§3Reason: §c%s1 \n \n§aYou can write an unban-request at §ewww.EpicPvP.org [length,reson]
