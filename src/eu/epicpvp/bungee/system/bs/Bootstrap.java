package eu.epicpvp.bungee.system.bs;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import dev.wolveringer.BungeeUtil.AsyncCatcher;
import dev.wolveringer.BungeeUtil.ClientVersion.BigClientVersion;
import dev.wolveringer.BungeeUtil.ClientVersion.ProtocollVersion;
import dev.wolveringer.BungeeUtil.PacketLib;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.Packet.ProtocollId;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutChat;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutEntityHeadRotation;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutEntityProperties;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutEntityTeleport;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutKeepAlive;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutPlayerListHeaderFooter;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutSetExperience;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutSpawnPostition;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutStatistic;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutUpdateHealth;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutUpdateSign;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutWorldParticles;
import dev.wolveringer.util.UtilReflection;
import eu.epicpvp.bungee.fakeserver.ServerConfiguration;
import eu.epicpvp.bungee.fakeserver.packets.PacketPlayInKeepAlive;
import eu.epicpvp.bungee.fakeserver.packets.PacketPlayOutMapChunk;
import eu.epicpvp.bungee.fakeserver.world.WorldFileReader;
import eu.epicpvp.bungee.system.actionbar.ActionBar;
import eu.epicpvp.bungee.system.afk.AfkListener;
import eu.epicpvp.bungee.system.ban.BanServerMessageListener;
import eu.epicpvp.bungee.system.ban.BannedServerListener;
import eu.epicpvp.bungee.system.ban.BannedServerManager;
import eu.epicpvp.bungee.system.booster.BoosterManager;
import eu.epicpvp.bungee.system.booster.CMD_BOOSTER;
import eu.epicpvp.bungee.system.bs.client.BungeecordDatenClient;
import eu.epicpvp.bungee.system.bs.commands.CommandBDebug;
import eu.epicpvp.bungee.system.bs.commands.CommandBan;
import eu.epicpvp.bungee.system.bs.commands.CommandBanInfo;
import eu.epicpvp.bungee.system.bs.commands.CommandBroad;
import eu.epicpvp.bungee.system.bs.commands.CommandBuild;
import eu.epicpvp.bungee.system.bs.commands.CommandClearChat;
import eu.epicpvp.bungee.system.bs.commands.CommandClient;
import eu.epicpvp.bungee.system.bs.commands.CommandCreative;
import eu.epicpvp.bungee.system.bs.commands.CommandEvent;
import eu.epicpvp.bungee.system.bs.commands.CommandGList;
import eu.epicpvp.bungee.system.bs.commands.CommandGilde;
import eu.epicpvp.bungee.system.bs.commands.CommandGunGame;
import eu.epicpvp.bungee.system.bs.commands.CommandHub;
import eu.epicpvp.bungee.system.bs.commands.CommandKicken;
import eu.epicpvp.bungee.system.bs.commands.CommandMOTD;
import eu.epicpvp.bungee.system.bs.commands.CommandMoney;
import eu.epicpvp.bungee.system.bs.commands.CommandNews;
import eu.epicpvp.bungee.system.bs.commands.CommandNick;
import eu.epicpvp.bungee.system.bs.commands.CommandPerformance;
import eu.epicpvp.bungee.system.bs.commands.CommandPermission;
import eu.epicpvp.bungee.system.bs.commands.CommandPremium;
import eu.epicpvp.bungee.system.bs.commands.CommandPvP;
import eu.epicpvp.bungee.system.bs.commands.CommandPwChange;
import eu.epicpvp.bungee.system.bs.commands.CommandResourcepack;
import eu.epicpvp.bungee.system.bs.commands.CommandRestart;
import eu.epicpvp.bungee.system.bs.commands.CommandRoulett;
import eu.epicpvp.bungee.system.bs.commands.CommandSendServer;
import eu.epicpvp.bungee.system.bs.commands.CommandServer;
import eu.epicpvp.bungee.system.bs.commands.CommandSkin;
import eu.epicpvp.bungee.system.bs.commands.CommandSky;
import eu.epicpvp.bungee.system.bs.commands.CommandTBuild;
import eu.epicpvp.bungee.system.bs.commands.CommandTeamspeak;
import eu.epicpvp.bungee.system.bs.commands.CommandTempBan;
import eu.epicpvp.bungee.system.bs.commands.CommandUnban;
import eu.epicpvp.bungee.system.bs.commands.CommandVersus;
import eu.epicpvp.bungee.system.bs.commands.CommandVorbau;
import eu.epicpvp.bungee.system.bs.commands.CommandVote;
import eu.epicpvp.bungee.system.bs.commands.CommandWarZ;
import eu.epicpvp.bungee.system.bs.commands.CommandWhereIs;
import eu.epicpvp.bungee.system.bs.commands.CommandWhitelist;
import eu.epicpvp.bungee.system.bs.commands.CommandaddServer;
import eu.epicpvp.bungee.system.bs.commands.CommanddelServer;
import eu.epicpvp.bungee.system.bs.commands.CommandgPing;
import eu.epicpvp.bungee.system.bs.consolencommand.CommandHashMap;
import eu.epicpvp.bungee.system.bs.consolencommand.PrefixCommandRegistry;
import eu.epicpvp.bungee.system.bs.information.InformationManager;
import eu.epicpvp.bungee.system.bs.listener.ChatListener;
import eu.epicpvp.bungee.system.bs.listener.CommandConsoleTeamMessage;
import eu.epicpvp.bungee.system.bs.listener.InvalidChatListener;
import eu.epicpvp.bungee.system.bs.listener.PingListener;
import eu.epicpvp.bungee.system.bs.listener.PlayerJoinListener;
import eu.epicpvp.bungee.system.bs.listener.PlayerKickListener;
import eu.epicpvp.bungee.system.bs.listener.ServerListener;
import eu.epicpvp.bungee.system.bs.listener.SkinListener;
import eu.epicpvp.bungee.system.bs.listener.TeamChatListener;
import eu.epicpvp.bungee.system.bs.listener.TimeListener;
import eu.epicpvp.bungee.system.bs.listener.WarzTexturePackListener;
import eu.epicpvp.bungee.system.bs.login.LoginManager;
import eu.epicpvp.bungee.system.bs.login.PlayerDisconnectListener;
import eu.epicpvp.bungee.system.bs.message.MessageManager;
import eu.epicpvp.bungee.system.bs.packets.PacketPlayInResourcepackStatus;
import eu.epicpvp.bungee.system.bs.packets.PacketPlayOutResourcepack;
import eu.epicpvp.bungee.system.bs.servermanager.ServerManager;
import eu.epicpvp.bungee.system.chat.ChatManager;
import eu.epicpvp.bungee.system.mysql.MySQL;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.bungee.system.report.commands.CMD_Report;
import eu.epicpvp.bungee.system.report.info.ActionBarInformation;
import eu.epicpvp.bungee.system.skin.SkinCacheManager;
import eu.epicpvp.bungee.system.slotmachine.RoulettHistory;
import eu.epicpvp.bungee.system.teamspeak.TeamspeakListener;
import eu.epicpvp.bungee.system.translation.TranslationHandler;
import eu.epicpvp.dataserver.protocoll.packets.PacketOutPlayerSettings.SettingValue;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.client.debug.Debugger;
import eu.epicpvp.datenclient.event.EventManager;
import eu.epicpvp.datenclient.gilde.GildManager;
import eu.epicpvp.datenserver.definitions.dataserver.player.Setting;
import eu.epicpvp.datenserver.definitions.events.EventType;
import eu.epicpvp.thread.ThreadFactory;
import eu.epicpvp.thread.ThreadRunner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants.Direction;

