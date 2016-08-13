package dev.wolveringer.bs.commands;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.dataserver.player.LanguageType;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.permission.PermissionManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

public class CommandPwChange extends Command {

	static {
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.pwchange.help.other", "§7/pwchange set <user> <password>");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.pwchange.changeing", "§aChaning password for user %s0");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.pwchange.changed", "§aPassword for user %s0 changed to %s1");
	}
	
	public CommandPwChange(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 3 && PermissionManager.getManager().hasPermission(sender, "command.pwchange.other") && args[0].equalsIgnoreCase("set")){
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.changeing", sender,player.getName()));
			String newpw = args[2];
			String newpwHashed = hashPassword(newpw, player.getName());
			if (newpwHashed == null) {
				sender.sendMessage("§cAn error occurred while trying to set the password of the player. Please report this issue together with the code 1 and the current time.");
				return;
			}
			player.setPasswordSync(newpw);
			player.setStats(new PacketInStatsEdit.EditStats(GameType.WARZ, PacketInStatsEdit.Action.SET, StatsKey.KILLS, 1));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.changed", sender,player.getName(), newpw));
			return;
		}
		if(args.length == 2){
			if(!(sender instanceof Player)){
				sender.sendMessage("§cPlayer only");
				return;
			}
			String oldpw = args[0];
			String newpw = args[1];
			
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(((ProxiedPlayer)sender).getUniqueId());
			if(((ProxiedPlayer)sender).getPendingConnection().isOnlineMode()){
				sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.error.premium", sender));
				return;
			}
			String oldpwHashed = hashPassword(oldpw, player.getName());
			if (oldpwHashed == null) {
				sender.sendMessage("§cAn error occurred while trying to set your password. Please report this issue together with the code 1 and the current time.");
				return;
			}
			String oldSavedPw = player.getPasswordSync();
			if(!oldpw.equals(oldSavedPw) && !oldpwHashed.equals(oldSavedPw)){
				sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.error.oldNotMatch", sender));
				return;
			}
			String newpwHashed = hashPassword(newpw, player.getName());
			if (newpwHashed == null) {
				sender.sendMessage("§cAn error occurred while trying to set your password. Please report this issue together with the code 2 and the current time.");
				return;
			}
			player.setPasswordSync(newpwHashed);
			player.setStats(new PacketInStatsEdit.EditStats(GameType.WARZ, PacketInStatsEdit.Action.SET, StatsKey.KILLS, 1));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.changed", sender));
			return;
		}
		
		if(args.length != 2 && args.length != 3){
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.help", sender));
			if(PermissionManager.getManager().hasPermission(sender, "command.pwchange.other"))
				sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.help.other", sender));
		}
	}

	/**
	 * Salts the password with the username, then hashes the utf8 bytes of the string
	 * @param password the password to hash. its lowercased automatically
	 * @param username the username to hash. its lowercased automatically
	 * @return the hashed password with a § sign infront to make it impossible that anyone can type a hashed password
	 */
	public static String hashPassword(String password, String username) {
		password = password.toLowerCase();
		username = username.toLowerCase();
		String toDigest = password + username;
		try {
			MessageDigest md = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256); //Using this field instead of a string directly because I hate string constants not in fields
			byte[] digest = md.digest(toDigest.getBytes(StandardCharsets.UTF_8));
			return "§" + String.format("%064x", new BigInteger(1, digest)); //stackoverflow says this puts the bytes into the known string representation of sha256
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
//command.pwchange.help - /pwchange §7[§4Altes Password§7] §7[§aNeues Password§7]
//command.pwchange.error.premium - §cYou are on premium. You cant change your password!
//command.pwchange.error.oldNotMatch - §cOld password dont match!
//command.pwchange.changed - §aYou changed your password.