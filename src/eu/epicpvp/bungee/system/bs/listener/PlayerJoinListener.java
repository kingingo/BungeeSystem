package eu.epicpvp.bungee.system.bs.listener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.wolveringer.BungeeUtil.ClientVersion;
import dev.wolveringer.BungeeUtil.ClientVersion.BigClientVersion;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.client.BungeecordDatenClient;
import eu.epicpvp.bungee.system.bs.information.InformationManager;
import eu.epicpvp.bungee.system.bs.listener.util.IpData;
import eu.epicpvp.bungee.system.bs.listener.util.Rate;
import eu.epicpvp.bungee.system.bs.message.MessageManager;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.dataserver.protocoll.packets.PacketInChangePlayerSettings;
import eu.epicpvp.dataserver.protocoll.packets.PacketOutPacketStatus;
import eu.epicpvp.datenclient.client.ClientWrapper;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.client.PacketHandleErrorException;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.datenserver.definitions.dataserver.player.Setting;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

	public PlayerJoinListener() {
		instance = this;
		ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), this::reloadFiles, 10 + new Random().nextInt(19), 30, TimeUnit.SECONDS);
	}

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	@Getter
	private static PlayerJoinListener instance;
	@Getter
	private static boolean attackMode = false;
	@Getter
	@Setter
	private static boolean antibotLog = true;

	public static boolean vhostBlockage = true;

	private Rate attackDetectionRate = new Rate(30, TimeUnit.SECONDS);
	private Rate filteredRate = new Rate(10, TimeUnit.SECONDS);
	private Rate allowRate = new Rate(1, TimeUnit.SECONDS);
	private Rate overallConnectionRate = new Rate(2, TimeUnit.SECONDS, 5500, TimeUnit.MILLISECONDS);
	private Set<String> premiumNames = Collections.synchronizedSet(new HashSet<>());
	private Cache<String, IpData> ipDatas =
			CacheBuilder.newBuilder()
					.expireAfterAccess(2, TimeUnit.HOURS)
					.build();
	private Cache<String, Set<String>> nameIp =
			CacheBuilder.newBuilder()
					.expireAfterAccess(2, TimeUnit.HOURS)
					.build();
	private Map<String, String> ipBlacklist = new ConcurrentHashMap<>();
	private Set<String> nameWhitelist = Collections.synchronizedSet(new HashSet<>());
	private Map<String, String> ipWhitelist = new ConcurrentHashMap<>();
	private Map<String, String> automaticIpBlackList = new ConcurrentHashMap<>();
	private boolean automaticIpBlackListChanged = false;

	@EventHandler
	@SneakyThrows(ExecutionException.class)
	public void onPreLogin(PreLoginEvent event) {
		if (!Main.loaded) {
			event.setCancelled(true);
			event.setCancelReason("§cStill setting up bungeecord.\n§aPlease try again in 10-20 seconds.");
			return;
		}
		if (!Main.getDatenServer().isActive()) {
			event.setCancelled(true);
			event.setCancelReason("§cCan't connect to §eserver-chef§c.\n§aPlease try again in 10-30 seconds.");
			return;
		}

		PendingConnection connection = event.getConnection();
		int versionNumber = connection.getVersion();
		ClientVersion version = ClientVersion.fromProtocoll(versionNumber);
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
		if (version == null || version == ClientVersion.v1_9_1 || version == ClientVersion.v1_9_2 || version == ClientVersion.v1_9_3
				|| (version.getBigVersion() != BigClientVersion.v1_8 && version.getBigVersion() != BigClientVersion.v1_9 && version != ClientVersion.v1_10_0)) {
			event.setCancelled(true);
			event.setCancelReason("§cYour minecraft version is not supported. Please use 1.8.X, 1.9.0, 1.9.4 or 1.10.X");
			System.out.println("Player " + name + " tried to connect with an unsupported version (" + versionNumber + ") with ip " + ip);
			return;
		}
		if (name.length() < 3 || name.length() > 16) {
			event.setCancelled(true);
			event.setCancelReason("§cInvalid name length!");
			return;
		}
		for (char c : name.toCharArray()) {
			if ((c < '0' || c > '9') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && c != '_') {
				event.setCancelled(true);
				event.setCancelReason("§cInvalid characters in name!");
				return;
			}
		}
		// AntiBot whitelists and blacklists
		if (ipWhitelist.containsKey(ip)) {
			allowLogin(event, connection, name, ip, true);
			return;
		}
		String lowerCaseName = name.toLowerCase();
		if (nameWhitelist.contains(lowerCaseName)) {
			allowLogin(event, connection, name, ip, true);
			return;
		}
		if (premiumNames.contains(lowerCaseName)) {
			allowLogin(event, connection, name, ip, true);
			return;
		}
		String blockInfo = automaticIpBlackList.get(ip);
		if (blockInfo != null) {
			event.setCancelled(true);
			event.setCancelReason("§cEs wurden ungewöhnliche Aktivitäten deiner IP festgestellt.\n" +
					"§cDu darfst daher nicht über diese IP spielen.\n" +
					"§aBitte melde dich bei unserem Teamspeak-Support, falls dies ein Fehler sein sollte.");
			logAntiBot("automatic blacklisted ip " + ip + " tried to join with name: " + name + " (blockinfo: " + blockInfo + ')');
		}
		blockInfo = ipBlacklist.get(ip);
		if (blockInfo != null) {
			event.setCancelled(true);
			event.setCancelReason("§cDeine IP wurde gesperrt.\n" +
					"§aBitte melde dich bei unserem Teamspeak-Support, falls dies ein Fehler sein sollte.");
			logAntiBot("manually blacklisted ip " + ip + " tried to join with name: " + name + " (blockinfo: " + blockInfo + ')');
			return;
		}

		// player's who send that they typed in axnj9ef90out4.epicpvp.eu are blocked, because no player will ever do a cname lookup on play.epicpvp.eu
		if (vhostBlockage && connection.getVirtualHost().getHostString().toLowerCase().contains("axnj9ef90out4")) {
			event.setCancelled(true);
			event.setCancelReason("§cDein Joinversuch wurde blockiert.\n" +
					"§cBitte nutze folgende IP um dich auf unser Netzwerk zu verbinden:\n" +
					"§fepicpvp.eu\n" +
					"§aSolltest du bereits über diese IP joinen und diesen Fehler erhalten, so melde dich bitte bei unserem Teamspeak-Support.");
			return;
		}

		// AntiBot ip-based rate-Limit
		IpData data = ipDatas.get(ip, () -> new IpData(ip));
		Cache<String, Integer> nameJoinCounter = data.getNameJoinCounter();
		Integer joins = nameJoinCounter.getIfPresent(lowerCaseName);
		Rate differentNameJoinRate = data.getDifferentNameJoinRate();
		if (joins == null) {
			nameJoinCounter.put(lowerCaseName, 1);
			differentNameJoinRate.eventTriggered();
		} else {
			nameJoinCounter.put(lowerCaseName, joins + 1);
		}
		int differentNameJoins2H = differentNameJoinRate.getOccurredEventsInMaxTime();
		if (differentNameJoins2H >= 4) {
			event.setCancelled(true);
			filteredRate.eventTriggered();
			event.setCancelReason("§cEs wurden ungewöhnliche Aktivitäten deiner IP festgestellt.\n" +
					"§c§lExtrem WICHTIG:\n" +
					"§f§lNicht mit einem anderen Namen verbinden!§7 (sonst kann deine IP gesperrt werden)\n\n" +
					"§aVerwende keinen Proxy, VPN, oder andere Dienste, die deine Internetverbindung umleiten, um auf EpicPvP zu spielen.\n" +
					"§aBitte melde dich bei unserem Teamspeak-Support, falls dies ein Fehler sein sollte.");
			if (differentNameJoins2H < 5) {
				logAntiBot("ip " + ip + " tried to join with " + differentNameJoins2H + " different names in 2h, latest name: " + name);
			} else {
				automaticIpBlackList.put(ip, DATE_FORMAT.format(new Date()) + ": ip logged in with " + differentNameJoins2H + " different names in 2h");
				logAntiBot("§eip " + ip + " tried to join with " + differentNameJoins2H + " different names in 2h, latest name: " + name + ", ip blacklisted");
				automaticIpBlackListChanged = true;
			}
			return;
		}
		int differentNameJoins1M = differentNameJoinRate.getOccurredEventsInTime(1, TimeUnit.MINUTES);
		if (attackDetectionRate.getOccurredEventsInMaxTime() != 0 && differentNameJoins1M >= 2) {
			event.setCancelled(true);
			filteredRate.eventTriggered();
			event.setCancelReason("§cEs wurden ungewöhnliche Aktivitäten deiner IP festgestellt.\n" +
					"§c§lWichtig:\n" +
					"§aVerwende keinen Proxy, VPN, oder andere Dienste, die deine Internetverbindung umleiten, um auf EpicPvP zu spielen.\n" +
					"§aBitte versuche dich über deine IP nur mit einem Namen innerhalb von 5 Minuten zu verbinden.\n" +
					"§aBitte melde dich bei unserem Teamspeak-Support, falls dies ein Fehler sein sollte.\n" +
					"§8Diese Restriktion ist nur während einer Joinbot-Attacke aktiv.");
			logAntiBot("ip " + ip + " tried to join with " + differentNameJoins1M + " different names in 1min, latest name: " + name);
			return;
		}
		Set<String> lastConnectedIpsForName = nameIp.get(ip, LinkedHashSet::new);

		lastConnectedIpsForName.add(ip);
		if (lastConnectedIpsForName.size() > (attackMode ? 1 : 2)) {
			event.setCancelled(true);
			filteredRate.eventTriggered();
			event.setCancelReason("§cEs wurden ungewöhnliche Aktivitäten festgestellt.\n" +
					"§aVerwende keinen Proxy, VPN, oder andere Dienste, die deine Internetverbindung umleiten, um auf EpicPvP zu spielen.\n" +
					"§aBitte melde dich bei unserem Teamspeak-Support, falls dies ein Fehler sein sollte.");
			logAntiBot("player " + name + " tried to join with " + lastConnectedIpsForName.size() + " different ips in 1min, ips so far: " + lastConnectedIpsForName);
		}
		if (filteredRate.getOccurredEventsInMaxTime() > 5) {
			enableAttackMode();
		}

//		Rate loginHubLeaveRate = data.getLoginHubLeaveRate();
//		int occurredEventsInTime = loginHubLeaveRate.getOccurredEventsInTime(20, TimeUnit.SECONDS);
//		if (occurredEventsInTime >= 4) {
//			event.setCancelled(true);
//			event.setCancelReason("§cSuspicious behaviour.\n§aBitte melde dich auf unserem Teamspeak, falls dies ein Fehler sein sollte.");
//			System.out.println("[AntiBot] " + ip + " tried to rejoin after leaving loginhub " + occurredEventsInTime + " times in 20sec (kicks count twice), latest name: " + name);
//		}
//		loginHubLeaveRate.eventTriggered();

		//Antibots overall rate-Limit
		double averagePerSecond = overallConnectionRate.getAveragePerSecondOnMaxTime();
		overallConnectionRate.eventTriggered();
//		int allowedJoinsLastSecond = allowRate.getOccurredEventsInMaxTime(); //TODO allowRate
		if (/*allowedJoinsLastSecond > 2 && */averagePerSecond >= 5) { //TODO allowRate
			enableAttackMode();
			event.setCancelled(true);
			event.setCancelReason("§cAktuell versuchen zu viele Spieler sich gleichzeitig anzumelden.\n" +
					"§aVersuche es bitte in ein paar Sekunden erneut.");
			logAntiBot("Cancelled login of " + ip + " / " + name + " because global rate limit being at " + averagePerSecond + " joins/sec in average over the last 2 sec");
			return;
		}
		if (attackMode && attackDetectionRate.getOccurredEventsInMaxTime() == 0) {
			attackMode = false;
			System.out.println("------------------------------");
			logAntiBot("§fAttack mode disabled", true);
			System.out.println("------------------------------");
		}
		allowLogin(event, connection, name, ip, false);
	}

	private void allowLogin(PreLoginEvent event, PendingConnection connection, String name, String ip, boolean isWhitelist) {
		if (!isWhitelist) {
//			allowRate.eventTriggered(); //TODO allowRate
		}

		//lets do stuff async
		event.registerIntent(Main.getInstance());
		try {
			ProxyServer.getInstance().getScheduler().runAsync(Main.getInstance(), () -> asyncLoginLogic(event, connection, name, ip));
		} catch (Throwable t) {
			event.completeIntent(Main.getInstance());
			throw t;
		}
	}

	private void asyncLoginLogic(PreLoginEvent event, PendingConnection connection, String name, String ip) {
		long start = System.currentTimeMillis();
		try {
			/*
			 * Clean up old cache
			 */
			BungeecordDatenClient datenServer = Main.getDatenServer();
			ClientWrapper datenClient = datenServer.getClient();
			datenClient.clearCacheForPlayer(name);
			datenClient.clearCacheForPlayer(connection.getUniqueId());

			LoadedPlayer lplayer = datenClient.getPlayerAndLoad(name);
			System.out.println("Connect: Name: " + name + " Player: " + lplayer.getName() + " PlayerID: " + lplayer.getPlayerId());
			datenClient.clearCacheForPlayer(name);
			/*//TODO cant get playerId before premium login
			try{
				LoadedPlayer playerUUID = Main.getDatenServer().getClient().getPlayerAndLoad(UUID.fromString(event.getConnection().getUUID()));
				if(playerUUID != null && !playerUUID.getName().equalsIgnoreCase(event.getConnection().getName())){
					if(playerUUID.isOnlineSync()){
						lplayer.setName(lplayer.getName()+"_old_player_overwridden_by_"+event.getConnection().getName());
						playerUUID.setName(event.getConnection().getName());
						lplayer = playerUUID;
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			*/

			if (datenServer.getPlayerCount() > Integer.parseInt(InformationManager.getManager().getInfo("maxPlayers"))) {
				if (!PermissionManager.getManager().hasPermission(lplayer.getPlayerId(), "proxy.join.full")) {
					event.setCancelled(true);
					event.setCancelReason(Main.getTranslationManager().translate("proxy.join.full", lplayer));
					return;
				}
			}

			try {
				if (lplayer.isPremiumSync()) {
					connection.setUniqueId(lplayer.getUUID());
					connection.setOnlineMode(true);
//					System.out.println("Player premium");
				} else {
					connection.setUniqueId(lplayer.getUUID());
					connection.setOnlineMode(false);
//					System.out.println("Player cracked");
				}
			} catch (PacketHandleErrorException ex) {
				for (PacketOutPacketStatus.Error er : ex.getErrors())
					System.out.println(er.getId() + ":" + er.getMessage());
				ex.printStackTrace();
				event.setCancelled(true);
				event.setCancelReason("§cAn error happened while joining.\n§cWe couldn't check your premium state.\nTry again in 10-30 seconds");
				return;
			}

			/*
			List<BanEntity> entries = lplayer.getBanStats(event.getConnection().getAddress().getHostString(),1).getSync();
			if (entries.size() > 0 && entries.get(0).isActive()) {
				BanEntity response = entries.get(0);
				String time;
				if (response.isTempBanned()) {
					time = getDurationBreakdown(response.getEnd() - System.currentTimeMillis());
				} else {
					time = "§cPermanent";
				}
				event.setCancelled(true);
				event.setCancelReason(Main.getTranslationManager().translate("event.join.kickBan", lplayer, new Object[] { time, response.getReson(),response.getLevel() }));
				return;
			}
			*/
			if ("true".equalsIgnoreCase(InformationManager.getManager().getInfo("whitelistActive")) && !PermissionManager.getManager().hasPermission(lplayer.getPlayerId(), "epicpvp.whitelist.bypass")) {
				String message = InformationManager.getManager().getInfo("whitelistMessage");
				if (message == null)
					message = "§cWhitelist is active!";
				event.setCancelled(true);
				event.setCancelReason(message);
				return;
			}
			lplayer.setIp(ip);
		} catch (Throwable t) {
			t.printStackTrace();
			event.setCancelled(true);
			if (t.getMessage() != null && t.getMessage().toLowerCase().contains("timeout")) {
				event.setCancelReason("§cCan't connect to §eserver-chef§c.\nPlease try again in 10-30 seconds.\n§7 Code: to");
			} else
				event.setCancelReason("§cAn error happened while joining.\n§cTry again in 10-30 seconds.\n§7" + t.getClass().getSimpleName() + ": " + t.getMessage());
		} finally {
			event.completeIntent(Main.getInstance());
		}
		long end = System.currentTimeMillis();
		if (end - start > 500)
			System.out.println("LoginEvent for player " + name + " needed more than 500ms (" + (end - start) + ")");
	}

	@EventHandler
	public void onPostLogin(PostLoginEvent e) {
		ProxiedPlayer player = e.getPlayer();
		UUID playerUuid = player.getUniqueId();
		if (playerUuid.version() == 4) {
			premiumNames.add(player.getName().toLowerCase());
		}
		ClientWrapper client = Main.getDatenServer().getClient();
		LoadedPlayer lplayer = client.getPlayerAndLoad(player.getName());
		if (Main.getDatenServer().getPlayers() != null)
			Main.getDatenServer().getPlayers().add(player.getName());
		UUID lplayerUuid = lplayer.getUUID();
		if (!lplayerUuid.equals(playerUuid)) {
			if (lplayerUuid.equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes()))) {
				System.out.println("Switching players uuid (cracked uuid to premium) (But premium is set!)");
				client.writePacket(new PacketInChangePlayerSettings(lplayer.getPlayerId(), Setting.UUID, playerUuid.toString())).getSync();
				client.clearCacheForPlayer(lplayer);
				lplayer = client.getPlayerAndLoad(player.getName());
				System.out.println("New uuid: " + lplayerUuid);
			}
		}
		LanguageType lang = lplayer.getLanguageSync();
		PermissionManager.getManager().loadPlayer(playerUuid);
		if (player.getPendingConnection().isOnlineMode())
			MessageManager.getManager(lang).playTitles(player);
	}

	private static void logAntiBot(String msg) {
		logAntiBot(msg, false);
	}

	private static void logAntiBot(String msg, boolean bypass) {
		if (!bypass && !antibotLog) {
			return;
		}
		if (attackMode) {
			System.out.println("§4§l[AntiBot] §6[AttackMode] §7" + msg);
		} else {
			System.out.println("§4§l[AntiBot] §7" + msg);
		}
	}

	public void enableAttackMode() {
		if (attackDetectionRate.getOccurredEventsInTime(20, TimeUnit.SECONDS) == 0) {
			attackMode = true;
			if (attackDetectionRate.getOccurredEventsInMaxTime() == 0) {
				System.out.println("-----------------------------");
				logAntiBot("§fAttack mode enabled", true);
				System.out.println("-----------------------------");
			}
			attackDetectionRate.eventTriggered();
		}
	}

	public void reloadFiles() {
		File folder = new File("/root/antibots");
		folder.mkdir();
		try {
//			System.out.println("Reloading ip_blacklist.txt...");
			ipBlacklist.clear();
			File ipBlacklistFile = new File(folder, "ip_blacklist.txt");
			ipBlacklistFile.createNewFile();
			Files.readAllLines(ipBlacklistFile.toPath())
					.forEach(line -> {
						int pos = line.indexOf(':');
						if (pos == -1) {
							ipBlacklist.put(line, "");
						} else {
							ipBlacklist.put(line.substring(0, pos), line.substring(pos));
						}
					});
//			System.out.println("Successfully reloaded ip_blacklist.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
//			System.out.println("Reloading name_whitelist.txt...");
			nameWhitelist.clear();
			File nameWhitelistFile = new File(folder, "name_whitelist.txt");
			nameWhitelistFile.createNewFile();
			Files.readAllLines(nameWhitelistFile.toPath())
					.forEach(line -> {
						nameWhitelist.add(line);
					});
//			System.out.println("Successfully reloaded name_whitelist.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
//			System.out.println("Reloading ip_whitelist.txt...");
			ipWhitelist.clear();
			File ipWhitelistFile = new File(folder, "ip_whitelist.txt");
			ipWhitelistFile.createNewFile();
			Files.readAllLines(ipWhitelistFile.toPath())
					.forEach(line -> {
						int pos = line.indexOf(':');
						if (pos == -1) {
							ipWhitelist.put(line, "");
						} else {
							ipWhitelist.put(line.substring(0, pos), line.substring(pos));
						}
					});
//			System.out.println("Successfully reloaded ip_whitelist.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		File automaticIpBlacklistFile = new File("/root/antibots/automatic_ip_blacklist.txt");
		Path automaticIpBlacklistPath = automaticIpBlacklistFile.toPath();
		try {
			automaticIpBlacklistFile.createNewFile();
//			System.out.println("Reloading automatic_ip_blacklist.txt...");
			Files.readAllLines(automaticIpBlacklistPath)
					.forEach(line -> {
						int pos = line.indexOf(':');
						if (pos == -1) {
							automaticIpBlackList.put(line, "");
						} else {
							String ip = line.substring(0, pos);
							String data = line.substring(pos);
							if (!data.equalsIgnoreCase("remove")) {
								automaticIpBlackList.put(ip, data);
							} else {
								automaticIpBlackList.remove(ip);
							}
						}
					});
//			System.out.println("Successfully reloaded automatic_ip_blacklist.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (automaticIpBlackListChanged) {
			try {
				BufferedWriter bufferedWriter = Files.newBufferedWriter(automaticIpBlacklistPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				automaticIpBlackList.forEach((ip, data) -> {
					try {
						if (data.isEmpty()) {
							bufferedWriter.write(ip);
						} else {
							bufferedWriter.write(ip);
							bufferedWriter.append(':');
							bufferedWriter.write(data);
						}
						bufferedWriter.newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				bufferedWriter.close();
				automaticIpBlackListChanged = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