@AllArgsConstructor
public class Bootstrap {
	@Getter
	File dataFolder;
	public static final Predicate<String> DEBUGGER_FILTER = s -> {
		s = s.toLowerCase();
		if (s.startsWith("readed packet in "))
			return false;
		if (s.startsWith("write packet "))
			return false;
		if (s.startsWith("packet sucessfull handled ("))
			return false;
		if (s.startsWith("skin data: "))
			return false;
		if (s.startsWith("reciving "))
			return false;
		if (s.startsWith("handeling "))
			return false;
		if (PlayerJoinListener.isAttackMode()) {
			if (s.startsWith("player settings for"))
				return false;
			if (s.startsWith("   uuid ->"))
				return false;
			if (s.startsWith("   name ->"))
				return false;
			if (s.startsWith("   nickname ->"))
				return false;
			if (s.contains("loaded -> playerid".toLowerCase()))
				return false;
			if (s.contains("   LANGUAGE ->".toLowerCase()))
				return false;
			if (s.contains("   PREMIUM_LOGIN ->".toLowerCase()))
				return false;
			if (s.contains("   LAST_PASSWORD_CHANGED ->".toLowerCase()))
				return false;
		}
		return true;
	};

	public void onEnable() {
		onEnable0();
	}

	public void unregisterPacket(Protocol p, Direction d, Class clazz, ProtocollId... ids) {
		for (ProtocollId id : ids) {
			Packet.unregisterPacket(id.getVersion(), p, d, id.getId());
			System.out.println("Unregistered packet " + id.getId() + " for " + id.getVersion() + " in dir " + d);
		}
	}

