package dev.wolveringer.bs;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import dev.wolveringer.bs.client.BungeecordDatenClient;
import dev.wolveringer.bs.commands.CommandBan;
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
import dev.wolveringer.bs.commands.CommandaddServer;
import dev.wolveringer.bs.commands.CommanddelServer;
import dev.wolveringer.bs.commands.CommandgPing;
import dev.wolveringer.bs.information.InformationManager;
import dev.wolveringer.bs.listener.ChatListener;
import dev.wolveringer.bs.listener.PingListener;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.bs.listener.PlayerKickListener;
import dev.wolveringer.bs.listener.ServerListener;
import dev.wolveringer.bs.listener.SkinListener;
import dev.wolveringer.bs.listener.TeamChatListener;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.message.MessageManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.client.threadfactory.ThreadRunner;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

@AllArgsConstructor
public class Bootstrap {
	@Getter
	File dataFolder;
	public void onEnable(){
		onEnable0();
	}
	public void onEnable0(){
		ThreadFactory.setFactory(new ThreadFactory(){
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
						task = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), run);
					}
				};
			}
		});
		Configuration configuration = null;
		try {
			if(!new File(getDataFolder(), "config.yml").exists()){
				getDataFolder().mkdirs();
				new File(getDataFolder(), "config.yml").createNewFile();
				configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
				configuration.set("mysql.host", "localhost");
				configuration.set("mysql.port", 3306);
				configuration.set("mysql.db", "none");
				configuration.set("mysql.user", "root");
				configuration.set("mysql.passwort", "underknown");
				configuration.set("serverId", "underknown");
				configuration.set("datenserver.host", "localhost");
				configuration.set("datenserver.port", 1111);
				configuration.set("datenserver.passwort", "HelloWorld");
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));
			}
			else
				configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			MySQL.setInstance(new MySQL(configuration.getString("mysql.host"), configuration.getInt("mysql.port")+"", configuration.getString("mysql.db"), configuration.getString("mysql.user"), configuration.getString("mysql.passwort")));
			MySQL.getInstance().connect();
			Main.getInstance().serverId = configuration.getString("serverId");
			if(!MySQL.getInstance().isConnected()){
				BungeeCord.getInstance().getConsole().sendMessage("§cCant connect to MySQL. Restart....");
				UtilBungeeCord.restart();
				return;
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		
		
		Main.data = new BungeecordDatenClient(Main.getInstance().serverId, new InetSocketAddress(configuration.getString("datenserver.host"),configuration.getInt("datenserver.port")));
		while (!Main.data.isActive()) {
			System.out.println("Try to connect to dataserver");
			try{
				Main.data.start(Main.getInstance().datenPassword = configuration.getString("datenserver.passwort"));
			}catch(Exception e){
				if(e.getMessage().equalsIgnoreCase("Connection refused")){
					System.out.println("Cant connect to DatenServer ["+((InetSocketAddress)Main.data.getAddress()).getHostName()+":"+((InetSocketAddress)Main.data.getAddress()).getPort()+"]. Try again in 5 seconds.");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
				e.printStackTrace();
			}
			System.out.println("Successful connected");
		};
		
		LoginManager.setManager(new LoginManager());
		Language.init();
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
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandHub("hub","l","tm","lobby","penis"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandWhereIs("whereis"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandVorbau("vorbau"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandBuild("build"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandTBuild("tbuild"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandPermission("perm"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandClient("client"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandPerformance("performance"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandVote("vote"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandSendServer("sendserver"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandVersus("versus","vs"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandgPing());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandEvent("event"));
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandBan());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandTempBan());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandUnban());
		BungeeCord.getInstance().getPluginManager().registerCommand(Main.getInstance(), new CommandSkin());
		
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new ChatListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new PingListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new PlayerJoinListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new PlayerKickListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new TeamChatListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new ServerListener());
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), new SkinListener());
	}
}
