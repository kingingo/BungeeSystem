package dev.wolveringer.bs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import dev.wolveringer.BungeeUtil.AsyncCatcher;
import dev.wolveringer.BungeeUtil.ClientVersion.BigClientVersion;
import dev.wolveringer.BungeeUtil.ClientVersion.ProtocollVersion;
import dev.wolveringer.BungeeUtil.PacketLib;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutEntityProperties;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutEntityTeleport;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutKeepAlive;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutSetExperience;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutSpawnPostition;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutUpdateHealth;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutUpdateSign;
import dev.wolveringer.afk.AfkListener;
import dev.wolveringer.booster.BoosterManager;
import dev.wolveringer.booster.CMD_BOOSTER;
import dev.wolveringer.bs.client.BungeecordDatenClient;
import dev.wolveringer.bs.commands.CommandBan;
import dev.wolveringer.bs.commands.CommandBanInfo;
import dev.wolveringer.bs.commands.CommandBroad;
import dev.wolveringer.bs.commands.CommandBuild;
import dev.wolveringer.bs.commands.CommandClearChat;
import dev.wolveringer.bs.commands.CommandClient;
import dev.wolveringer.bs.commands.CommandCreative;
import dev.wolveringer.bs.commands.CommandEvent;
import dev.wolveringer.bs.commands.CommandGList;
import dev.wolveringer.bs.commands.CommandGunGame;
import dev.wolveringer.bs.commands.CommandHub;
import dev.wolveringer.bs.commands.CommandKicken;
import dev.wolveringer.bs.commands.CommandMOTD;
import dev.wolveringer.bs.commands.CommandNews;
import dev.wolveringer.bs.commands.CommandPerformance;
import dev.wolveringer.bs.commands.CommandPermission;
import dev.wolveringer.bs.commands.CommandPremium;
import dev.wolveringer.bs.commands.CommandPvP;
import dev.wolveringer.bs.commands.CommandPwChange;
import dev.wolveringer.bs.commands.CommandRestart;
import dev.wolveringer.bs.commands.CommandRoulett;
import dev.wolveringer.bs.commands.CommandSendServer;
import dev.wolveringer.bs.commands.CommandServer;
import dev.wolveringer.bs.commands.CommandSkin;
import dev.wolveringer.bs.commands.CommandSky;
import dev.wolveringer.bs.commands.CommandTBuild;
import dev.wolveringer.bs.commands.CommandTempBan;
import dev.wolveringer.bs.commands.CommandUnban;
import dev.wolveringer.bs.commands.CommandVersus;
import dev.wolveringer.bs.commands.CommandVorbau;
import dev.wolveringer.bs.commands.CommandVote;
import dev.wolveringer.bs.commands.CommandWhereIs;
import dev.wolveringer.bs.commands.CommandWhitelist;
import dev.wolveringer.bs.commands.CommandaddServer;
import dev.wolveringer.bs.commands.CommanddelServer;
import dev.wolveringer.bs.commands.CommandgPing;
import dev.wolveringer.bs.consolencommand.CommandHashMap;
import dev.wolveringer.bs.consolencommand.PrefixCommandRegistry;
import dev.wolveringer.bs.information.InformationManager;
import dev.wolveringer.bs.listener.ChatListener;
import dev.wolveringer.bs.listener.ConsoleTeamMessageListener;
import dev.wolveringer.bs.listener.InvalidChatListener;
import dev.wolveringer.bs.listener.PingListener;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.bs.listener.PlayerKickListener;
import dev.wolveringer.bs.listener.ServerListener;
import dev.wolveringer.bs.listener.SkinListener;
import dev.wolveringer.bs.listener.TeamChatListener;
import dev.wolveringer.bs.listener.TimeListener;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.login.PlayerDisconnectListener;
import dev.wolveringer.bs.message.MessageManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.client.threadfactory.ThreadRunner;
import dev.wolveringer.event.EventListener;
import dev.wolveringer.event.EventManager;
import dev.wolveringer.events.Event;
import dev.wolveringer.events.EventConditions;
import dev.wolveringer.events.EventType;
import dev.wolveringer.events.player.PlayerServerSwitchEvent;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.report.commands.CMD_Report;
import dev.wolveringer.report.info.ActionBarInformation;
import dev.wolveringer.server.ServerConfiguration;
import dev.wolveringer.server.packets.PacketPlayInKeepAlive;
import dev.wolveringer.server.packets.PacketPlayOutMapChunk;
import dev.wolveringer.server.packets.PacketPlayOutMapChunkBulk;
import dev.wolveringer.server.world.WorldFileReader;
import dev.wolveringer.skin.SkinCacheManager;
import dev.wolveringer.slotmachine.RoulettHistory;
import dev.wolveringer.util.UtilReflection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kingingo.kBungeeCord.Language.TranslationHandler;
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

	public void onEnable() {
		onEnable0();
	}

	@SuppressWarnings("serial")
	public void onEnable0() {
		AsyncCatcher.disableAll();
		UtilBungeeCord.class.getName(); //Keep loaded in memory
		ThreadFactory.setFactory(new ThreadFactory() {
			@Override
			public ThreadRunner createThread(Runnable run) {
				return new ThreadRunner() {
					ScheduledTask task;

					@Override
					public void stop() {
						task.cancel();
					}

					@Override
					public void start() {
						task = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
							@Override
							public void run() {
								try {
									run.run();
								} catch (Exception e) {
									System.err.println("Having error while excecuting Runable " + run.getClass() + ":");
									e.printStackTrace();
								}
							}
						});
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
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(conf, new File(getDataFolder(), "config.yml"));
			} else
				conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			MySQL.setInstance(new MySQL(conf.getString("mysql.host"), conf.getInt("mysql.port") + "", conf.getString("mysql.db"), conf.getString("mysql.user"), conf.getString("mysql.passwort")));
			MySQL.getInstance().connect();
			Main.getInstance().serverId = conf.getString("serverId");
			if (!MySQL.getInstance().isConnected()) {
				BungeeCord.getInstance().getConsole().sendMessage("§cCant connect to MySQL. Restart....");
				UtilBungeeCord.restart();
				return;
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		final Configuration configuration = conf;

		Main.data = new BungeecordDatenClient(Main.getInstance().serverId, new InetSocketAddress(configuration.getString("datenserver.host"), configuration.getInt("datenserver.port")));
		Main.data.setPassword(configuration.getString("datenserver.passwort"));
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				while (true) {
					while (!Main.data.isActive()) {
						if(Main.data.isConnecting()){
							System.out.println("§6Connecting to datenserver....");
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
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
		RoulettHistory.history = new RoulettHistory();

		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			public void run() {
				PermissionManager.getManager().loadGroups();
			}
		});
		Main.boosterManager = new BoosterManager();
		Main.getBoosterManager().init();

		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandCreative("creative"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandNews("news"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandBroad("broad"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandKicken("kicken"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandRestart("grestart"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandaddServer("addserver"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommanddelServer("delserver"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandMOTD("motd"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandPremium("premium"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandPwChange("pwchange"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandServer("server"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandClearChat("cc"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandGList("glist"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandPvP("pvp"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandGunGame("gungame"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandSky("sky"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandHub("hub", "l", "tm", "lobby"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandWhereIs("whereis"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandVorbau("vorbau"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandBuild("build"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandTBuild("tbuild"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandPermission("perm"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandClient("client"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandPerformance("performance"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandVote("vote"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandSendServer("sendserver"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandVersus("versus", "vs"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandgPing());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandEvent("event"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandBan());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandTempBan());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandUnban());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandBanInfo());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandWhitelist());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandSkin());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CMD_Report());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CMD_BOOSTER());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandRoulett());

		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), InformationManager.getManager());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), ServerManager.getManager());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), RoulettHistory.history);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new ChatListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new PingListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new PlayerJoinListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new PlayerKickListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new TeamChatListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new ServerListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new SkinListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new InvalidChatListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new PlayerDisconnectListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new TimeListener());

		Packet.registerPacket(Protocol.GAME, Direction.TO_SERVER, PacketPlayInKeepAlive.class,new Packet.ProtocollId(BigClientVersion.v1_8, 0),new Packet.ProtocollId(BigClientVersion.v1_9, 0x1F));
		
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutKeepAlive.class,new Packet.ProtocollId(BigClientVersion.v1_8, 0x00),new Packet.ProtocollId(BigClientVersion.v1_9, 0x1F));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, 0x05,0x43, PacketPlayOutSpawnPostition.class);
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, 0x21,0x20, PacketPlayOutMapChunk.class);
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, 0x26,null, PacketPlayOutMapChunkBulk.class);

		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, 0x06,0x3E, PacketPlayOutUpdateHealth.class);
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, 24,0x4A, PacketPlayOutEntityTeleport.class);
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, 0x20,0x4B, PacketPlayOutEntityProperties.class); 
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, 0x33,0x46, PacketPlayOutUpdateSign.class); 
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, 0x1F,0x3D, PacketPlayOutSetExperience.class); 
		
		System.out.println(Packet.getPacketId(ProtocollVersion.v1_8, PacketPlayOutKeepAlive.class));
		
		System.out.println("Packets loaded");
		
		if(!WorldFileReader.isWorld(new File(conf.getString("server.afk.world")))){
			System.out.println("§cCant create AFK server!");
		}
		else
		{
			System.out.println("§aLoading world!");
			PacketLib.addHandler(new AfkListener(new ServerConfiguration("§cDu bist AFK", "", "§aDu bist AFK", Arrays.asList("","","","","§aDU bist AFK!"), Arrays.asList("",""), WorldFileReader.read(new File(conf.getString("server.afk.world"))), conf.getInt("server.chunksize")))); //Afk Server
			System.out.println("§aAFK-Server loaded!");
		}
		
		Main.skins = new SkinCacheManager();
		Main.info = new ActionBarInformation(1000, 5000);
		Main.info.start();

		System.out.println("Event hander");
		EventManager emanager = Main.getDatenServer().getClient().getHandle().getEventManager();

		emanager.getEventManager(EventType.BOOSTER_SWITCH).setEventEnabled(true);
		emanager.registerListener(Main.getBoosterManager());

		emanager.getEventManager(EventType.SERVER_SWITCH).setEventEnabled(true);
		emanager.getEventManager(EventType.SERVER_SWITCH).setConditionEnables(EventConditions.PLAYERS_WHITELIST, true);
		emanager.getEventManager(EventType.SERVER_SWITCH).getCondition(EventConditions.PLAYERS_WHITELIST).addValue(Main.getDatenServer().getClient().getPlayerAndLoad("WolverinDEV").getUUID());
		emanager.registerListener(new EventListener() {
			@Override
			public void fireEvent(Event e) {
				if (e instanceof PlayerServerSwitchEvent) {
					PlayerServerSwitchEvent ev = (PlayerServerSwitchEvent) e;
					System.out.println("§aServerswitch: " + ev.getFrom() + ":" + ev.getTo() + ":" + ev.getPlayerId());
				}
			}
		});

		//PacketLib.addHandler(new TabListener());

		Main.loaded = true;

		try {
			PrefixCommandRegistry.setInstance(new PrefixCommandRegistry());
			Field commandMap = UtilReflection.getField(PluginManager.class, "commandMap");
			HashMap<String, Command> old = (HashMap<String, Command>) commandMap.get(BungeeCord.getInstance().getPluginManager());
			CommandHashMap _new = new CommandHashMap();
			commandMap.set(BungeeCord.getInstance().getPluginManager(), (Map<String, Command>) _new);
			for(Entry<String, Command> cmd : old.entrySet())
				_new.put(cmd.getKey(), cmd.getValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PrefixCommandRegistry.getInstance().registerCommandListener("~", new ConsoleTeamMessageListener());
	}
	public static void main(String[] args) {
		int i = 0x00;
		Integer x = i;
		System.out.println((x == null)+" - "+x.intValue()+" - "+PacketPlayOutKeepAlive.class);
	}
}
