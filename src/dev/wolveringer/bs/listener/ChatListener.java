package dev.wolveringer.bs.listener;

import java.util.HashMap;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.client.LoadedPlayer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {
	private HashMap<ProxiedPlayer, Long> time = new HashMap<>();

	@EventHandler
	public void a(PostLoginEvent e) {
		if (!e.getPlayer().getPendingConnection().isOnlineMode())
			time.put(e.getPlayer(), System.currentTimeMillis()+15*60*1000);
	}

	@EventHandler
	public void a(PlayerDisconnectEvent e) {
		time.remove(e.getPlayer());
	}

	@EventHandler
	public void a(ChatEvent e) {
		if (e.getSender() instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) e.getSender();
			if (time.containsKey(e.getSender())){
				if(e.getMessage().startsWith("/") && !LoginManager.getManager().isLoggedIn((ProxiedPlayer) e.getSender())){
					if(!(e.getMessage().startsWith("/login") || e.getMessage().startsWith("/register") || e.getMessage().startsWith("/captcha")))
						e.setCancelled(true);
					else
						return;
				}
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
					if(e.getMessage().split(" ").length < 2){
						p.sendMessage("§cPlease write a message.");
						return;
					}
					
					String player = e.getMessage().split(" ")[0].substring(1);
					
					if(player.equalsIgnoreCase(p.getName())){
						p.sendMessage("§cAre you so alone? You write with yourself....");
						return;
					}
					
					String message = e.getMessage().substring(player.length()+2);
					
					if(BungeeCord.getInstance().getPlayer(player) != null){
						BungeeCord.getInstance().getPlayer(player).sendMessage("§8[§6»§o "+p.getName()+"§8] §7"+message);
						p.sendMessage("§8[§6§o"+player+" §6»§8] §7"+message);
					}
					else{
						LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(player);
						if(target.getServer().getSync() != null){
							p.sendMessage("§8[§6§o"+player+" §6»§8] §7"+message);
							Main.getDatenServer().getClient().sendMessage(target.getUUID(), "§8[§o§6» "+p.getName()+"§8] §7"+message);
						}
						else
							p.sendMessage("§cTarget player isnt online.");
					}
				}
			}
		}
	}
	
	
	private String join(String[] copyOfRange, String string) {
		String out = "";
		for(String s : copyOfRange)
			out+=string+s;
		return out.substring(string.length());
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