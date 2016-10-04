package dev.wolveringer.bs.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class InvalidChatListener implements Listener {

	private static final BaseComponent[] kickReason = new ComponentBuilder("No!").color(ChatColor.RED).create(); //Don't give excessive details to an attacker

	@EventHandler
	public void onChat(ChatEvent event) {
		if (event.getSender() instanceof ProxiedPlayer) {
			// these two conditions are only met by bots, which do not wait until they are fully connected or simply send invalid data
			// when the server of a player is null, "real" players are still in the loading screen and can't chat
			if (((ProxiedPlayer) event.getSender()).getServer() == null || event.getMessage().trim().isEmpty()) {
				event.setCancelled(true);
				event.getSender().disconnect(kickReason);
			}
		}
	}
}

