package dev.wolveringer.bs;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import dev.wolveringer.BungeeUtil.AsyncCatcher;
import dev.wolveringer.booster.BoosterManager;
import dev.wolveringer.booster.CMD_BOOSTER;
import dev.wolveringer.bs.client.BungeecordDatenClient;
import dev.wolveringer.bs.commands.CommandBan;
import dev.wolveringer.bs.commands.CommandBanInfo;
import dev.wolveringer.bs.commands.CommandBroad;
import dev.wolveringer.bs.commands.CommandBuild;
import dev.wolveringer.bs.commands.CommandClearChat;
import dev.wolveringer.bs.commands.CommandClient;
import dev.wolveringer.bs.commands.CommandEvent;
import dev.wolveringer.bs.commands.CommandGList;
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
import dev.wolveringer.bs.information.InformationManager;
import dev.wolveringer.bs.listener.ChatListener;
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
import dev.wolveringer.report.commands.CMD_Report;
import dev.wolveringer.report.info.ActionBarInformation;
import dev.wolveringer.skin.SkinCacheManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kingingo.kBungeeCord.Language.TranslationHandler;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

@AllArgsConstructor
public class Bootstrap {
	@Getter
	File dataFolder;

	public void onEnable() {
		onEnable0();
	}

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
								try{
									run.run();
								}catch(Exception e){
									System.err.println("Having error while excecuting Runable "+run.getClass()+":");
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

		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				while (true) {
					while (!Main.data.isActive()) {
						System.out.println("§aTry to connect to dataserver");
						try {
							Main.data.start(Main.getInstance().datenPassword = configuration.getString("datenserver.passwort"));
							for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers()){
								try{
									if(PermissionManager.getManager().hasPermission(p, "epicpvp.bc.dataserver"))
										p.sendMessage("§aDatenserver connected!");
								}catch(Exception ex){
								}
							}
						} catch (Exception e) {
							System.out.println("§cCant connect to DatenServer [" + ((InetSocketAddress) Main.data.getAddress()).getHostName() + ":" + ((InetSocketAddress) Main.data.getAddress()).getPort() + "]. Reson: "+e.getMessage()+" . Try again in 5 seconds.");
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers()){
								try{
									if(PermissionManager.getManager().hasPermission(p, "epicpvp.bc.dataserver"))
										p.sendMessage("§cDatenserver offline!");
								}catch(Exception ex){
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
		while (Main.data.getClient() == null || !Main.data.getClient().getHandle().isHandschakeCompleded()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
		}

		Main.translationManager = new TranslationHandler(Main.getDatenServer().getClient().getTranslationManager());
		
		System.out.println("Checking for translation updates:");
		Main.getTranslationManager().updateTranslations();
		System.out.println("All translations are now up to date!");
		
		LoginManager.setManager(new LoginManager());
		InformationManager.setManager(new InformationManager());
		PermissionManager.setManager(new PermissionManager()); //TODO load
		ServerManager.setManager(new ServerManager());
		ServerManager.getManager().loadServers();
		MessageManager.start();

		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			public void run() {
				PermissionManager.getManager().loadGroups();
			}
		});
		Main.boosterManager = new BoosterManager();
		Main.getBoosterManager().init();
		
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
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandSky("sky"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandHub("hub", "l", "tm", "lobby", "penis"));
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
		
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), InformationManager.getManager());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), ServerManager.getManager());
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
		
		Main.skins = new SkinCacheManager();
		Main.info = new ActionBarInformation(1000, 5000);
		Main.info.start();
		
		System.out.println("Event hander");
		EventManager emanager = Main.getDatenServer().getClient().getHandle().getEventManager();
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
		
		Main.loaded = true;
	}
}
