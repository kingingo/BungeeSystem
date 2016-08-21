package dev.wolveringer.bs;

import java.util.concurrent.TimeUnit;

import dev.wolveringer.booster.BoosterManager;
import dev.wolveringer.bs.client.BungeecordDatenClient;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.gamestats.Statistic;
import dev.wolveringer.gilde.GildManager;
import dev.wolveringer.skin.SkinCacheManager;
import lombok.Getter;
import lombok.Setter;
import me.kingingo.kBungeeCord.Language.TranslationHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
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

	private static final net.md_5.bungee.api.chat.BaseComponent[] PASSWORD_CHANGE_MESSAGE = new ComponentBuilder("")
			.append("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n\n").color(ChatColor.GRAY).bold(true)
			.append("      Bitte ändere dein Passwort aus Sicherheitsgründen.\n").color(ChatColor.RED).bold(false)
			.append("              Mit diesem Befehl kannst du dies tun:\n        ").color(ChatColor.RED)
			.append("/pwchange <altes Passwort> <neues Passwort>").color(ChatColor.YELLOW).underlined(true)
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klicke, um den Befehl zu kopieren.").color(ChatColor.RED).create()))
			.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pwchange "))
			.append("\n\n Die Sicherheit deines Accounts liegt uns am Herzen.\n" +
					" Diese Nachricht verschwindet, sobald du dein Passwort\n geändert hast.\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
			.append("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").color(ChatColor.GRAY).bold(true)
			.create();

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
					System.out.println("Spieler online: " + onlineCount);
				}
				lastAmount = onlineCount;
			}
		}, 1, 10, TimeUnit.SECONDS);
		getProxy().getPluginManager().registerListener(this, new MyListener());
		getProxy().getScheduler().schedule(this, () -> {
			for (ProxiedPlayer plr : getProxy().getPlayers()) {
				try {
					LoadedPlayer player = getDatenServer().getClient().getPlayer(plr.getUniqueId());
					if (player == null || plr.getServer().getInfo().getName().toLowerCase().startsWith("loginhub") || plr.getServer().getInfo().getName().equalsIgnoreCase("proxylobby") || player.isPremiumSync()) {
						continue;
					}
					player.getStats(GameType.WARZ).getAsync((statistics, throwable) -> {
						if (throwable != null) {
							throwable.printStackTrace();
							return;
						}
						for (Statistic statistic : statistics) {
							if (statistic.getStatsKey() == StatsKey.KILLS && statistic.asInt() == 0) {
								plr.sendMessage(PASSWORD_CHANGE_MESSAGE);
								break;
							}
						}
					});
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}, 1, 15, TimeUnit.MINUTES);
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
		public void onChat(ChatEvent event) {
			if (event.getMessage().equalsIgnoreCase("test_pwcmsg") && event.getSender() instanceof ProxiedPlayer && ((ProxiedPlayer) event.getSender()).getName().equals("Janmm14")) {
				((ProxiedPlayer) event.getSender()).sendMessage(PASSWORD_CHANGE_MESSAGE);
				event.setCancelled(true);
			}
		}
	}
}
