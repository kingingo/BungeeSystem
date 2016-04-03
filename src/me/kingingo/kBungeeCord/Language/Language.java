package me.kingingo.kBungeeCord.Language;

import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LanguageType;
import lombok.Getter;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Language {
	
	private static HashMap<LanguageType, Translation> translations = new HashMap<>();
	@Getter
	private static HashMap<UUID, LanguageType> languages;

	public static void init() {
		languages = new HashMap<>();
		translations.put(LanguageType.ENGLISH, new TranslationEnglish());
		translations.put(LanguageType.GERMAN, new TranslationEnglish());
	}

	public static void updateLanguage(ProxiedPlayer player, LanguageType type) {
		if(player == null)
			return;
		languages.remove(player.getUniqueId());
		languages.put(player.getUniqueId(), type);
	}

	public static LanguageType getLanguage(ProxiedPlayer player) {
		return getLanguage(player.getPendingConnection());
	}

	public static void sendText(ProxiedPlayer player, String name, Object... input) {
		player.sendMessage(translations.get(getLanguage(player)).translate(name, input));
	}

	public static String getText(ProxiedPlayer player, String name,Object...input) {
		return getText(player.getPendingConnection(), name, input);
	}
	
	public static String getText(LanguageType lang, String name,Object...input) {
		if (!translations.containsKey(lang)) {
			return "Language not found!";
		}
		return translations.get(lang).translate(name,input);
	}

	public static String getText(PendingConnection connection, String name, Object... input) {
		return getText(getLanguage(connection), name, input);
	}
	
	public static LanguageType getLanguage(PendingConnection player) {
		if (player == null) {
			System.out.println("[Language] Spieler == null");
			return LanguageType.ENGLISH;
		} else if (!languages.containsKey(player.getUniqueId())) {
			LanguageType lang = Main.getDatenServer().getClient().getPlayer(player.getUniqueId()).getLanguageSync();
			languages.put(player.getUniqueId(), lang);
			return lang;
		} else if (!translations.containsKey(languages.get(player.getUniqueId()))) {
			System.out.println("[Language] Die Sprache " + languages.get(player.getUniqueId()).getDef() + " wurde nicht gefunden.");
		}

		return languages.get(player.getUniqueId());
	}
}
