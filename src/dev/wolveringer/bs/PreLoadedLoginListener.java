package dev.wolveringer.bs;

import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PreLoadedLoginListener implements Listener{
	@EventHandler
	public void a(PreLoginEvent e) {
		if (!Main.loaded) {
			e.setCancelled(true);
			e.setCancelReason("§cBungeecord isnt fully loaded");
		}
	}
}
