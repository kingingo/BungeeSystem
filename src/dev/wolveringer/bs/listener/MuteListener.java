package dev.wolveringer.bs.listener;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import dev.wolveringer.bs.login.LoginManager;
import me.kingingo.kBungeeCord.Language.Language;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MuteListener implements Listener {
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
			if (time.containsKey(e.getSender())){
				if(e.getMessage().startsWith("/") && !LoginManager.getManager().isLoggedIn((ProxiedPlayer) e.getSender())){
					if(!(e.getMessage().startsWith("/login") || e.getMessage().startsWith("/register") || e.getMessage().startsWith("/captcha")))
						e.setCancelled(true);
					else
						return;
				}
				if (time.get(e.getSender()) > System.currentTimeMillis()) {
					if ((((ProxiedPlayer) e.getSender()).getServer().getInfo().getName().toLowerCase().contains("hub") || ((ProxiedPlayer) e.getSender()).getServer().getInfo().getName().toLowerCase().contains("lobby")) && !e.getMessage().startsWith("/")) {
						((ProxiedPlayer) e.getSender()).sendMessage(Language.getText(((ProxiedPlayer) e.getSender()), "PREFIX") + Language.getText(((ProxiedPlayer) e.getSender()), "HUB_MUTE", formatMili((time.get(e.getSender()) - System.currentTimeMillis()))));
						e.setCancelled(true);
					}
				} else
					time.remove(e.getSender());
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
