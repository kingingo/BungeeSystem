package dev.wolveringer.bs;

import dev.wolveringer.booster.BoosterManager;
import dev.wolveringer.bs.client.BungeecordDatenClient;
import dev.wolveringer.report.info.ActionBarInformation;
import dev.wolveringer.skin.SkinCacheManager;
import lombok.Getter;
import me.kingingo.kBungeeCord.Language.TranslationHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin{
	public static boolean restart = false;
	public static boolean loaded = false;
	@Getter
	protected static TranslationHandler translationManager;
	protected static Main main;
	protected static BungeecordDatenClient data;
	
	public static SkinCacheManager getSkinManager() {
		return skins;
	}
	protected static SkinCacheManager skins;
	protected static ActionBarInformation info;
	@Getter
	protected static BoosterManager boosterManager;
	public static Main getInstance(){
		return main;
	}
	
	protected String datenPassword;
	
	protected String serverId;
	
	@Override
	public void onEnable() {
		main = this;
		BungeeCord.getInstance().getPluginManager().registerListener(this, new PreLoadedLoginListener());
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				Plugin plugin;
				Class<?> clazz = null;
				while ((plugin = BungeeCord.getInstance().getPluginManager().getPlugin("DatenClient")) == null || clazz == null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try{
						clazz = Class.forName("dev.wolveringer.client.LoadedPlayer");
					}catch(Exception e){};
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