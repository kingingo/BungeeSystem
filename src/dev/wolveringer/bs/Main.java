package dev.wolveringer.bs;

import dev.wolveringer.booster.BoosterManager;
import dev.wolveringer.bs.client.BungeecordDatenClient;
import dev.wolveringer.gilde.GildManager;
import dev.wolveringer.report.info.ActionBarInformation;
import dev.wolveringer.skin.SkinCacheManager;
import lombok.Getter;
import lombok.Setter;
import me.kingingo.kBungeeCord.Language.TranslationHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

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
}