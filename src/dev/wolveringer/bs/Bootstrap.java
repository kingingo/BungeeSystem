package dev.wolveringer.bs;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
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
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutChat;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutEntityProperties;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutEntityTeleport;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutKeepAlive;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutSetExperience;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutSpawnPostition;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutUpdateHealth;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutUpdateSign;
import dev.wolveringer.actionbar.ActionBar;
import dev.wolveringer.afk.AfkListener;
import dev.wolveringer.ban.BanServerMessageListener;
import dev.wolveringer.ban.BannedServerListener;
import dev.wolveringer.ban.BannedServerManager;
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
import dev.wolveringer.bs.commands.CommandNick;
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
import dev.wolveringer.bs.commands.CommandTeamspeak;
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
import dev.wolveringer.chat.ChatManager;
import dev.wolveringer.client.debug.Debugger;
import dev.wolveringer.event.EventListener;
import dev.wolveringer.event.EventManager;
import dev.wolveringer.events.Event;
import dev.wolveringer.events.EventConditions;
import dev.wolveringer.events.EventType;
import dev.wolveringer.events.player.PlayerServerSwitchEvent;
import dev.wolveringer.gilde.GildManager;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nick.NickHandler;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.report.commands.CMD_Report;
import dev.wolveringer.report.info.ActionBarInformation;
import dev.wolveringer.server.ServerConfiguration;
import dev.wolveringer.server.packets.PacketPlayInKeepAlive;
import dev.wolveringer.server.packets.PacketPlayOutMapChunk;
import dev.wolveringer.server.world.WorldFileReader;
import dev.wolveringer.skin.SkinCacheManager;
import dev.wolveringer.slotmachine.RoulettHistory;
import dev.wolveringer.teamspeak.TeamspeakListener;
import dev.wolveringer.thread.ThreadFactory;
import dev.wolveringer.thread.ThreadRunner;
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

	public void onEnable0() {
		AsyncCatcher.disableAll();
		Debugger.setFilter(s -> {
			s = s.toLowerCase();
			if (s.startsWith("readed packet in "))
				return false;
			if (s.startsWith("write packet "))
				return false;
			if (s.startsWith("packet successfull handled ("))
				return false;
			if (s.startsWith("skin data: "))
				return false;
			if (s.startsWith("reciving "))
				return false;
			return true;
		});
		try {
			Class.forName(UtilBungeeCord.class.getName());
		} catch (ClassNotFoundException ex) {
		}
		if (false)
		ThreadFactory.setInstance(new ThreadFactory() {
			@Override
			public ThreadRunner createThread(Runnable run) {
				return new ThreadRunner() {
					ScheduledTask task;
					Thread current;
					
					@Override
					public void stop() {
						if(task != null){
							task.cancel();
							try{
								current.interrupt();
							}catch(Exception e){
								e.printStackTrace();
							}
							if(current.getState() != State.TERMINATED)
								try{
									current.stop();
								}catch(Exception e){
									e.printStackTrace();
								}
						}
						else
							System.err.println("Try to cancel an not started task...");
						task = null;
					}

					@Override
					public void start() {
						if(task != null){
							System.err.println("Try to start a task twice!");
							throw new RuntimeException("Task is alredy runnings");
						}
						task = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
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
						return current != null ? current.toString()+"/RUNNING" : super.toString()+"/STOPPED";
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

		ActionBar.setInstance(new ActionBar());
		ChatManager.setInstance(new ChatManager());
		
		Main.data = new BungeecordDatenClient(Main.getInstance().serverId, new InetSocketAddress(configuration.getString("datenserver.host"), configuration.getInt("datenserver.port")));
		Main.data.setPassword(configuration.getString("datenserver.passwort"));
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
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
						if(Main.isRestarting())
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
		NickHandler.setInstance(new NickHandler());
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
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandHub("hub", "l", "tm", "lobby", "penis", "bigcock"));
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
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandNick());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandTeamspeak());
		//BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandGilde());
		
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), InformationManager.getManager());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), ServerManager.getManager());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), RoulettHistory.history);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), NickHandler.getInstance());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), ChatManager.getInstance());
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
		
		ActionBar.getInstance().start();
		
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutChat.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x02), new Packet.ProtocollId(BigClientVersion.v1_9, 0x0F), new Packet.ProtocollId(BigClientVersion.v1_10, 0x0F));
		Packet.registerPacket(Protocol.GAME, Direction.TO_SERVER, PacketPlayInKeepAlive.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0), new Packet.ProtocollId(BigClientVersion.v1_9, 0x1F), new Packet.ProtocollId(BigClientVersion.v1_10, 0x1F));

		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutKeepAlive.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x00), new Packet.ProtocollId(BigClientVersion.v1_9, 0x1F), new Packet.ProtocollId(BigClientVersion.v1_10, 0x1F));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutSpawnPostition.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x05), new Packet.ProtocollId(BigClientVersion.v1_9, 0x43), new Packet.ProtocollId(BigClientVersion.v1_10, 0x43));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutMapChunk.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x21), new Packet.ProtocollId(BigClientVersion.v1_9, 0x20), new Packet.ProtocollId(BigClientVersion.v1_10, 0x20));
		//Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutMapChunkBulk.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x26));

		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutUpdateHealth.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x06), new Packet.ProtocollId(BigClientVersion.v1_9, 0x3E), new Packet.ProtocollId(BigClientVersion.v1_10, 0x3E));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutEntityTeleport.class, new Packet.ProtocollId(BigClientVersion.v1_8, 24), new Packet.ProtocollId(BigClientVersion.v1_9, 0x4A), new Packet.ProtocollId(ProtocollVersion.v1_9_2, 0x49), new Packet.ProtocollId(BigClientVersion.v1_10, 0x49)); //Change?
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutEntityProperties.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x20), new Packet.ProtocollId(BigClientVersion.v1_9, 0x4B) , new Packet.ProtocollId(ProtocollVersion.v1_9_2, 0x4A), new Packet.ProtocollId(ProtocollVersion.v1_9_3, 0x4A), new Packet.ProtocollId(ProtocollVersion.v1_9_4, 0x4A), new Packet.ProtocollId(BigClientVersion.v1_10, 0x4A)); //Change?
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutUpdateSign.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x33), new Packet.ProtocollId(BigClientVersion.v1_9, 0x46), new Packet.ProtocollId(BigClientVersion.v1_10, 0x46));
		Packet.registerPacket(Protocol.GAME, Direction.TO_CLIENT, PacketPlayOutSetExperience.class, new Packet.ProtocollId(BigClientVersion.v1_8, 0x1F), new Packet.ProtocollId(BigClientVersion.v1_9, 0x3D), new Packet.ProtocollId(BigClientVersion.v1_10, 0x3D));
		PacketLib.addHandler(NickHandler.getInstance(), 100); //Register before chat log! Use chat handle self
		//PacketLib.addHandler(ChatManager.getInstance(), 50);
		
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
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new BanServerMessageListener());

		Main.skins = new SkinCacheManager();
		ActionBarInformation.setInstance(new ActionBarInformation(1000, 5000));
		ActionBarInformation.getInstance().start();

		Main.gildeManager = new GildManager(Main.data.getClient());
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
		
		emanager.getEventManager(EventType.TEAMSPEAK_LINK_REQUEST).setEventEnabled(true);
		emanager.registerListener(new TeamspeakListener());
		//PacketLib.addHandler(new TabListener());

		Main.loaded = true;

		try {
			PrefixCommandRegistry.setInstance(new PrefixCommandRegistry());
			Field commandMap = UtilReflection.getField(PluginManager.class, "commandMap");
			HashMap<String, Command> old = (HashMap<String, Command>) commandMap.get(BungeeCord.getInstance().getPluginManager());
			CommandHashMap _new = new CommandHashMap();
			commandMap.set(BungeeCord.getInstance().getPluginManager(), (Map<String, Command>) _new);
			for (Entry<String, Command> cmd : old.entrySet())
				_new.put(cmd.getKey(), cmd.getValue());
		} catch (Exception e) {
			e.printStackTrace();
		}

		PrefixCommandRegistry.getInstance().registerCommandListener("~", new ConsoleTeamMessageListener());
	}

	public static void main(String[] args) {
		int i = 0x00;
		Integer x = i;
		System.out.println((x == null) + " - " + x.intValue() + " - " + PacketPlayOutKeepAlive.class);
	}
}
