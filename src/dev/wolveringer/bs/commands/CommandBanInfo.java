package dev.wolveringer.bs.commands;

import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.ban.BanEntity;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandBanInfo extends Command{
	
	public CommandBanInfo() {
		super("baninfo");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_INFO,true)) return;
		
		if(args.length == 1){
			cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.status.loadingPlayer",cs)); 
			LoadedPlayer player = null;

			if (args[0].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
				player = Main.getDatenServer().getClient().getPlayerAndLoad(UUID.fromString(args[0]));
			else {
				if (args[0].length() > 16) {
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.error.toLongName",cs));
					return;
				}
				player = Main.getDatenServer().getClient().getPlayerAndLoad(args[0]);
			}
			BanEntity baned = player.getBanStats("system").getSync();
			cs.sendMessage("");
			cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.general",cs,player.getName()));
			if(baned.isActive())
				if(baned.isTempBanned()){
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.temporary",cs));
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.expire",cs,PlayerJoinListener.getDurationBreakdown(baned.getEnd()-System.currentTimeMillis())));
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.level",cs,baned.getLevel()));
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.banner",cs,baned.getBanner()));
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.reson",cs,baned.getReson()));
				}
				else
				{
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.permanent",cs));
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.level",cs,baned.getLevel()));
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.banner",cs,baned.getBanner()));
					cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.reson",cs,baned.getReson()));
				}
			else
				cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.notbanned",cs));
			return;
		}
		cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.help",cs));
	}
	
}
//command.baninfo.status.loadingPlayer - §aLoading player...
//command.baninfo.error.toLongName - §cPlayer name cant be longer than 16.
//command.baninfo.baninfo.general - §6Baninformations for the player §e%s0 [playername]
//command.baninfo.baninfo.temporary - §6This player is §ntemporary§6 banned from this network.
//command.baninfo.baninfo.expire - §6Ban expire in %s0 [time]
//command.baninfo.baninfo.level - §6Level: §e%s0 [level]
//command.baninfo.baninfo.banner - §6Banner: %s0 [player]
//command.baninfo.baninfo.reson - §6Reson: %s0 [reson]
//command.baninfo.baninfo.permanent - §cThis player is §npermernatly§c banned from the network.
//command.baninfo.baninfo.notbanned - §aThis player isnt banned ;)
//command.baninfo.help - §6/baninfo <Username/UUID>