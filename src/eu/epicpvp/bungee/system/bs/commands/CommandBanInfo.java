package eu.epicpvp.bungee.system.bs.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.listener.PlayerJoinListener;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.dataserver.ban.BanEntity;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandBanInfo extends Command{
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm");
	static {
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.baninfo.history.tempban", "§7[§b%s5§7] §aBaned from §e%s0 §afor §e%s1§a with level §e%s2 §areason: §e%s4");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.baninfo.history.ban", "§7[§b%s4§7] §aBaned from §e%s0§a with level §e%s1 §areason: §e%s3");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.baninfo.history.unban.buy", "§7[§b%s0§7] §aBought unban.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.baninfo.history.unban.player", "§7[§b%s0§7] §aUnbaned by §e%s1§a.");
	}

	private static final int DEFAULT_DEEP = 5;
	public CommandBanInfo() {
		super("baninfo");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_INFO,true)) return;

		if(args.length >= 1){
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
			List<BanEntity> entries = player.getBanStats("system", args.length >= 2 ? Integer.parseInt(args[1]) : DEFAULT_DEEP).getSync();
			cs.sendMessage("");
			cs.sendMessage("");
			cs.sendMessage("");
			cs.sendMessage("");
			cs.sendMessage("");
			cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.general",cs,player.getName()));
			boolean skipFirst = false;
			if(entries.size() > 0 && entries.get(0).isActive()){
				skipFirst = true;
				BanEntity baned = entries.get(0);
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
			}
			else
				cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.baninfo.baninfo.notbanned",cs));
			cs.sendMessage("");
			cs.sendMessage("§7--------------------[ §8Histoy §7]--------------------");
			if(entries.size() > (skipFirst ? 1 : 0))
				for(BanEntity e : entries.subList(skipFirst ? 1 : 0, entries.size()))
					if(e.getLevel() >= -1)
						if(e.isTempBanned())
							cs.sendMessage(Main.getTranslationManager().translate("command.baninfo.history.tempban", cs, e.getBanner(), PlayerJoinListener.getDurationBreakdown(e.getEnd()-e.getDate()), e.getLevel(), e.getIp(), e.getReson(),DATE_FORMAT.format(new Date(e.getDate()))));
						else
							cs.sendMessage(Main.getTranslationManager().translate("command.baninfo.history.ban", cs, e.getBanner(), e.getLevel(), e.getIp(), e.getReson(),DATE_FORMAT.format(new Date(e.getDate()))));
					else
						if(e.getLevel() == -2)
						cs.sendMessage(Main.getTranslationManager().translate("command.baninfo.history.unban.player", cs,DATE_FORMAT.format(new Date(e.getDate())),e.getReson()));
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
