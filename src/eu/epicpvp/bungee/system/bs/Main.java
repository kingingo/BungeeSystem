package eu.epicpvp.bungee.system.bs;

import eu.epicpvp.bungee.system.booster.BoosterManager;
import eu.epicpvp.bungee.system.bs.client.BungeecordDatenClient;
import eu.epicpvp.bungee.system.skin.SkinCacheManager;
import eu.epicpvp.bungee.system.translation.TranslationHandler;
import eu.epicpvp.datenclient.gilde.GildManager;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.plugin.Plugin;

public class Main extends Plugin {

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

	public static Main getInstance() {
		return main;
	}

	protected String serverId;

	//@formatter:off
	public static final net.md_5.bungee.api.chat.BaseComponent[] PASSWORD_CHANGE_MESSAGE = new ComponentBuilder("")
			.append("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n\n")
				.color(ChatColor.GRAY).bold(true)
			.append("      Bitte ändere dein Passwort aus Sicherheitsgründen alle 6 Monate.\n")
				.color(ChatColor.RED).bold(false)
			.append("              Mit diesem Befehl kannst du dies tun:\n        ")
				.color(ChatColor.RED)
			.append("/pwchange <altes Passwort> <neues Passwort>")
				.color(ChatColor.YELLOW).underlined(true)
				.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klicke, um den Befehl zu kopieren.").color(ChatColor.RED).create()))
				.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/pwchange "))
			.append("\n\n Die Sicherheit deines Accounts liegt uns am Herzen.\n" + " Diese Nachricht verschwindet, sobald du dein Passwort\n geändert hast.\n", ComponentBuilder.FormatRetention.NONE)
				.color(ChatColor.GRAY)
			.append("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
				.color(ChatColor.GRAY).bold(true)
			.create();
	//@formatter:on

	@Override
	public void onEnable() {
		main = this;
		try {
			BungeeCord.getInstance().getPluginManager().registerListener(this, new PreLoadedLoginListener());
			new Bootstrap(Main.getInstance().getDataFolder()).onEnable(); //Directly all loaded
		} catch (Throwable ex) {
			System.err.println("§cHaving an exception on startup!");
			ex.printStackTrace();
			UtilBungeeCord.restart();
		}
	}

	@Override
	public void onDisable() {
		restarting = true;
		if (data != null && data.getClient() != null && data.getClient().getHandle() != null)
			data.getClient().getHandle().disconnect("Bungeecord stopped");
	}

	public static BungeecordDatenClient getDatenServer() {
		return data;
	}

	public String getServerId() {
		return serverId;
	}
}
