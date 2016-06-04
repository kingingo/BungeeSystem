package dev.wolveringer.bs.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.client.Callback;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.protocoll.packets.PacketVersion;
import dev.wolveringer.gamestats.Statistic;
import dev.wolveringer.hashmaps.CachedHashMap;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {
	private HashMap<ProxiedPlayer, Long> time = new HashMap<>();
	private CachedHashMap<ProxiedPlayer, String> lastTarget = new CachedHashMap<>(10, TimeUnit.SECONDS);
	private CachedHashMap<Player, Long> lastCommand = new CachedHashMap<>(750, TimeUnit.MILLISECONDS);
	
	@EventHandler
	public void a(PostLoginEvent e) {
		if (!e.getPlayer().getPendingConnection().isOnlineMode()){
			LoadedPlayer loadedplayer = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName());
			
			loadedplayer.getStats(GameType.TIME).getAsync(new Callback<Statistic[]>() {
				
				@Override
				public void call(Statistic[] obj,Throwable ex) {
					if(ex != null){
						ex.printStackTrace();
						loadedplayer.kickPlayer("§cError while loading stats");
					}
					int total = 0;
					
					for(Statistic s : obj){
						if(Statistic.types.get(s.getValue().getClass()) == 0){
							total += s.asInt();
						}
					}
					
					if(total < (15*60*1000)){
						time.put(e.getPlayer(), System.currentTimeMillis()+((15*60*1000)-total));
					}
				}
			});
			
		}
	}

	@EventHandler
	public void a(PlayerDisconnectEvent e) {
		time.remove(e.getPlayer());
	}

	@EventHandler
	public void a(ChatEvent e) {
		if (e.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) e.getSender();
			if(!Main.getDatenServer().isActive()){
				p.sendMessage("§cCant connect to §eServer-Chef§c. Protocoll version: §a" + PacketVersion.PROTOCOLL_VERSION + "\n§c(Bungeecord) -> §aPlease wait a littlebit.");
				e.setCancelled(true);
				return;
			}
			if(e.getMessage().startsWith("/") && System.currentTimeMillis()-lastCommand.getOrDefault(p, System.currentTimeMillis())>1 && !PermissionManager.getManager().hasPermission(p, "chat.delay.bypass")){
				p.sendMessage("§cDu chattest zu schnell. Bitte chatte etwas langsamer ;)");
				e.setCancelled(true);
				return;
			}
				
			if(e.getMessage().startsWith("/") && !LoginManager.getManager().isLoggedIn((ProxiedPlayer) e.getSender())){
				if(!(e.getMessage().toLowerCase().startsWith("/login") || e.getMessage().toLowerCase().startsWith("/register") || e.getMessage().toLowerCase().startsWith("/captcha")))
					e.setCancelled(true);
				else
					return;
			}

			if (time.containsKey(e.getSender())){
				if (time.get(e.getSender()) > System.currentTimeMillis()) {
					if ((((ProxiedPlayer) e.getSender()).getServer().getInfo().getName().toLowerCase().contains("hub") || ((ProxiedPlayer) e.getSender()).getServer().getInfo().getName().toLowerCase().contains("lobby")) && !e.getMessage().startsWith("/")) {
						//§cYou are able to Chat here in §e%s0§c!
						((ProxiedPlayer) e.getSender()).sendMessage(Main.getTranslationManager().translate("prefix", p)+Main.getTranslationManager().translate("event.chat.timewait", p,formatMili((time.get(e.getSender()) - System.currentTimeMillis()))));//formatMili((time.get(e.getSender()) - System.currentTimeMillis())
						e.setCancelled(true);
					}
				} else
					time.remove(e.getSender());
			}
			
			if(!e.isCancelled()){
				if(e.getMessage().startsWith("@")){
					e.setCancelled(true);
					String player = e.getMessage().split(" ")[0].substring(1);
					
					if(player.equalsIgnoreCase(p.getName())){
						p.sendMessage("§cYou cant write with yourself.\n\n§cIf you want write with yourself then you must add yourself on Whatsapp.\n§cThis server doesn't support even talks.\n§cYours truly §aWolverinDEV");
						return;
					}
					if(player.length()+2 > e.getMessage().length()){
						p.sendMessage("Please provide a message.");
						return;
					}
					String message = e.getMessage().substring(player.length()+2);
					if(player.isEmpty()){
						if(!lastTarget.containsKey(p)){
							p.sendMessage("§cPlease provide a player.");
							return;
						}
						player = lastTarget.get(p);
					}
					else
						lastTarget.put(p, player);
					
					if(message.isEmpty()){
						p.sendMessage("§cPlease provide a message.");
						return;
					}
					if(BungeeCord.getInstance().getPlayer(player) != null){
						BungeeCord.getInstance().getPlayer(player).sendMessage("§8[§6»§o "+p.getName()+"§8] §7"+message);
						p.sendMessage("§8[§6§o"+player+" §6»§8] §7"+message);
					}
					else{
						LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(player);
						if(target.getServer().getSync() != null){
							p.sendMessage("§8[§6§o"+player+" §6»§8] §7"+message);
							Main.getDatenServer().getClient().sendMessage(target.getPlayerId(), "§8[§o§6» "+p.getName()+"§8] §7"+message);
						}
						else
							p.sendMessage("§cTarget player isnt online.");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void a(TabCompleteEvent e){
		if(e.getCursor().startsWith("@")){
			if(e.getCursor().split(" ").length==1 && !e.getCursor().endsWith(" ")){
				String nameStart = e.getCursor().substring(1, e.getCursor().length());
				List<String> avariable = new ArrayList<>();
				for(String s : Main.getDatenServer().getPlayers())
					if(s.toLowerCase().startsWith(nameStart.toLowerCase()))
						avariable.add("@"+s);
				e.getSuggestions().clear();
				e.getSuggestions().addAll(avariable);
			}
		}
	}
	
	public static final long DAY = 86400000L;
	public static final long HOUR = 3600000L;
	public static final long MINUTE = 60000L;
	public static final long SECOND = 1000L;

	public static String formatMili(long milis) {
		if (milis > MINUTE) {

			if (milis > DAY) {
				int time = (int) (milis / DAY);
				if (milis - time * DAY > 1) {
					return time + "day " + formatMili(milis - time * DAY);
				}
				return time + "day";
			}

			if (milis > HOUR) {
				int time = (int) (milis / HOUR);
				if (milis - time * HOUR > 1) {
					return time + "h " + formatMili(milis - time * HOUR);
				}
				return time + "h";
			}

			int time = (int) (milis / MINUTE);
			if (milis - time * MINUTE > 1) {
				return time + "min " + formatMili(milis - time * MINUTE);
			}
			return time + "min";
		}

		return (int) (milis / SECOND) + "sec";
	}
}

//event.chat.timewait - §cYou are able to Chat here in §e%s0§c! [time]