	public void onEnable0() {
		AsyncCatcher.disableAll();
		Debugger.setFilter(DEBUGGER_FILTER);
		//unregister unused packets
		unregisterPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutWorldParticles.class, new ProtocollId(BigClientVersion.v1_8, 0x2A), new ProtocollId(BigClientVersion.v1_9, 0x22), new ProtocollId(BigClientVersion.v1_10, 0x22));
		unregisterPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutPlayerListHeaderFooter.class, new ProtocollId(BigClientVersion.v1_8, 0x47), new ProtocollId(BigClientVersion.v1_9, 0x48), new ProtocollId(ProtocollVersion.v1_9_4, 0x47), new ProtocollId(BigClientVersion.v1_10, 0x47));// ->0x48
		unregisterPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutEntityHeadRotation.class, new ProtocollId(BigClientVersion.v1_8, 0x19), new ProtocollId(BigClientVersion.v1_9, 0x34), new ProtocollId(BigClientVersion.v1_10, 0x34));
		unregisterPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutStatistic.class, new ProtocollId(BigClientVersion.v1_8, 0x37), new ProtocollId(BigClientVersion.v1_9, 0x07), new ProtocollId(BigClientVersion.v1_10, 0x07)); // -> 0x07

		try {
			Class.forName(UtilBungeeCord.class.getName());
		} catch (ClassNotFoundException ex) {
		}
		final Main plugin = Main.getInstance();
		if (false)
			ThreadFactory.setInstance(new ThreadFactory() {
				@Override
				public ThreadRunner createThread(Runnable run) {
					return new ThreadRunner() {
						ScheduledTask task;
						Thread current;

						@Override
						public void stop() {
							if (task != null) {
								task.cancel();
								try {
									current.interrupt();
								} catch (Exception e) {
									e.printStackTrace();
								}
								if (current.getState() != State.TERMINATED)
									try {
										current.stop();
									} catch (Exception e) {
										e.printStackTrace();
									}
							} else
								System.err.println("Try to cancel an not started task...");
							task = null;
						}

						@Override
						public void start() {
							if (task != null) {
								System.err.println("Try to start a task twice!");
								throw new RuntimeException("Task is alredy runnings");
							}
							task = BungeeCord.getInstance().getScheduler().runAsync(plugin, new Runnable() {
								@Override
								public void run() {
									current = Thread.currentThread();
									try {
										run.run();
									} catch (Exception e) {
										System.err.println("Having error while excecuting Runable " + run.getClass() + ":");
										e.printStackTrace();
									}
								}
							});
						}

						@Override
						public String toString() {
							return current != null ? current.toString() + "/RUNNING" : super.toString() + "/STOPPED";
						}

						@Override
						public Thread getThread() {
							return current;
						}
					};
				}
			});
		Configuration conf = null;
		try {
			if (!new File(getDataFolder(), "config.yml").exists()) {
				getDataFolder().mkdirs();
				new File(getDataFolder(), "config.yml").createNewFile();
				conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
				conf.set("mysql.host", "localhost");
				conf.set("mysql.port", 3306);
				conf.set("mysql.db", "none");
				conf.set("mysql.user", "root");
				conf.set("mysql.passwort", "underknown");
				conf.set("serverId", "underknown");
				conf.set("datenserver.host", "localhost");
				conf.set("datenserver.port", 1111);
				conf.set("datenserver.passwort", "HelloWorld");
				conf.set("server.afk.world", "worlds/afk/");
				conf.set("server.banned.world", "worlds/banned/");
				conf.set("server.chunksize", 2);
				if (conf.get("gilde.enabled") == null) {
					conf.set("gilde.enabled", "false");
				}
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(conf, new File(getDataFolder(), "config.yml"));
			} else
				conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			MySQL.setInstance(new MySQL(conf.getString("mysql.host"), conf.getInt("mysql.port") + "", conf.getString("mysql.db"), conf.getString("mysql.user"), conf.getString("mysql.passwort")));
			MySQL.getInstance().connect();
			plugin.serverId = conf.getString("serverId");
			if (!MySQL.getInstance().isConnected()) {
				BungeeCord.getInstance().getConsole().sendMessage("§cCant connect to MySQL. Restart....");
				UtilBungeeCord.restart();
				return;
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		final Configuration configuration = conf;

		ActionBar.setInstance(new ActionBar());
		ChatManager.setInstance(new ChatManager());

		Main.data = new BungeecordDatenClient(plugin.serverId, new InetSocketAddress(configuration.getString("datenserver.host"), configuration.getInt("datenserver.port")));
		Main.data.setPassword(configuration.getString("datenserver.passwort"));
		BungeeCord.getInstance().getScheduler().runAsync(plugin, new Runnable() {
			@Override
			public void run() {
				while (!Main.isRestarting()) {
					while (!Main.data.isActive()) {
						if (Main.data.isConnecting()) {
							System.out.println("§6Connecting to datenserver....");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
						if (Main.isRestarting())
							return;
						System.out.println("§aTry to connect to dataserver");
						try {
							Main.data.start();
							Main.data.getClient().getHandle().getPingManager().ping(); //init first ping
							for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
								try {
									if (PermissionManager.getManager().hasPermission(p, "epicpvp.bc.dataserver"))
										p.sendMessage(Main.getTranslationManager().translate("prefix", p) + "§aDatenserver connected!");
								} catch (Exception ex) {
								}
							}
						} catch (Exception e) {
							System.out.println("§cCant connect to DatenServer [" + ((InetSocketAddress) Main.data.getAddress()).getHostName() + ":" + ((InetSocketAddress) Main.data.getAddress()).getPort() + "]. Reason: " + e.getMessage() + " . Try it again in 1 second.");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}

							for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
								try {
									if (p.hasPermission("epicpvp.bc.dataserver"))
										p.sendMessage("§cDatenserver offline!");
								} catch (Exception ex) {
								}
							}
							continue;
						}
						System.out.println("§aSuccessful connected");
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		System.out.println("Pause main thread while client try to connect!");
		while (Main.data.getClient() == null || !Main.data.getClient().getHandle().isHandshakeCompleted()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
		}

		Main.translationManager = new TranslationHandler(Main.getDatenServer().getClient().getTranslationManager());

		System.out.println("Checking for translation updates:");
		try {
			Main.getTranslationManager().updateTranslations();
		} catch (Exception e) {
			System.out.println("§cCould not update tragslations:");
			e.printStackTrace();
		}
		System.out.println("All translations are now up to date!");

		LoginManager.setManager(new LoginManager());
		InformationManager.setManager(new InformationManager());
		PermissionManager.setManager(new PermissionManager()); //TODO load
		ServerManager.setManager(new ServerManager());
		ServerManager.getManager().loadServers();
		MessageManager.start();
//		NickHandler.setInstance(new NickHandler());
		RoulettHistory.history = new RoulettHistory();
		BungeeCord.getInstance().getScheduler().runAsync(plugin, new Runnable() {
			public void run() {
				PermissionManager.getManager().loadGroups();
			}
		});
		Main.boosterManager = new BoosterManager();
		Main.getBoosterManager().init();

		PluginManager pluginManager = BungeeCord.getInstance().getPluginManager();
		pluginManager.registerCommand(plugin, new CommandBDebug("bdebug"));
		pluginManager.registerCommand(plugin, new CommandCreative("creative"));
		pluginManager.registerCommand(plugin, new CommandNews("news"));
		pluginManager.registerCommand(plugin, new CommandBroad("broad"));
		pluginManager.registerCommand(plugin, new CommandKicken("kicken"));
		pluginManager.registerCommand(plugin, new CommandRestart("grestart"));
		pluginManager.registerCommand(plugin, new CommandaddServer("addserver"));
		pluginManager.registerCommand(plugin, new CommanddelServer("delserver"));
		pluginManager.registerCommand(plugin, new CommandMOTD("motd"));
		pluginManager.registerCommand(plugin, new CommandPremium("premium"));
		pluginManager.registerCommand(plugin, new CommandPwChange("pwchange"));
		pluginManager.registerCommand(plugin, new CommandServer("server"));
		pluginManager.registerCommand(plugin, new CommandClearChat("cc"));
		pluginManager.registerCommand(plugin, new CommandGList("glist"));
		pluginManager.registerCommand(plugin, new CommandPvP("pvp"));
		pluginManager.registerCommand(plugin, new CommandGunGame("gungame"));
		pluginManager.registerCommand(plugin, new CommandSky("sky"));
		pluginManager.registerCommand(plugin, new CommandWarZ("warz"));
		pluginManager.registerCommand(plugin, new CommandHub("hub", "l", "tm", "lobby"));
		pluginManager.registerCommand(plugin, new CommandWhereIs("whereis"));
		pluginManager.registerCommand(plugin, new CommandVorbau("vorbau"));
		pluginManager.registerCommand(plugin, new CommandBuild("build"));
		pluginManager.registerCommand(plugin, new CommandTBuild("tbuild"));
		pluginManager.registerCommand(plugin, new CommandPermission("perm"));
		pluginManager.registerCommand(plugin, new CommandClient("client"));
		pluginManager.registerCommand(plugin, new CommandPerformance("performance"));
		pluginManager.registerCommand(plugin, new CommandVote("vote"));
		pluginManager.registerCommand(plugin, new CommandSendServer("sendserver"));
		pluginManager.registerCommand(plugin, new CommandVersus("versus", "vs"));
		pluginManager.registerCommand(plugin, new CommandgPing());
		pluginManager.registerCommand(plugin, new CommandEvent("event"));
		pluginManager.registerCommand(plugin, new CommandBan());
		pluginManager.registerCommand(plugin, new CommandTempBan());
		pluginManager.registerCommand(plugin, new CommandUnban());
		pluginManager.registerCommand(plugin, new CommandBanInfo());
		pluginManager.registerCommand(plugin, new CommandWhitelist());
		pluginManager.registerCommand(plugin, new CommandSkin());
		pluginManager.registerCommand(plugin, new CMD_Report());
		pluginManager.registerCommand(plugin, new CMD_BOOSTER());
		pluginManager.registerCommand(plugin, new CommandRoulett());
		pluginManager.registerCommand(plugin, new CommandNick());
		pluginManager.registerCommand(plugin, new CommandTeamspeak());
		pluginManager.registerCommand(plugin, new CommandResourcepack());
		pluginManager.registerCommand(plugin, new CommandMoney());
		pluginManager.registerCommand(plugin, new CommandConsoleTeamMessage());

		if (configuration.get("gilde.enabled") == null) {
			configuration.set("gilde.enabled", "false");
			try {
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else if (configuration.getBoolean("gilde.enabled")){
			pluginManager.registerCommand(plugin, new CommandGilde());
		}

		pluginManager.registerListener(plugin, InformationManager.getManager());
		pluginManager.registerListener(plugin, ServerManager.getManager());
		pluginManager.registerListener(plugin, RoulettHistory.history);
//		pluginManager.registerListener(plugin, NickHandler.getInstance());
		pluginManager.registerListener(plugin, ChatManager.getInstance());
		pluginManager.registerListener(plugin, new ChatListener());
		pluginManager.registerListener(plugin, new PingListener());
		pluginManager.registerListener(plugin, new PlayerJoinListener());
		pluginManager.registerListener(plugin, new PlayerKickListener());
		pluginManager.registerListener(plugin, new TeamChatListener());
		pluginManager.registerListener(plugin, new ServerListener());
		pluginManager.registerListener(plugin, new SkinListener());
		pluginManager.registerListener(plugin, new InvalidChatListener());
		pluginManager.registerListener(plugin, new PlayerDisconnectListener());
		TimeListener.setInstance(new TimeListener());
		pluginManager.registerListener(plugin, TimeListener.getInstance());
		WarzTexturePackListener.setInstance(new WarzTexturePackListener());
		pluginManager.registerListener(plugin, WarzTexturePackListener.getInstance());

		ActionBar.getInstance().start();

		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutChat.class, new ProtocollId(BigClientVersion.v1_8, 0x02), new ProtocollId(BigClientVersion.v1_9, 0x0F), new ProtocollId(BigClientVersion.v1_10, 0x0F));
		Packet.registerPacket(Protocol.GAME, Direction.TO_SERVER, PacketPlayInKeepAlive.class, new ProtocollId(BigClientVersion.v1_8, 0), new ProtocollId(BigClientVersion.v1_9, 0x1F), new ProtocollId(BigClientVersion.v1_10, 0x1F));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutKeepAlive.class, new ProtocollId(BigClientVersion.v1_8, 0x00), new ProtocollId(BigClientVersion.v1_9, 0x1F), new ProtocollId(BigClientVersion.v1_10, 0x1F));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutSpawnPostition.class, new ProtocollId(BigClientVersion.v1_8, 0x05), new ProtocollId(BigClientVersion.v1_9, 0x43), new ProtocollId(BigClientVersion.v1_10, 0x43));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutMapChunk.class, new ProtocollId(BigClientVersion.v1_8, 0x21), new ProtocollId(BigClientVersion.v1_9, 0x20), new ProtocollId(BigClientVersion.v1_10, 0x20));
		//Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutMapChunkBulk.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x26));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutUpdateHealth.class, new ProtocollId(BigClientVersion.v1_8, 0x06), new ProtocollId(BigClientVersion.v1_9, 0x3E), new ProtocollId(BigClientVersion.v1_10, 0x3E));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutEntityTeleport.class, new ProtocollId(BigClientVersion.v1_8, 24), new ProtocollId(BigClientVersion.v1_9, 0x4A), new ProtocollId(ProtocollVersion.v1_9_2, 0x49), new ProtocollId(BigClientVersion.v1_10, 0x49)); //Change?
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutEntityProperties.class, new ProtocollId(BigClientVersion.v1_8, 0x20), new ProtocollId(BigClientVersion.v1_9, 0x4B), new ProtocollId(ProtocollVersion.v1_9_2, 0x4A), new ProtocollId(ProtocollVersion.v1_9_3, 0x4A), new ProtocollId(ProtocollVersion.v1_9_4, 0x4A), new ProtocollId(BigClientVersion.v1_10, 0x4A)); //Change?
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutUpdateSign.class, new ProtocollId(BigClientVersion.v1_8, 0x33), new ProtocollId(BigClientVersion.v1_9, 0x46), new ProtocollId(BigClientVersion.v1_10, 0x46));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutSetExperience.class, new ProtocollId(BigClientVersion.v1_8, 0x1F), new ProtocollId(BigClientVersion.v1_9, 0x3D), new ProtocollId(BigClientVersion.v1_10, 0x3D));

		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutResourcepack.class, new ProtocollId(BigClientVersion.v1_8, 0x48), new ProtocollId(BigClientVersion.v1_9, 0x32), new ProtocollId(BigClientVersion.v1_10, 0x32));
		Packet.registerPacket(Protocol.GAME, Direction.TO_SERVER, PacketPlayInResourcepackStatus.class, new ProtocollId(BigClientVersion.v1_8, 0x19), new ProtocollId(BigClientVersion.v1_9, 0x16), new ProtocollId(BigClientVersion.v1_10, 0x16));
