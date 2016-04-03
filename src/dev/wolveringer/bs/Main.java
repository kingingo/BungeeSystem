package dev.wolveringer.bs;

import dev.wolveringer.bs.client.BungeecordDatenClient;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

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
				plugin = null;
				new Bootstrap(Main.getInstance().getDataFolder()).onEnable();
			}
		});
	}
	
	@Override
	public void onDisable() {
		data.getClient().getHandle().disconnect("Bungeecord stopped");
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