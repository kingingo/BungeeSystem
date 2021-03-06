package eu.epicpvp.bungee.system.translation;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.translation.TranslationManager;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TranslationHandler {
	private TranslationManager manager;

	public TranslationHandler(TranslationManager manager) {
		this.manager = manager;
	}

	public void updateTranslations() {
		manager.updateTranslations();
	}

	public String translate(String key, Object... args) {
		return manager.translate(key, args);
	}

	public String translate(String key, LoadedPlayer player, Object... args) {
		return manager.translate(key, player, args);
	}

	public String translate(String key, ProxiedPlayer player, Object... args) {
		return manager.translate(key, Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()), args);
	}

	public String translateOffline(String key, ProxiedPlayer player, Object... args) {
		return manager.translate(key, args);
	}

	public String translate(String key, CommandSender cs, Object... args) {
		if(cs instanceof ProxiedPlayer)
			return manager.translate(key, Main.getDatenServer().getClient().getPlayerAndLoad(cs.getName()), args);
		else
			return translate(key, LanguageType.ENGLISH, args);
	}

	public String translateWithPrefix(String key, CommandSender cs, Object... args) {
		if(cs instanceof ProxiedPlayer)
			return Main.getTranslationManager().translate("prefix",cs)+manager.translate(key, Main.getDatenServer().getClient().getPlayerAndLoad(cs.getName()), args);
		else
			return Main.getTranslationManager().translate("prefix",cs)+translate(key, LanguageType.ENGLISH, args);
	}

	public String translate(String key, LanguageType lang, Object... args) {
		return manager.translate(key, lang, args);
	}

	public void updateLanguage(LoadedPlayer player) {
		manager.updateLanguage(player);
	}

	public LanguageType getLanguage(ProxiedPlayer player) {
		if(!Main.getDatenServer().isActive())
			return LanguageType.ENGLISH;
		return manager.getLanguage(Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()));
	}
	public void registerFallback(LanguageType type,String key,String message){
		manager.getTranslationFile(type).registerFallbackMessage(key, message);
	}

}