//		PacketLib.addHandler(NickHandler.getInstance(), 100); //Register before chat log! Use chat handle self
		PacketLib.addHandler(ChatManager.getInstance(), 50);

		if (!WorldFileReader.isWorld(new File(conf.getString("server.afk.world")))) {
			System.out.println("§cCant create AFK server!");
		} else {
			System.out.println("§aLoading AFK world!");
			PacketLib.addHandler(new AfkListener(new ServerConfiguration("§cDu bist AFK", "", "§aDu bist AFK", Arrays.asList("", "", "", "", "§aDu bist AFK!"), Arrays.asList("", ""), WorldFileReader.read(new File(conf.getString("server.afk.world"))), conf.getInt("server.chunksize")))); //Afk Server
			System.out.println("§aAFK-Server loaded!");
		}
		if (!WorldFileReader.isWorld(new File(conf.getString("server.banned.world")))) {
			System.out.println("§cCant create Ban server!");
		} else {
			System.out.println("§aLoading Ban world!");
			PacketLib.addHandler(new BannedServerListener());
			BannedServerManager.setInstance(new BannedServerManager(WorldFileReader.read(new File(conf.getString("server.banned.world"))), conf.getInt("server.chunksize")));
			System.out.println("§aBan-Server loaded!");
		}
		pluginManager.registerListener(plugin, new BanServerMessageListener());

		Main.skins = new SkinCacheManager();
		ActionBarInformation.setInstance(new ActionBarInformation(1000, 5000));
		ActionBarInformation.getInstance().start();

		Main.gildeManager = new GildManager(Main.data.getClient());
		System.out.println("Event hander");
		EventManager emanager = Main.getDatenServer().getClient().getHandle().getEventManager();

		emanager.getEventManager(EventType.BOOSTER_SWITCH).setEventEnabled(true);
		emanager.registerListener(Main.getBoosterManager());

