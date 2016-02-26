package dev.wolveringer.bs;

import dev.wolveringer.bs.client.BungeecordDatenClient;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.mysql.MySQL;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin{
	private static Main main;
	private static BungeecordDatenClient data;
	public static Main getInstance(){
		return main;
	}
	
	private String serverId;
	
	@Override
	public void onEnable() {
		main = this;
		
		
		MySQL.setInstance(new MySQL("148.251.143.2", "3306", "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT"));
		LoginManager.setManager(new LoginManager());
		Language.init();
		PermissionManager.setManager(new PermissionManager());
		ServerManager.setManager(new ServerManager());
	}
	public static BungeecordDatenClient getDatenServer() {
		return data;
	}
	public String getServerId() {
		return serverId;
	}
}
//TODO
//Join set right UUID
//Language Update (When Change)
//get Message from Database
//Permission System!
//Join add all Permissions