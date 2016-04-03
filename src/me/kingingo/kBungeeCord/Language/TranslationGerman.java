package me.kingingo.kBungeeCord.Language;

import dev.wolveringer.client.LanguageType;

public class TranslationGerman extends Translation{

	@Override
	public LanguageType getLanguage() {
		return LanguageType.GERMAN;
	}

	@Override
	public void registerTranslations() {
		registerTranslation("PREFIX", "§6EpicPvP §8§ §7");
		registerTranslation("BG_ADD_SERVER", "§aDer Server§e {INPUT0}§a wurde hinzugefuegt!");
		registerTranslation("BG_NO_MSG", "§cDu musst eine Nachricht Schreiben!");
		registerTranslation("BG_YOU_ARE_NOW_ON", "§cDu bist bereits auf dem §e{INPUT0} §cServer!");
		registerTranslation("BG_CC", "§aDer Chat wurde geleert!");
		registerTranslation("BG_CLIENT_SEND", "§aDie Nachricht §e{INPUT0}§a wurde gesendet!");
		registerTranslation("BG_CLIENT_PING", "§aDie Ping anfrage wurde gestellt!");
		registerTranslation("BG_CLIENT_RECONNECT", "§aDer Client reconnected...!");
		registerTranslation("BG_DEL_SERVER", "§aDer Server§e {INPUT0}§a wurde entfernt!");
		registerTranslation("BG_CLIENT_SERVER_NOT_FOUND", "§cDer Server wurde nicht gefunden .... ");
		registerTranslation("BG_GLIST", "§7Auf allen Servern sind insgesamt: §e{INPUT0} §7Spieler online!");
		registerTranslation("BG_INTEGER", "§cDas ist keine Zahl!");
		registerTranslation("BG_NOT_PERMISSION_LVL", "§cDu besitzt nicht die noetigen Permission um dieses LVL zu vergeben!");
		registerTranslation("BG_BAN_SCREEN", "§cDu wurdest §4PERMANENT §cvom Netzwerk gebannt! \n§3Grund: §c{INPUT0} \n§aDu kannst auf §ewww.EpicPvP.org §aeinen Entbannungsantrag stellen.");
		registerTranslation("BG_BAN_DISCONNECT", "§cDu wurdest {INPUT0} §cvom Netzwerk gebannt! \n§3Grund: §c{INPUT1} \n§aDu kannst auf §ewww.EpicPvP.org §aeinen Entbannungsantrag stellen.");
		registerTranslation("BG_BAN_MSG", "§cDer Spieler §e{INPUT0}§c wurde von §e{INPUT1}§c Permanent Gesperrt Grund:§e {INPUT2}");
		registerTranslation("BG_KICK_MSG", "§cDer Spieler §e{INPUT0}§c wurde von §e{INPUT1}§c Gekickt Grund: §e{INPUT2}");
		registerTranslation("BG_KICK_SCREEN", "§cDu wurdest vom Netzwerk gekickt. \n§3Grund§8:§c {INPUT0}");
		registerTranslation("BG_INFO_TIME_BAN", "§cZeit-Banned:§e gebannt seit {INPUT0} noch {INPUT1} von {INPUT2}");
		registerTranslation("BG_INFO_TIME_BAN_GRUND", "§cGrund:§e {INPUT0}");
		registerTranslation("BG_INFO_NOT_TIME_BANNED", "§cZeit-Banned:§e nicht zeit gebannt");
		registerTranslation("BG_INFO_NOT_BANNED", "§cBanned:§e nicht gebannt");
		registerTranslation("BG_INFO_BAN", "§cBanned:§e gebannt seit {INPUT0} von {INPUT1}");
		registerTranslation("BG_NICK_SET", "§aDer Nickname §e{INPUT0}§a wurde gesetzt!");
		registerTranslation("BG_NICK_DEL", "§aDer Nickname wurde entfernt!");
		registerTranslation("BG_PERFORMANCE", "§7BG: §b{INPUT0}§7 Online:§b{INPUT2} §7Player-Online:§b{INPUT3}§7 Avg-Ping:§b{INPUT4} §7TPS:§b{INPUT5} §7Ram:§b{INPUT6} MB");
		registerTranslation("BG_PREMIUM_MSG1", "§4§lACHTUNG! §cWenn du fortfaehrst, kannst du dich nicht mehr mit");
		registerTranslation("BG_PREMIUM_MSG2", "§cCracked Einloggen! §7Bitte ueberlege diesen Schritt genau und");
		registerTranslation("BG_PREMIUM_MSG3", "§7fuehre ihn wirklich §4§lNUR §7durch wenn du diesen Account bei");
		registerTranslation("BG_PREMIUM_MSG4", "§eminecraft.net §7gekauft hast! §4Du wurdest gewarnt!");
		registerTranslation("BG_PREMIUM_MSG5", "§aFortfahren §7(§cauf eigene Gefahr!§7): §e§l/Premium on");
		registerTranslation("BG_PREMIUM_YOU_ARE_NOT", "§cDu bist kein Premium Mitglied!");
		registerTranslation("BG_PREMIUM_KICK_OFF", "§7Dein §ePremium-Login§7 wurde §cdeaktiviert§7! Probiere es gleich aus!");
		registerTranslation("BG_PREMIUM_KICK_ON", "§7Dein §ePremium-Login§7 wurde §aaktiviert§7! Probiere es gleich aus!");
		registerTranslation("BG_PREMIUM_ON", "§a{INPUT0} §ePremium-Login§a wurde aktiviert!");
		registerTranslation("BG_PREMIUM_NOT", "§cDieser Spieler ist kein Premium Mitglied!");
		registerTranslation("BG_PREMIUM_OFF", "§c{INPUT0} §ePremium-Login§c wurde deaktiviert!");
		registerTranslation("BG_PW_NOT", "§cDas ist nicht dein Password {INPUT0}");
		registerTranslation("BG_PW_CHANGE", "§aDas Password wurde zu §e{INPUT0} geaendert!");
		registerTranslation("BG_SERVER_NOT_EXIST", "§cServer exestiert nicht!");
		registerTranslation("BG_PLAYER_SEND", "§aSpieler wurde zum Server versandt");
		registerTranslation("BG_SEND_A", "§aEine Anfrage wurde zu den BGs gesendet");
		registerTranslation("NO_PERMISSION", "§cKeine Berechtigungen!");
		registerTranslation("BG_TS", "§aTeamSpeak: §7ts.EpicPvP.eu §8| §eWebsite: §7EpicPvP.eu");
		registerTranslation("BG_EVENT_SERVER", "§aHerzlichen Glueckwunsch du bist den §eEvent-Server §abeigetreten!");
		registerTranslation("BG_EVENT_NOT_NOW", "§cMomentan steht kein §eEvent §cauf dem Plan!");
		registerTranslation("BG_TEST_SERVER", "§aHerzlichen Glueckwunsch du bist der §eTest-Runde §abeigetreten!");
		registerTranslation("BG_TEST_NOT_NOW", "§cMomentan steht keine §eTest-Runde §cauf dem Plan!");
		registerTranslation("BG_TEST_SERVER_INVITE", "§aDu wurdest von §e{INPUT0}§a zu einer Test-Runde eingeladen!");
		registerTranslation("BG_TEST_SERVER_INVITE1", "§aUm anzunehmen: §e/testserver");
		registerTranslation("BG_TEST_SEND_INVITES", "§aDie Spieler wurde eingeladen!");
		registerTranslation("BG_TICKET_NOT_FOUND", "§cEs wurde kein Ticket gefunden!");
		registerTranslation("BG_TICKET_PREFIX", "§eTicket §8§ §7");
		registerTranslation("BG_NO_TICKETS_OPEN", "§cIm moment sind keine Tickets offen!");
		registerTranslation("BG_TICKET_CREATE", "§aDas Ticket wurde gestellt!");
		registerTranslation("BG_TICKET_LAST_TICKET", "§cDein letztes Ticket ist noch in Bearbeitung!");
		registerTranslation("BG_TICKET_REASON", "§a{INPUT0}§7 Grund: §e{INPUT1}");
		registerTranslation("BG_TICKET_SUFFIX", ":§7 ");
		registerTranslation("BG_TICKET_MSG1", "§7Der Spieler §a{INPUT0}§7 hat ein Neues Ticket eroeffnet!");
		registerTranslation("BG_TICKET_MSG2", "§7Ticket-Grund:§c {INPUT0}");
		registerTranslation("BG_TICKET_MSG3", "§7Server:§e {INPUT0}");
		registerTranslation("BG_TICKET_MSG4", "§7BungeeCord-Instance:§e {INPUT0}");
		registerTranslation("BG_TICKET_ACCEPT", "§a{INPUT0}§7 hat das Ticket von §e{INPUT1}§7 angenommen und bearbeitet dieses!");
		registerTranslation("BG_TICKET_CLOSE", "§7Das Ticket von §a{INPUT0}§7 wurde geschlossen!");
		registerTranslation("BG_UNBAN", "§aDer Spieler§e {INPUT0} §awurde entbannt!");
		registerTranslation("BG_WHEREIS", "§7Der Spieler §a{INPUT0}§7 befindet sich auf den Server §e{INPUT1} §7 und BungeeCord §e {INPUT2}");
		registerTranslation("BG_WHO", "§7Spieler: §e{INPUT0} §7IP:§e {INPUT1}§7 UUID:§e {INPUT2}§7 Real-UUID:§e {INPUT3}");
		registerTranslation("BG_WHEREIS_SEARCH", "§cDer Spieler §e{INPUT0}§c ist offline!");
		registerTranslation("BG_TIME_BAN_NAME", "§cDu musst einen Namen angeben!");
		registerTranslation("BG_TIME_BAN_TYP", "§e{INPUT0}§c ist kein Typ");
		registerTranslation("BG_TIME_BAN_MAX_DAY", "§cDu kannst nur maximal einen §e{INPUT0} §cTage Zeitban geben!");
		registerTranslation("BG_TIME_BAN_CHAT", "§cDer Spieler §e{INPUT0}§c wurde von §e{INPUT1}§c fuer {INPUT2} {INPUT3} Gesperrt Grund: §e{INPUT4}");
		registerTranslation("BG_TIME_BAN_KICK", "§cDu wurdest fuer §e{INPUT0} {INPUT1} §cvom Netzwerk gebannt! \n§3Grund: §c{INPUT2} \n§aDu kannst auf §ewww.EpicPvP.org §aeinen Entbannungsantrag stellen.");
		registerTranslation("TWITTER_NOT_FOLLOW", "§cDu musst @EpicPvPMC auf Twitter folgen um deinen Account hinzuzufuegen!");
		registerTranslation("TWITTER_CHECK", "Twitter Account wird §berpr§ft...");
		registerTranslation("TWITTER_ADD", "§aDein Twitter Account wurde hinzugef§gt!");
		registerTranslation("TWITTER_IS_ADDED", "Du hast diesen Twitter Account bereits hinzugef§gt!");
		registerTranslation("TWITTER_IS_USED", "§cDieser Twitter Account hat bereits ein andere Spieler benutzt!");
		registerTranslation("TWITTER_FAIL", "§cBitte kontaktiere KingIngo und schreibe ihn diese Daten: §b{INPUT0}");
		registerTranslation("TWITTER_ACC", "Dein Twitter Account: §a{INPUT0}");
		registerTranslation("TWITTER_NOT_FIND", "§cWir konnten deinen Twitter Account nicht finden!");
		registerTranslation("BG_PLAYER_FIND", "§e{INPUT0}§a wurde zu den Server §e{INPUT1}§a geschickt!");
		registerTranslation("HUB_MUTE", "§cDu kannst erst in §e{INPUT0}§c auf dem Hub schreiben!");
		registerTranslation("BG_DATEN_SERVER_DISCONNECT", "§cDer Daten Server ist nicht erreichbar!");
		registerTranslation("BG_DATEN_SERVER_CONNECT", "§aDer Daten Server hat sich nun verbunden!");
		registerTranslation("BG_EVENT_SERVER_WAIT", "§cEs verbinden sich gerade zu viele Spieler.");
	}

}
