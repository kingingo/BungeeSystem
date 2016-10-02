package dev.wolveringer.bs.commands;

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
			player.setPasswordSync(args[2]);
			player.setStats(new PacketInStatsEdit.EditStats(GameType.WARZ, PacketInStatsEdit.Action.SET, StatsKey.KILLS, 1));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.changed", sender,player.getName(),args[2]));
			return;
		}
		if(args.length == 2){
			if(!(sender instanceof Player)){
				sender.sendMessage("§cPlayer only");
				return;
			}
			
			String old = args[0];
			String newpw = args[1];
			
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(((ProxiedPlayer)sender).getUniqueId());
			if(((ProxiedPlayer)sender).getPendingConnection().isOnlineMode()){
				sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.error.premium", sender));
				return;
			}
			if(!old.equals(player.getPasswordSync())){
				((ProxiedPlayer)sender).sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.error.oldNotMatch", sender));
				return;
			}
			player.setPasswordSync(newpw);
			((ProxiedPlayer)sender).sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.changed", sender));
			return;
		}
		
		if(args.length != 2 && args.length != 3){
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.help", sender));
			if(PermissionManager.getManager().hasPermission(sender, "command.pwchange.other"))
				sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.help.other", sender));
		}
	}

}
//command.pwchange.help - /pwchange §7[§4Altes Password§7] §7[§aNeues Password§7]
//command.pwchange.error.premium - §cYou are on premium. You cant change your password!
//command.pwchange.error.oldNotMatch - §cOld password dont match!
//command.pwchange.changed - §aYou changed your password.