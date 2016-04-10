package me.kingingo.kBungeeCord.Language;

import dev.wolveringer.dataserver.player.LanguageType;

public class TranslationEnglish extends Translation{

	@Override
	public LanguageType getLanguage() {
		return LanguageType.ENGLISH;
	}

	@Override
	public void registerTranslations() {
		registerTranslation("PREFIX", "§6EpicPvP §8§ §7");
		registerTranslation("BG_DATEN_SERVER_DISCONNECT", "§cThe Data Server is not contactable!");
		registerTranslation("BG_DATEN_SERVER_CONNECT", "§aThe Data Server connected!");
		registerTranslation("BG_ADD_SERVER", "§aServer§e {INPUT0}§a was added!");
		registerTranslation("BG_NO_MSG", "§cYou have to write a message!");
		registerTranslation("BG_YOU_ARE_NOW_ON", "§cYou are allready on the §e{INPUT0} §cserver!");
		registerTranslation("BG_CC", "§aChat was cleared!");
		registerTranslation("BG_CLIENT_SEND", "§aThe message §e{INPUT0}§a was send!");
		registerTranslation("BG_CLIENT_PING", "§aThe ping request was send!");
		registerTranslation("BG_CLIENT_RECONNECT", "§aThe client reconnected...!");
		registerTranslation("BG_DEL_SERVER", "§aThe Server §e {INPUT0}§a was removed!");
		registerTranslation("BG_CLIENT_SERVER_NOT_FOUND", "§c404 Server not found .... ");
		registerTranslation("BG_GLIST", "§7On all server there is a total of §e{INPUT0} §7players online!");
		registerTranslation("BG_INTEGER", "§cThat is not a number!");
		registerTranslation("BG_NOT_PERMISSION_LVL", "§cYou do not have necessary permissions for this ban-level!");
		registerTranslation("BG_BAN_SCREEN", "§cYou were banned §4PERMANENTLY §cfrom the Network! \n§3Reason: §c{INPUT0} \n \n§aYou can write an unban-request at §ewww.EpicPvP.org");
		registerTranslation("BG_BAN_DISCONNECT", "§cYou were banned {INPUT0} §cfrom the Network! \n§3Reason: §c{INPUT1} \n \n§aYou can write an unban-request at §ewww.EpicPvP.org");
		registerTranslation("BG_BAN_MSG", "§e{INPUT0}§c was permanently banned by §e{INPUT1}§c. Reason:§e {INPUT2}");
		registerTranslation("BG_KICK_MSG", "§e{INPUT0}§c was kicked by §e{INPUT1}§c. Reason: §e{INPUT2}");
		registerTranslation("BG_KICK_SCREEN", "§cYou were kicked form the Network. \n§3Reason§8:§c {INPUT0}");
		registerTranslation("BG_INFO_TIME_BAN", "§cTime-banned:§e banned since {INPUT0} remaining {INPUT1} by {INPUT2}");
		registerTranslation("BG_INFO_TIME_BAN_GRUND", "§cReason:§e {INPUT0}");
		registerTranslation("BG_INFO_NOT_TIME_BANNED", "§cTime-banned:§e not time-banned");
		registerTranslation("BG_INFO_NOT_BANNED", "§cBanned:§e not banned");
		registerTranslation("BG_INFO_BAN", "§cBanned:§e banned since {INPUT0} by {INPUT1}");
		registerTranslation("BG_NICK_SET", "§aNickname §e{INPUT0}§a set!");
		registerTranslation("BG_NICK_DEL", "§aNickname removed!");
		registerTranslation("BG_PERFORMANCE", "§7BG: §b{INPUT0}§7 Online:§b{INPUT2} §7Player-Online:§b{INPUT3}§7 Avg-Ping:§b{INPUT4} §7TPS:§b{INPUT5} §7Ram:§b{INPUT6} MB");
		registerTranslation("BG_PREMIUM_MSG1", "§4§lWARNING! §cIf you proceed you will be unable to use a cracked minecraft version");
		registerTranslation("BG_PREMIUM_MSG2", "§7Please think carefully about this step and");
		registerTranslation("BG_PREMIUM_MSG3", "§7only continue if you bought the game at");
		registerTranslation("BG_PREMIUM_MSG4", "§eminecraft.net§7! §4You have been warned!");
		registerTranslation("BG_PREMIUM_MSG5", "§aProceed §7(§cat your own risk!§7): §e§l/premium on");
		registerTranslation("BG_PREMIUM_YOU_ARE_NOT", "§cYou are not a minecraft premium member!");
		registerTranslation("BG_PREMIUM_KICK_OFF", "§7Your §ePremium-login§7 was §cdeactivated§7! Try it!");
		registerTranslation("BG_PREMIUM_KICK_ON", "§7Your §ePremium-login§7 was §aactivated§7! Try it!");
		registerTranslation("BG_PREMIUM_ON", "§a{INPUT0}s §ePremium-login§a was activated!");
		registerTranslation("BG_PREMIUM_NOT", "§cThis player is not a minecraft premium member!");
		registerTranslation("BG_PREMIUM_OFF", "§c{INPUT0} §ePremium-login§c was deactivated!");
		registerTranslation("BG_PW_NOT", "§cThis is not your password {INPUT0}");
		registerTranslation("BG_PW_CHANGE", "§aYour password was changed to §e{INPUT0}");
		registerTranslation("BG_SERVER_NOT_EXIST", "§cServer does not exist!");
		registerTranslation("BG_PLAYER_SEND", "§aPlayer was send to the server");
		registerTranslation("BG_SEND_A", "§aA request was send to the BGs");
		registerTranslation("NO_PERMISSION", "§cNo permissions!");
		registerTranslation("BG_TS", "§aTeamSpeak: §7ts.EpicPvP.eu §8| §eWebsite: §7EpicPvP.eu");
		registerTranslation("BG_EVENT_SERVER", "§aCongratulations you joined the §eEvent-Server§a!");
		registerTranslation("BG_EVENT_NOT_NOW", "§cThere is no §eevent-round §cscheduled!");
		registerTranslation("BG_TEST_SERVER", "§aCongratulations you joined the §etest-round§a!");
		registerTranslation("BG_TEST_NOT_NOW", "§cThere is no §etest-round §cscheduled!");
		registerTranslation("BG_TEST_SERVER_INVITE", "§aYou were invited to a test-round by §e{INPUT0}§a!");
		registerTranslation("BG_TEST_SERVER_INVITE1", "§aTo accept: §e/testserver");
		registerTranslation("BG_TEST_SEND_INVITES", "§aPlayer invited!");
		registerTranslation("BG_TICKET_NOT_FOUND", "§cNo ticket found!");
		registerTranslation("BG_TICKET_PREFIX", "§eTicket §8§ §7");
		registerTranslation("BG_NO_TICKETS_OPEN", "§cThere are not any open tickets at the moment!");
		registerTranslation("BG_TICKET_CREATE", "§aTicket created!");
		registerTranslation("BG_TICKET_LAST_TICKET", "§cYour last ticket is still pending!");
		registerTranslation("BG_TICKET_REASON", "§a{INPUT0}§7 Reason: §e{INPUT1}");
		registerTranslation("BG_TICKET_SUFFIX", ":§7 ");
		registerTranslation("BG_TICKET_MSG1", "§a{INPUT0}§7 opend a new ticket!");
		registerTranslation("BG_TICKET_MSG2", "§7Ticket-reason:§c {INPUT0}");
		registerTranslation("BG_TICKET_MSG3", "§7Server:§e {INPUT0}");
		registerTranslation("BG_TICKET_MSG4", "§7BungeeCord-Instance:§e {INPUT0}");
		registerTranslation("BG_TICKET_ACCEPT", "§a{INPUT0}§7 is now working on §e{INPUT1}s§7 ticket!");
		registerTranslation("BG_TICKET_CLOSE", "§a{INPUT0}s§7 ticket closed!");
		registerTranslation("BG_UNBAN", "§e{INPUT0} §ais now unbanned!");
		registerTranslation("BG_WHEREIS", "§a{INPUT0}§7 is currently on server §e{INPUT1}§7 and BungeeCord §e{INPUT2}");
		registerTranslation("BG_WHO", "§7Player: §e{INPUT0} §7IP:§e {INPUT1}§7 UUID:§e {INPUT2}§7 Real-UUID:§e {INPUT3}");
		registerTranslation("BG_WHEREIS_SEARCH", "§e{INPUT0}§c is currently offline!");
		registerTranslation("BG_TIME_BAN_NAME", "§cYou have to enter a name!");
		registerTranslation("BG_TIME_BAN_TYP", "§e{INPUT0}§c is not a type");
		registerTranslation("BG_TIME_BAN_MAX_DAY", "§cYou can only set a max. temp-ban time of §e{INPUT0} §cdays!");
		registerTranslation("BG_TIME_BAN_CHAT", "§e{INPUT0}§c was banned by §e{INPUT1}§c for {INPUT2} {INPUT3} banned. Reason: §e{INPUT4}");
		registerTranslation("BG_TIME_BAN_KICK", "§cYou were banned for §e{INPUT0} {INPUT1} §cfrom the Network! \n§3Reason: §c{INPUT2} \n \n§aYou can write an unban-request at §ewww.EpicPvP.org");
		registerTranslation("TWITTER_NOT_FOLLOW", "§cYou have to follow @EpicPvPMC on Twitter to add your account!");
		registerTranslation("TWITTER_CHECK", "Check Twitter account...");
		registerTranslation("TWITTER_ADD", "§aYour Twitter account has been added!");
		registerTranslation("TWITTER_IS_ADDED", "You have already added this Twitter account!");
		registerTranslation("TWITTER_IS_USED", "§cThis Twitter account is already in use by another Player!");
		registerTranslation("TWITTER_FAIL", "§cPlease contact Kingingo with this message: §b{INPUT0}");
		registerTranslation("TWITTER_ACC", "Your Twitter account: §a{INPUT0}");
		registerTranslation("TWITTER_NOT_FIND", "§cYour Twitter account cannot find!");
		registerTranslation("HUB_MUTE", "§cYou are able to Chat here in §e{INPUT0}§c!");
		registerTranslation("BG_PLAYER_FIND", "§e{INPUT0}§a switched to §e{INPUT1}");
		registerTranslation("BG_EVENT_SERVER_WAIT", "§cToo many people try to connect. Please wait.");
	}
}
