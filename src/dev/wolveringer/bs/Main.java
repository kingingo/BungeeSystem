package dev.wolveringer.bs;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import dev.wolveringer.bs.client.BungeecordDatenClient;
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
import dev.wolveringer.bs.commands.CommandSky;
import dev.wolveringer.bs.commands.CommandTBuild;
import dev.wolveringer.bs.commands.CommandVersus;
import dev.wolveringer.bs.commands.CommandVorbau;
import dev.wolveringer.bs.commands.CommandVote;
import dev.wolveringer.bs.commands.CommandWhereIs;
import dev.wolveringer.bs.commands.CommandaddServer;
import dev.wolveringer.bs.commands.CommanddelServer;
import dev.wolveringer.bs.commands.CommandgPing;
import dev.wolveringer.bs.information.InformationManager;
import dev.wolveringer.bs.listener.MuteListener;
import dev.wolveringer.bs.listener.PingListener;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.bs.listener.PlayerKickListener;
import dev.wolveringer.bs.listener.ServerListener;
import dev.wolveringer.bs.listener.TeamChatListener;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.message.MessageManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.client.threadfactory.ThreadRunner;
import dev.wolveringer.mysql.MySQL;


import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Main extends Plugin{
	protected static Main main;
	protected static BungeecordDatenClient data;
	public static Main getInstance(){
		return main;
	}
	
	protected String datenPassword;
	
	protected String serverId;
	
	@Override
	public void onEnable() {
		main = this;
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				Plugin plugin;
				while ((plugin = BungeeCord.getInstance().getPluginManager().getPlugin("DatenClient")) == null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				new Bootstrap(Main.getInstance().getDataFolder()).onEnable();
			}
		});
	}
	
	@Override
	public void onDisable() {
		data.getClient().getHandle().disconnect("stoping server");
	}
	
	public static BungeecordDatenClient getDatenServer() {
		return data;
	}
	public String getServerId() {
		return serverId;
	}
	public String getDatenPassword() {
		return datenPassword;
	}
}
//TODO
//Join set right UUID Done
//Language Update (When Change) Done
//get Message from Database Done
//Permission System! Done
//Join add all Permissions Done