//		emanager.getEventManager(EventType.SERVER_SWITCH).setEventEnabled(true);
//		emanager.getEventManager(EventType.SERVER_SWITCH).setConditionEnables(EventConditions.PLAYERS_WHITELIST, true);
//		emanager.getEventManager(EventType.SERVER_SWITCH).getCondition(EventConditions.PLAYERS_WHITELIST, UUID.class).addValue(Main.getDatenServer().getClient().getPlayerAndLoad("WolverinDEV").getUUID());
//		emanager.registerListener(new EventListener() {
//			@Override
//			public void fireEvent(Event e) {
//				if (e instanceof PlayerServerSwitchEvent) {
//					PlayerServerSwitchEvent ev = (PlayerServerSwitchEvent) e;
//					System.out.println("§aServerswitch: " + ev.getFrom() + ":" + ev.getTo() + ":" + ev.getPlayerId());
//				}
//			}
//		});

		emanager.getEventManager(EventType.TEAMSPEAK_LINK_REQUEST).setEventEnabled(true);
		emanager.registerListener(new TeamspeakListener());
		//PacketLib.addHandler(new TabListener());

		Main.loaded = true;

		try {
			PrefixCommandRegistry.setInstance(new PrefixCommandRegistry());
			Field commandMap = UtilReflection.getField(PluginManager.class, "commandMap");
			HashMap<String, Command> old = (HashMap<String, Command>) commandMap.get(pluginManager);
			CommandHashMap _new = new CommandHashMap();
			commandMap.set(pluginManager, (Map<String, Command>) _new);
			for (Entry<String, Command> cmd : old.entrySet())
				_new.put(cmd.getKey(), cmd.getValue());
		} catch (Exception e) {
			e.printStackTrace();
		}


		BungeeCord.getInstance().getScheduler().schedule(plugin, new Runnable() {
			private int lastAmount = -7;

			@Override
			public void run() {
				int onlineCount = BungeeCord.getInstance().getOnlineCount();
				if (lastAmount == -7) {
					lastAmount = onlineCount;
				}
				int diff = onlineCount - lastAmount;
				if (diff < 0) {
					System.out.println("Spieler online: " + onlineCount + " " + diff);
				} else if (diff > 0) {
					System.out.println("Spieler online: " + onlineCount + " +" + diff);
				} else { //diff==0
					System.out.println("Spieler online: " + onlineCount);
				}
				lastAmount = onlineCount;
			}
		}, 1, 10, TimeUnit.SECONDS);
		BungeeCord.getInstance().getScheduler().schedule(plugin, () -> {
			for (ProxiedPlayer plr : BungeeCord.getInstance().getPlayers()) {
				try {
					LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(plr.getUniqueId());
					if (player == null || plr.getServer().getInfo().getName().toLowerCase().startsWith("loginhub") || plr.getServer().getInfo().getName().equalsIgnoreCase("proxylobby") || player.isPremiumSync()) {
						continue;
					}
					player.getSettings(Setting.LAST_PASSWORD_CHANGED).getAsync((settings, throwable) -> {
						if (throwable != null) {
							throwable.printStackTrace();
							return;
						}
						for (SettingValue statistic : settings) {
							if (statistic.getSetting() == Setting.LAST_PASSWORD_CHANGED && Long.parseLong(statistic.getValue()) + TimeUnit.DAYS.toMillis((long) (30.5 * 6)) < System.currentTimeMillis()) {
								plr.sendMessage(Main.PASSWORD_CHANGE_MESSAGE);
								break;
							}
						}
					});
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}, 1, 15, TimeUnit.MINUTES);
	}
}
