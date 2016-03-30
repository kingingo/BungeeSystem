package me.kingingo.kBungeeCord.Language;

import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LanguageType;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Language {

	@Getter
	private static HashMap<LanguageType, HashMap<String, String>> list;
	@Getter
	private static HashMap<UUID, LanguageType> languages;

	public static void init() {
		list = new HashMap<>();
		languages = new HashMap<>();
		for(LanguageType l : LanguageType.values())
			addALL(l);
	}

	public static void updateLanguage(ProxiedPlayer player, LanguageType type) {
		if(player == null)
			return;
		languages.remove(player.getUniqueId());
		languages.put(player.getUniqueId(), type);
	}

	public static LanguageType getLanguage(ProxiedPlayer player) {
		if (player == null) {
			System.out.println("[Language] Spieler == null");
			return LanguageType.ENGLISH;
		} else if (!languages.containsKey(player.getUniqueId())) {
			LanguageType lang = Main.getDatenServer().getClient().getPlayer(player.getUniqueId()).getLanguageSync();
			languages.put(player.getUniqueId(), lang);
			return lang;
		} else if (!list.containsKey(languages.get(player.getUniqueId()))) {
			addALL(languages.get(player.getUniqueId()));
			System.out.println("[Language] Die Sprache " + languages.get(player.getUniqueId()).getDef() + " wurde nicht gefunden.");
		}

		return languages.get(player.getUniqueId());
	}

	public static void sendText(ProxiedPlayer player, String name, Object[] input) {
		player.sendMessage(toText(list.get(getLanguage(player)).get(name), input));
	}

	public static void sendText(ProxiedPlayer player, String name, Object input) {
		player.sendMessage(toText(list.get(getLanguage(player)).get(name), input));
	}

	public static String getText(String name, Object input) {
		if (!list.get(LanguageType.ENGLISH).containsKey(name)) {
			System.out.println("[Language] Message nicht gefunden " + name);
			return Language.getText("MSG_NOT_FOUND", name);
		}
		return toText(list.get(LanguageType.ENGLISH).get(name), input);
	}

	public static String getText(String name, Object[] input) {
		if (!list.get(LanguageType.ENGLISH).containsKey(name)) {
			System.out.println("[Language] Message nicht gefunden " + name);
			return Language.getText("MSG_NOT_FOUND", name);
		}
		return toText(list.get(LanguageType.ENGLISH).get(name), input);
	}

	public static String getText(String name) {
		if (!list.get(LanguageType.ENGLISH).containsKey(name)) {
			System.out.println("[Language] Message nicht gefunden " + name);
			return Language.getText("MSG_NOT_FOUND", name);
		}
		return list.get(LanguageType.ENGLISH).get(name);
	}

	public static String getText(ProxiedPlayer player, String name) {
		if (!list.get(getLanguage(player)).containsKey(name)) {
			System.out.println("[Language] Message nicht gefunden " + name + " " + getLanguage(player).getDef());
			return Language.getText(player, "MSG_NOT_FOUND", name);
		}
		return list.get(getLanguage(player)).get(name);
	}

	public static String getText(ProxiedPlayer player, String name, Object[] input) {
		if (!list.get(getLanguage(player)).containsKey(name)) {
			System.out.println("[Language] Message nicht gefunden " + name + " " + getLanguage(player).getDef());
			return Language.getText(player, "MSG_NOT_FOUND", name);
		}
		return toText(list.get(getLanguage(player)).get(name), input);
	}

	public static String getText(ProxiedPlayer player, String name, Object input) {
		if (!list.get(getLanguage(player)).containsKey(name)) {
			System.out.println("[Language] Message nicht gefunden " + name + " " + getLanguage(player).getDef());
			return Language.getText(player, "MSG_NOT_FOUND", name);
		}
		return toText(list.get(getLanguage(player)).get(name), input);
	}

	public static String toText(String msg, Object input) {
		return (msg.contains("{INPUT0}") ? msg.replaceAll("\\{INPUT0\\}", String.valueOf(input)) : msg);
	}

	public static String toText(String msg, Object[] input) {
		for (int i = 0; i < input.length; i++) {
			msg = msg.replaceFirst("\\{INPUT" + i + "\\}", String.valueOf(input[i]));
		}
		return msg;
	}

	public static void addALL(LanguageType type) {
		if (type == LanguageType.GERMAN) {
			if (!list.containsKey(LanguageType.GERMAN))
				list.put(LanguageType.GERMAN, new HashMap<String, String>());
			add(type, "PREFIX", "§6EpicPvP §8§ §7");
			add(type, "BG_ADD_SERVER", "§aDer Server§e {INPUT0}§a wurde hinzugefuegt!");
			add(type, "BG_NO_MSG", "§cDu musst eine Nachricht Schreiben!");
			add(type, "BG_YOU_ARE_NOW_ON", "§cDu bist bereits auf dem §e{INPUT0} §cServer!");
			add(type, "BG_CC", "§aDer Chat wurde geleert!");
			add(type, "BG_CLIENT_SEND", "§aDie Nachricht §e{INPUT0}§a wurde gesendet!");
			add(type, "BG_CLIENT_PING", "§aDie Ping anfrage wurde gestellt!");
			add(type, "BG_CLIENT_RECONNECT", "§aDer Client reconnected...!");
			add(type, "BG_DEL_SERVER", "§aDer Server§e {INPUT0}§a wurde entfernt!");
			add(type, "BG_CLIENT_SERVER_NOT_FOUND", "§cDer Server wurde nicht gefunden .... ");
			add(type, "BG_GLIST", "§7Auf allen Servern sind insgesamt: §e{INPUT0} §7Spieler online!");
			add(type, "BG_INTEGER", "§cDas ist keine Zahl!");
			add(type, "BG_NOT_PERMISSION_LVL", "§cDu besitzt nicht die noetigen Permission um dieses LVL zu vergeben!");
			add(type, "BG_BAN_SCREEN", "§cDu wurdest §4PERMANENT §cvom Netzwerk gebannt! \n§3Grund: §c{INPUT0} \n§aDu kannst auf §ewww.EpicPvP.org §aeinen Entbannungsantrag stellen.");
			add(type, "BG_BAN_DISCONNECT", "§cDu wurdest {INPUT0} §cvom Netzwerk gebannt! \n§3Grund: §c{INPUT1} \n§aDu kannst auf §ewww.EpicPvP.org §aeinen Entbannungsantrag stellen.");
			add(type, "BG_BAN_MSG", "§cDer Spieler §e{INPUT0}§c wurde von §e{INPUT1}§c Permanent Gesperrt Grund:§e {INPUT2}");
			add(type, "BG_KICK_MSG", "§cDer Spieler §e{INPUT0}§c wurde von §e{INPUT1}§c Gekickt Grund: §e{INPUT2}");
			add(type, "BG_KICK_SCREEN", "§cDu wurdest vom Netzwerk gekickt. \n§3Grund§8:§c {INPUT0}");
			add(type, "BG_INFO_TIME_BAN", "§cZeit-Banned:§e gebannt seit {INPUT0} noch {INPUT1} von {INPUT2}");
			add(type, "BG_INFO_TIME_BAN_GRUND", "§cGrund:§e {INPUT0}");
			add(type, "BG_INFO_NOT_TIME_BANNED", "§cZeit-Banned:§e nicht zeit gebannt");
			add(type, "BG_INFO_NOT_BANNED", "§cBanned:§e nicht gebannt");
			add(type, "BG_INFO_BAN", "§cBanned:§e gebannt seit {INPUT0} von {INPUT1}");
			add(type, "BG_NICK_SET", "§aDer Nickname §e{INPUT0}§a wurde gesetzt!");
			add(type, "BG_NICK_DEL", "§aDer Nickname wurde entfernt!");
			add(type, "BG_PERFORMANCE", "§7BG: §b{INPUT0}§7 Online:§b{INPUT2} §7Player-Online:§b{INPUT3}§7 Avg-Ping:§b{INPUT4} §7TPS:§b{INPUT5} §7Ram:§b{INPUT6} MB");
			add(type, "BG_PREMIUM_MSG1", "§4§lACHTUNG! §cWenn du fortfaehrst, kannst du dich nicht mehr mit");
			add(type, "BG_PREMIUM_MSG2", "§cCracked Einloggen! §7Bitte ueberlege diesen Schritt genau und");
			add(type, "BG_PREMIUM_MSG3", "§7fuehre ihn wirklich §4§lNUR §7durch wenn du diesen Account bei");
			add(type, "BG_PREMIUM_MSG4", "§eminecraft.net §7gekauft hast! §4Du wurdest gewarnt!");
			add(type, "BG_PREMIUM_MSG5", "§aFortfahren §7(§cauf eigene Gefahr!§7): §e§l/Premium on");
			add(type, "BG_PREMIUM_YOU_ARE_NOT", "§cDu bist kein Premium Mitglied!");
			add(type, "BG_PREMIUM_KICK_OFF", "§7Dein §ePremium-Login§7 wurde §cdeaktiviert§7! Probiere es gleich aus!");
			add(type, "BG_PREMIUM_KICK_ON", "§7Dein §ePremium-Login§7 wurde §aaktiviert§7! Probiere es gleich aus!");
			add(type, "BG_PREMIUM_ON", "§a{INPUT0} §ePremium-Login§a wurde aktiviert!");
			add(type, "BG_PREMIUM_NOT", "§cDieser Spieler ist kein Premium Mitglied!");
			add(type, "BG_PREMIUM_OFF", "§c{INPUT0} §ePremium-Login§c wurde deaktiviert!");
			add(type, "BG_PW_NOT", "§cDas ist nicht dein Password {INPUT0}");
			add(type, "BG_PW_CHANGE", "§aDas Password wurde zu §e{INPUT0} geaendert!");
			add(type, "BG_SERVER_NOT_EXIST", "§cServer exestiert nicht!");
			add(type, "BG_PLAYER_SEND", "§aSpieler wurde zum Server versandt");
			add(type, "BG_SEND_A", "§aEine Anfrage wurde zu den BGs gesendet");
			add(type, "NO_PERMISSION", "§cKeine Berechtigungen!");
			add(type, "BG_TS", "§aTeamSpeak: §7ts.EpicPvP.eu §8| §eWebsite: §7EpicPvP.eu");
			add(type, "BG_EVENT_SERVER", "§aHerzlichen Glueckwunsch du bist den §eEvent-Server §abeigetreten!");
			add(type, "BG_EVENT_NOT_NOW", "§cMomentan steht kein §eEvent §cauf dem Plan!");
			add(type, "BG_TEST_SERVER", "§aHerzlichen Glueckwunsch du bist der §eTest-Runde §abeigetreten!");
			add(type, "BG_TEST_NOT_NOW", "§cMomentan steht keine §eTest-Runde §cauf dem Plan!");
			add(type, "BG_TEST_SERVER_INVITE", "§aDu wurdest von §e{INPUT0}§a zu einer Test-Runde eingeladen!");
			add(type, "BG_TEST_SERVER_INVITE1", "§aUm anzunehmen: §e/testserver");
			add(type, "BG_TEST_SEND_INVITES", "§aDie Spieler wurde eingeladen!");
			add(type, "BG_TICKET_NOT_FOUND", "§cEs wurde kein Ticket gefunden!");
			add(type, "BG_TICKET_PREFIX", "§eTicket §8§ §7");
			add(type, "BG_NO_TICKETS_OPEN", "§cIm moment sind keine Tickets offen!");
			add(type, "BG_TICKET_CREATE", "§aDas Ticket wurde gestellt!");
			add(type, "BG_TICKET_LAST_TICKET", "§cDein letztes Ticket ist noch in Bearbeitung!");
			add(type, "BG_TICKET_REASON", "§a{INPUT0}§7 Grund: §e{INPUT1}");
			add(type, "BG_TICKET_SUFFIX", ":§7 ");
			add(type, "BG_TICKET_MSG1", "§7Der Spieler §a{INPUT0}§7 hat ein Neues Ticket eroeffnet!");
			add(type, "BG_TICKET_MSG2", "§7Ticket-Grund:§c {INPUT0}");
			add(type, "BG_TICKET_MSG3", "§7Server:§e {INPUT0}");
			add(type, "BG_TICKET_MSG4", "§7BungeeCord-Instance:§e {INPUT0}");
			add(type, "BG_TICKET_ACCEPT", "§a{INPUT0}§7 hat das Ticket von §e{INPUT1}§7 angenommen und bearbeitet dieses!");
			add(type, "BG_TICKET_CLOSE", "§7Das Ticket von §a{INPUT0}§7 wurde geschlossen!");
			add(type, "BG_UNBAN", "§aDer Spieler§e {INPUT0} §awurde entbannt!");
			add(type, "BG_WHEREIS", "§7Der Spieler §a{INPUT0}§7 befindet sich auf den Server §e{INPUT1} §7 und BungeeCord §e {INPUT2}");
			add(type, "BG_WHO", "§7Spieler: §e{INPUT0} §7IP:§e {INPUT1}§7 UUID:§e {INPUT2}§7 Real-UUID:§e {INPUT3}");
			add(type, "BG_WHEREIS_SEARCH", "§cDer Spieler §e{INPUT0}§c ist offline!");
			add(type, "BG_TIME_BAN_NAME", "§cDu musst einen Namen angeben!");
			add(type, "BG_TIME_BAN_TYP", "§e{INPUT0}§c ist kein Typ");
			add(type, "BG_TIME_BAN_MAX_DAY", "§cDu kannst nur maximal einen §e{INPUT0} §cTage Zeitban geben!");
			add(type, "BG_TIME_BAN_CHAT", "§cDer Spieler §e{INPUT0}§c wurde von §e{INPUT1}§c fuer {INPUT2} {INPUT3} Gesperrt Grund: §e{INPUT4}");
			add(type, "BG_TIME_BAN_KICK", "§cDu wurdest fuer §e{INPUT0} {INPUT1} §cvom Netzwerk gebannt! \n§3Grund: §c{INPUT2} \n§aDu kannst auf §ewww.EpicPvP.org §aeinen Entbannungsantrag stellen.");
			add(type, "TWITTER_NOT_FOLLOW", "§cDu musst @EpicPvPMC auf Twitter folgen um deinen Account hinzuzufuegen!");
			add(type, "TWITTER_CHECK", "Twitter Account wird §berpr§ft...");
			add(type, "TWITTER_ADD", "§aDein Twitter Account wurde hinzugef§gt!");
			add(type, "TWITTER_IS_ADDED", "Du hast diesen Twitter Account bereits hinzugef§gt!");
			add(type, "TWITTER_IS_USED", "§cDieser Twitter Account hat bereits ein andere Spieler benutzt!");
			add(type, "TWITTER_FAIL", "§cBitte kontaktiere KingIngo und schreibe ihn diese Daten: §b{INPUT0}");
			add(type, "TWITTER_ACC", "Dein Twitter Account: §a{INPUT0}");
			add(type, "TWITTER_NOT_FIND", "§cWir konnten deinen Twitter Account nicht finden!");
			add(type, "BG_PLAYER_FIND", "§e{INPUT0}§a wurde zu den Server §e{INPUT1}§a geschickt!");
			add(type, "HUB_MUTE", "§cDu kannst erst in §e{INPUT0}§c auf dem Hub schreiben!");
			add(type, "BG_DATEN_SERVER_DISCONNECT", "§cDer Daten Server ist nicht erreichbar!");
			add(type, "BG_DATEN_SERVER_CONNECT", "§aDer Daten Server hat sich nun verbunden!");
			add(type, "BG_EVENT_SERVER_WAIT", "§cEs verbinden sich gerade zu viele Spieler.");
		}

		if (type == LanguageType.ENGLISH) {
			if (!list.containsKey(LanguageType.ENGLISH))
				list.put(LanguageType.ENGLISH, new HashMap<String, String>());
			add(type, "PREFIX", "§6EpicPvP §8§ §7");
			add(type, "BG_DATEN_SERVER_DISCONNECT", "§cThe Data Server is not contactable!");
			add(type, "BG_DATEN_SERVER_CONNECT", "§aThe Data Server connected!");
			add(type, "BG_ADD_SERVER", "§aServer§e {INPUT0}§a was added!");
			add(type, "BG_NO_MSG", "§cYou have to write a message!");
			add(type, "BG_YOU_ARE_NOW_ON", "§cYou are allready on the §e{INPUT0} §cserver!");
			add(type, "BG_CC", "§aChat was cleared!");
			add(type, "BG_CLIENT_SEND", "§aThe message §e{INPUT0}§a was send!");
			add(type, "BG_CLIENT_PING", "§aThe ping request was send!");
			add(type, "BG_CLIENT_RECONNECT", "§aThe client reconnected...!");
			add(type, "BG_DEL_SERVER", "§aThe Server §e {INPUT0}§a was removed!");
			add(type, "BG_CLIENT_SERVER_NOT_FOUND", "§c404 Server not found .... ");
			add(type, "BG_GLIST", "§7On all server there is a total of §e{INPUT0} §7players online!");
			add(type, "BG_INTEGER", "§cThat is not a number!");
			add(type, "BG_NOT_PERMISSION_LVL", "§cYou do not have necessary permissions for this ban-level!");
			add(type, "BG_BAN_SCREEN", "§cYou were banned §4PERMANENTLY §cfrom the Network! \n§3Reason: §c{INPUT0} \n \n§aYou can write an unban-request at §ewww.EpicPvP.org");
			add(type, "BG_BAN_DISCONNECT", "§cYou were banned {INPUT0} §cfrom the Network! \n§3Reason: §c{INPUT1} \n \n§aYou can write an unban-request at §ewww.EpicPvP.org");
			add(type, "BG_BAN_MSG", "§e{INPUT0}§c was permanently banned by §e{INPUT1}§c. Reason:§e {INPUT2}");
			add(type, "BG_KICK_MSG", "§e{INPUT0}§c was kicked by §e{INPUT1}§c. Reason: §e{INPUT2}");
			add(type, "BG_KICK_SCREEN", "§cYou were kicked form the Network. \n§3Reason§8:§c {INPUT0}");
			add(type, "BG_INFO_TIME_BAN", "§cTime-banned:§e banned since {INPUT0} remaining {INPUT1} by {INPUT2}");
			add(type, "BG_INFO_TIME_BAN_GRUND", "§cReason:§e {INPUT0}");
			add(type, "BG_INFO_NOT_TIME_BANNED", "§cTime-banned:§e not time-banned");
			add(type, "BG_INFO_NOT_BANNED", "§cBanned:§e not banned");
			add(type, "BG_INFO_BAN", "§cBanned:§e banned since {INPUT0} by {INPUT1}");
			add(type, "BG_NICK_SET", "§aNickname §e{INPUT0}§a set!");
			add(type, "BG_NICK_DEL", "§aNickname removed!");
			add(type, "BG_PERFORMANCE", "§7BG: §b{INPUT0}§7 Online:§b{INPUT2} §7Player-Online:§b{INPUT3}§7 Avg-Ping:§b{INPUT4} §7TPS:§b{INPUT5} §7Ram:§b{INPUT6} MB");
			add(type, "BG_PREMIUM_MSG1", "§4§lWARNING! §cIf you proceed you will be unable to use a cracked minecraft version");
			add(type, "BG_PREMIUM_MSG2", "§7Please think carefully about this step and");
			add(type, "BG_PREMIUM_MSG3", "§7only continue if you bought the game at");
			add(type, "BG_PREMIUM_MSG4", "§eminecraft.net§7! §4You have been warned!");
			add(type, "BG_PREMIUM_MSG5", "§aProceed §7(§cat your own risk!§7): §e§l/premium on");
			add(type, "BG_PREMIUM_YOU_ARE_NOT", "§cYou are not a minecraft premium member!");
			add(type, "BG_PREMIUM_KICK_OFF", "§7Your §ePremium-login§7 was §cdeactivated§7! Try it!");
			add(type, "BG_PREMIUM_KICK_ON", "§7Your §ePremium-login§7 was §aactivated§7! Try it!");
			add(type, "BG_PREMIUM_ON", "§a{INPUT0}s §ePremium-login§a was activated!");
			add(type, "BG_PREMIUM_NOT", "§cThis player is not a minecraft premium member!");
			add(type, "BG_PREMIUM_OFF", "§c{INPUT0} §ePremium-login§c was deactivated!");
			add(type, "BG_PW_NOT", "§cThis is not your password {INPUT0}");
			add(type, "BG_PW_CHANGE", "§aYour password was changed to §e{INPUT0}");
			add(type, "BG_SERVER_NOT_EXIST", "§cServer does not exist!");
			add(type, "BG_PLAYER_SEND", "§aPlayer was send to the server");
			add(type, "BG_SEND_A", "§aA request was send to the BGs");
			add(type, "NO_PERMISSION", "§cNo permissions!");
			add(type, "BG_TS", "§aTeamSpeak: §7ts.EpicPvP.eu §8| §eWebsite: §7EpicPvP.eu");
			add(type, "BG_EVENT_SERVER", "§aCongratulations you joined the §eEvent-Server§a!");
			add(type, "BG_EVENT_NOT_NOW", "§cThere is no §eevent-round §cscheduled!");
			add(type, "BG_TEST_SERVER", "§aCongratulations you joined the §etest-round§a!");
			add(type, "BG_TEST_NOT_NOW", "§cThere is no §etest-round §cscheduled!");
			add(type, "BG_TEST_SERVER_INVITE", "§aYou were invited to a test-round by §e{INPUT0}§a!");
			add(type, "BG_TEST_SERVER_INVITE1", "§aTo accept: §e/testserver");
			add(type, "BG_TEST_SEND_INVITES", "§aPlayer invited!");
			add(type, "BG_TICKET_NOT_FOUND", "§cNo ticket found!");
			add(type, "BG_TICKET_PREFIX", "§eTicket §8§ §7");
			add(type, "BG_NO_TICKETS_OPEN", "§cThere are not any open tickets at the moment!");
			add(type, "BG_TICKET_CREATE", "§aTicket created!");
			add(type, "BG_TICKET_LAST_TICKET", "§cYour last ticket is still pending!");
			add(type, "BG_TICKET_REASON", "§a{INPUT0}§7 Reason: §e{INPUT1}");
			add(type, "BG_TICKET_SUFFIX", ":§7 ");
			add(type, "BG_TICKET_MSG1", "§a{INPUT0}§7 opend a new ticket!");
			add(type, "BG_TICKET_MSG2", "§7Ticket-reason:§c {INPUT0}");
			add(type, "BG_TICKET_MSG3", "§7Server:§e {INPUT0}");
			add(type, "BG_TICKET_MSG4", "§7BungeeCord-Instance:§e {INPUT0}");
			add(type, "BG_TICKET_ACCEPT", "§a{INPUT0}§7 is now working on §e{INPUT1}s§7 ticket!");
			add(type, "BG_TICKET_CLOSE", "§a{INPUT0}s§7 ticket closed!");
			add(type, "BG_UNBAN", "§e{INPUT0} §ais now unbanned!");
			add(type, "BG_WHEREIS", "§a{INPUT0}§7 is currently on server §e{INPUT1}§7 and BungeeCord §e{INPUT2}");
			add(type, "BG_WHO", "§7Player: §e{INPUT0} §7IP:§e {INPUT1}§7 UUID:§e {INPUT2}§7 Real-UUID:§e {INPUT3}");
			add(type, "BG_WHEREIS_SEARCH", "§e{INPUT0}§c is currently offline!");
			add(type, "BG_TIME_BAN_NAME", "§cYou have to enter a name!");
			add(type, "BG_TIME_BAN_TYP", "§e{INPUT0}§c is not a type");
			add(type, "BG_TIME_BAN_MAX_DAY", "§cYou can only set a max. temp-ban time of §e{INPUT0} §cdays!");
			add(type, "BG_TIME_BAN_CHAT", "§e{INPUT0}§c was banned by §e{INPUT1}§c for {INPUT2} {INPUT3} banned. Reason: §e{INPUT4}");
			add(type, "BG_TIME_BAN_KICK", "§cYou were banned for §e{INPUT0} {INPUT1} §cfrom the Network! \n§3Reason: §c{INPUT2} \n \n§aYou can write an unban-request at §ewww.EpicPvP.org");
			add(type, "TWITTER_NOT_FOLLOW", "§cYou have to follow @EpicPvPMC on Twitter to add your account!");
			add(type, "TWITTER_CHECK", "Check Twitter account...");
			add(type, "TWITTER_ADD", "§aYour Twitter account has been added!");
			add(type, "TWITTER_IS_ADDED", "You have already added this Twitter account!");
			add(type, "TWITTER_IS_USED", "§cThis Twitter account is already in use by another Player!");
			add(type, "TWITTER_FAIL", "§cPlease contact Kingingo with this message: §b{INPUT0}");
			add(type, "TWITTER_ACC", "Your Twitter account: §a{INPUT0}");
			add(type, "TWITTER_NOT_FIND", "§cYour Twitter account cannot find!");
			add(type, "HUB_MUTE", "§cYou are able to Chat here in §e{INPUT0}§c!");
			add(type, "BG_PLAYER_FIND", "§e{INPUT0}§a switched to §e{INPUT1}");
			add(type, "BG_EVENT_SERVER_WAIT", "§cToo many people try to connect. Please wait.");
		}
	}

	public static void add(LanguageType type, String name, String msg) {
		if (!list.get(type).containsKey(name)) {
			list.get(type).put(name, msg);
		}
	}
}
