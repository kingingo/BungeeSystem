package eu.epicpvp.bungee.system.bs.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.epicpvp.bungee.system.ban.BanServerMessageListener;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.datenclient.client.Callback;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.dataserver.ban.BanEntity;
import eu.epicpvp.datenserver.definitions.dataserver.player.Setting;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerSettings.SettingValue;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import dev.wolveringer.thread.ThreadFactory;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandTempBan extends Command {
	private static final HashMap<String, TimeUnit> timeConverts = new HashMap<>();
	static {
		timeConverts.put("S", TimeUnit.SECONDS);
		timeConverts.put("M", TimeUnit.MINUTES);
		timeConverts.put("H", TimeUnit.HOURS);
		timeConverts.put("D", TimeUnit.DAYS);
	}
	public CommandTempBan() {
		super("tempban", null, "zeitban");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if (!PermissionManager.getManager().hasPermission( cs, PermissionType.ZEITBAN, true))
			return; //basic permission

		if (args.length >= 4) {
			if (!isNumber(args[2])) {
				cs.sendMessage("§cBan-Level is not a number!");
				return;
			}
			int level = Integer.parseInt(args[2]);
			if (level < 1 || level > 5) {
				cs.sendMessage("§cBan-Level out of bounds! [1-5]");
				return;
			}
			switch (level) { //Ban-Level 1 alredy tested
			case 2:
				if (!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_LVL_2, true))
					return;
				break;
			case 3:
				if (!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_LVL_3, true))
					return;
				break;
			case 4:
				if (!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_LVL_4, true))
					return;
				break;
			case 5:
				if (!PermissionManager.getManager().hasPermission(cs, PermissionType.BAN_LVL_5, true))
					return;
				break;
			}

			long time = 0;
			for(String s : args[1].split(";")){
				String stime = "";
				String identifier = "";
				boolean timeActive = true;
				for(int i = 0;i<s.length();i++){
					if(timeActive)
						if(isNumber(s.charAt(i)))
							stime += new String(new char[]{s.charAt(i)});
						else
							timeActive = false;
					if(!timeActive)
						identifier += new String(new char[]{s.charAt(i)});
				}
				if(!timeConverts.containsKey(identifier.toUpperCase())){
					cs.sendMessage("§cCant find timeconverter for identifier '"+identifier+"'");
					return;
				}
				if(!isNumber(stime)){
					cs.sendMessage("§cTime '"+stime+"' isnt a number!");
					return;
				}
				time+=timeConverts.get(identifier.toUpperCase()).toMillis(Integer.parseInt(stime));
			}
			if(TimeUnit.DAYS.toMillis(14)<time){
				cs.sendMessage("§cToo long ban time. Maximal temp-time 14D");
				return;
			}

			String reson = ChatColor.translateAlternateColorCodes('&', join(Arrays.copyOfRange(args, 3, args.length), " "));

			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(args[0]);
			cs.sendMessage("§aChecking player online state.");
			List<BanEntity> bans = player.getBanStats("", 1).getSync();
			if(bans.size() > 0 && bans.get(0).isActive() && !PermissionManager.getManager().hasPermission(cs, "ban.overban",false)){
				cs.sendMessage("§cYou cant overban an Player!");
				return;
			}
			String server;
			String curruntIp = "undefined";
			if ((server = player.getServer().getSyncSave()) != null) {
				cs.sendMessage("§aPlayer is curruntly online at " + server);
				cs.sendMessage("§aLooking up IP-Address");
				SettingValue[] var = player.getSettings(Setting.CURRUNT_IP).getSync();
				if (var != null && var.length == 1)
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
			cs.sendMessage("§aBanning player and ip");
			player.banPlayer(curruntIp, cs.getName(), (cs instanceof ProxiedPlayer ? ((ProxiedPlayer) cs).getAddress().getHostName() : "console"), (cs instanceof ProxiedPlayer ? ((ProxiedPlayer) cs).getUniqueId() : UUID.nameUUIDFromBytes("console".getBytes())), level, System.currentTimeMillis()+time, reson);
			cs.sendMessage("§aPlayer §e" + player.getName() + " §ais now for "+getDurationBreakdown(time)+" banned!");
			Main.getDatenServer().getClient().broadcastMessage(PermissionType.ZEITBAN.getPermissionToString(), "§cThe player §e"+player.getName()+" §cwas banned for §a"+getDurationBreakdown(time)+" by §e"+cs.getName()+"§c!");
			Main.getDatenServer().getClient().broadcastMessage(PermissionType.ZEITBAN.getPermissionToString(), "§cReson: §6"+reson);
			return;
		}
		cs.sendMessage("§cUsage: §6/tempban <Player> <Time> <Level> <Reson>");
		cs.sendMessage("§cTime format: 1H;22D;11S");
		cs.sendMessage("§cAvariable Time Types: S-Seconds, M-Minutes, H-Houer, D-Days");
	}

	private String join(String[] copyOfRange, String string) {
		String out = "";
		for(String s : copyOfRange)
			out+=string+s;
		return out.substring(string.length());
	}

	public static boolean isNumber(char ch){
		return ch >= '0' && ch <= '9';
	}
	public static boolean isNumber(String in){
		for(char c : in.toCharArray())
			if(!isNumber(c))
				return false;
		return true;
	}

	public static String getDurationBreakdown(long millis) {
		if (millis < 0) {
			throw new IllegalArgumentException("Duration must be greater than zero!");
		}

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		StringBuilder sb = new StringBuilder(64);
		if (days > 0) {
			sb.append(days);
			sb.append(" day"+(days == 1 ? "":"s")+" ");
		}
		if (hours > 0) {
			sb.append(hours);
			sb.append(" hour"+(hours == 1 ? "":"s")+" ");
		}
		if (minutes > 0) {
			sb.append(minutes);
			sb.append(" minute"+(minutes == 1 ? "":"s")+" ");
		}
		if (seconds > 0) {
			sb.append(seconds);
			sb.append(" second"+(seconds == 1 ? "":"s")+"");
		}
		return (sb.toString());
	}

	public static void main(String[] args) {
		for(String s : "1D".split(";")){
			String stime = "";
			String identifier = "";
			boolean timeActive = true;
			for(int i = 0;i<s.length();i++){
				if(timeActive)
					if(isNumber(s.charAt(i))){
						stime += new String(new char[]{s.charAt(i)});
					}else{
						timeActive = false;
					}
				if(!timeActive)
					identifier += new String(new char[]{s.charAt(i)});
			}
			if(!timeConverts.containsKey(identifier)){
				System.out.println("§cCant find timeconverter for identifier '"+identifier+"'"+timeActive);
				return;
			}
			if(!isNumber(stime)){
				System.out.println("§cTime '"+stime+"' isnt a number!");
				return;
			}
			System.out.println("Time: "+timeConverts.get(identifier).toMillis(Integer.parseInt(stime)));
		}
	}
}
