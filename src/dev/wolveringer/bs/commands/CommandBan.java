package dev.wolveringer.bs.commands;

import java.util.Arrays;
import java.util.UUID;

import dev.wolveringer.ban.BanServerMessageListener;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.Callback;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.dataserver.player.Setting;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerSettings.SettingValue;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandBan extends Command{

	public CommandBan() {
		super("ban",null,"kban");
	}
	
	///ban <Player> <level> <reson>
	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!PermissionManager.getManager().hasPermission(cs, PermissionType.KBAN,true))return; //basic permission
		
		if(args.length >= 3){
			if(!isNumber(args[1])){
				cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.error.invalidNumber", cs)); 
			}
			int level = Integer.parseInt(args[1]);
			if(level < 1 || level > 5){
				cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.error.invalidRange", cs)); 
			}
			switch (level) { //Ban-Level 1 alredy tested
			case 2:
				if(!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_LVL_2,true))return;
				break;
			case 3:
				if(!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_LVL_3,true))return;
				break;
			case 4:
				if(!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_LVL_4,true))return;
				break;
			case 5:
				if(!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_LVL_5,true))return;
				break;
			}
			
			String reson = ChatColor.translateAlternateColorCodes('&', join(Arrays.copyOfRange(args, 2, args.length)," "));
			
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(args[0]);
			cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.status.loadingstats", cs));
			String server;
			String curruntIp = "undefined";
			if((server = player.getServer().getSyncSave()) != null){
				cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.status.playerOnline", cs,server));
				cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.status.loopupIp", cs));
				SettingValue[] var = player.getSettings(Setting.CURRUNT_IP).getSync();
				if(var != null && var.length == 1)
					curruntIp = var[0].getValue();
				ThreadFactory.getFactory().createThread(()->{
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					}
					BanServerMessageListener.getInstance().movePlayer(player, true).getAsync(new Callback<Boolean>() { //Kick/Move player
						@Override
						public void call(Boolean obj, Throwable exception) {
							if(exception != null || obj == false){
								if(BungeeCord.getInstance().getPlayer(player.getName()) != null)
									BungeeCord.getInstance().getPlayer(player.getName()).disconnect(Main.getTranslationManager().translate("command.ban.kickplayer", cs,reson,cs.getName())); 
								else
									player.kickPlayer(Main.getTranslationManager().translate("command.ban.kickplayer", cs,reson,cs.getName()));
							}
						}
					},2000);
				}).start();
			}
			cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.banningPlayerIp", cs,curruntIp));
			player.banPlayer(curruntIp, cs.getName(), (cs instanceof ProxiedPlayer ? ((ProxiedPlayer)cs).getAddress().getHostName() : "console"), (cs instanceof ProxiedPlayer ? ((ProxiedPlayer)cs).getUniqueId() : UUID.nameUUIDFromBytes("console".getBytes())), level, -1, reson);
			cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.playerBannd", cs,player.getName()));
			Main.getDatenServer().getClient().broadcastMessage(PermissionType.KBAN.getPermissionToString(), Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.breadcast.informations", cs,player.getName(),cs.getName())).getSync();
			Main.getDatenServer().getClient().broadcastMessage(PermissionType.KBAN.getPermissionToString(), Main.getTranslationManager().translate("prefix",cs)+Main.getTranslationManager().translate("command.ban.breadcast.reson", cs,reson)).getSync();
			return;
		}
		cs.sendMessage(Main.getTranslationManager().translate("prefix",cs)+"§cUsage: §6/ban <Player> <Level> <Reson>");
	}
	
	private String join(String[] copyOfRange, String string) {
		String out = "";
		for(String s : copyOfRange)
			out+=string+s;
		return out.substring(string.length());
	}

	public boolean isNumber(char ch){
		return ch >= '0' && ch <= '9';
	}
	public boolean isNumber(String in){
		for(char c : in.toCharArray())
			if(!isNumber(c))
				return false;
		return true;
	}
	
}
//command.ban.error.invalidNumber - §cBan-Level isn't a number!
//command.ban.error.invalidRange - §cBan-Level out of bounds! [1-5]
//command.ban.status.loadingstats - "§aChecking player online state."
//command.ban.status.playerOnline - §aPlayer is curruntly online at %s0 - [Servername]
//command.ban.status.loopupIp -§aLooking up IP-Address
//command.ban.kickplayer - §cYou are banned! [reson,banner-name]
//command.ban.banningPlayerIp - §aBanning player and ip [playerIp]
//command.ban.playerBannd - §aPlayer §e%s0 §ais now banned! [playername]
//command.ban.breadcast.informations - §cThe player §e%s0 §cwas permanently banned by §e%s1§c! [bannedPlayer,banner]
//command.ban.breadcast.reson - §cReson: §6%s0 [reson]