package eu.epicpvp.bungee.system.bs.listener;

import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.PacketLib;
import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.packets.PacketPlayInResourcepackStatus;
import eu.epicpvp.bungee.system.bs.packets.PacketPlayInResourcepackStatus.Action;
import eu.epicpvp.bungee.system.bs.packets.PacketPlayOutResourcepack;
import eu.epicpvp.datenserver.definitions.arrays.CachedArrayList;
import eu.epicpvp.datenserver.definitions.arrays.CachedArrayList.UnloadListener;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.thread.ThreadFactory;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class WarzTexturePackListener implements Listener{
	static {
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "warz.resourcepack.warz.accepted", "§7> §aThanks for accepting the resourcepack. The download has started.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "warz.resourcepack.warz.download_failed", "§7> §aThe download failed, please try it again.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "warz.resourcepack.warz.successfully_loaded", "§7> §aThe Texture-Pack is completly loaded. Thanks for using it.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "warz.resourcepack.warz.declined", "§7> §6You denied our original WarZ-Texturepack. If you change your mind then you can use \"/rp\" to load the Texture-Pack.");

		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "warz.resourcepack.default.accepted", "§7> §aYou accepted the Reset-Texturepack.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "warz.resourcepack.default.download_failed", "§7> §6The download of the Reset-Texturepack failed. Please try it again with \"\".");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "warz.resourcepack.default.successfully_loaded", "§7> §aThe Reset was successful.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "warz.resourcepack.default.declined", "§7> §cYou denied the Reset-Texturepack.");
	}

	@Getter
	@Setter
	private static WarzTexturePackListener instance;

	public static final String DEFAULT_URL = "https://github.com/Phoenix616/BungeeResourcepacks/blob/master/Empty.zip?raw=true";
	public static final String DEFAULT_HASH = "3934d29cc6f7c271afdc477f6dd6b2ea90493825";
	public static final String WARZ_URL = "http://rs.connect-handler.net/WarZ.zip";
	public static final String WARZ_HASH = "78777520a231a4698b76b8adb12b0090718003d4";
	public CachedArrayList<Player> textureUsing = new CachedArrayList<>(10, TimeUnit.MINUTES);

	public WarzTexturePackListener() {
		PacketLib.addHandler(new PacketHandler<PacketPlayInResourcepackStatus>() {
			@Override
			public void handle(PacketHandleEvent<PacketPlayInResourcepackStatus> e) {
				System.out.println("Having packet: "+e.getPacket());
				if (e.getPacket().getHash() == null) { //TODO track which pack was sent
					return;
				}
				if(!e.getPacket().getHash().equalsIgnoreCase(DEFAULT_HASH)){
					if(e.getPacket().getAction() == Action.SUCCESSFULLY_LOADED)
						textureUsing.add(e.getPlayer());
					e.getPlayer().sendMessage(Main.getTranslationManager().translate("warz.resourcepack.warz."+e.getPacket().getAction().name().toLowerCase(), e.getPlayer()));;
				}
				else{
					if(e.getPacket().getAction() == Action.SUCCESSFULLY_LOADED)
						textureUsing.remove(e.getPlayer());
					e.getPlayer().sendMessage(Main.getTranslationManager().translate("warz.resourcepack.default."+e.getPacket().getAction().name().toLowerCase(), e.getPlayer()));
				}
			}
		});
		textureUsing.addUnloadListener(new UnloadListener<Player>() {
			@Override
			public boolean canUnload(Player element) {
				return !element.isConnected();
			}
		});
	}

	@EventHandler
	public void a(ServerSwitchEvent e){
		ThreadFactory.getFactory().createThread(()->{
			try {
				Thread.sleep(500);
			} catch (Exception e1) {
			}
			if(e.getPlayer().getServer().getInfo().getName().equalsIgnoreCase("warz")){
				((Player)e.getPlayer()).sendPacket(new PacketPlayOutResourcepack(WARZ_URL, WARZ_HASH)); //Hash of WarZ.zip xD
			}
			else if(textureUsing.contains(e.getPlayer())){
				((Player)e.getPlayer()).sendPacket(new PacketPlayOutResourcepack(DEFAULT_URL, DEFAULT_HASH));
			}
		}).start();
	}

	@EventHandler
	public void a(PlayerDisconnectEvent e){
		textureUsing.remove(e.getPlayer());
	}
}
