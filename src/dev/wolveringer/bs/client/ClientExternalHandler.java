package dev.wolveringer.bs.client;

import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.UtilBungeeCord;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.client.external.BungeeCordActionListener;
import dev.wolveringer.dataserver.player.Setting;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ClientExternalHandler implements BungeeCordActionListener{

	@Override
	public void sendMessage(int player, String message) {
		System.out.println("Sendmessage: "+player+" Message: "+message);
		LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(player);
		ProxiedPlayer p = BungeeCord.getInstance().getPlayer(lp.getName());
		if(p != null)
			p.sendMessage(message);
	}

	@Override
	public void brotcast(String permission, String message) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers()){
			if(permission == null || PermissionManager.getManager().hasPermission(player, permission, false))
				player.sendMessage(message);
		}
		BungeeCord.getInstance().getConsole().sendMessage("§7[Brotcast] §r"+message);
	}

	@Override
	public void kickPlayer(int player, String message) {
		LoadedPlayer p = Main.getDatenServer().getClient().getPlayerAndLoad(player);
		if(p != null){
			BungeeCord.getInstance().getPlayer(p.getName()).disconnect(message);
		}
	}

	@Override
	public void disconnected() {
		System.err.println("Client disconnected!");
	}

	@Override
	public void connected() {
		System.out.println("Client connected");
	}

	@Override
	public void serverMessage(String channel, DataBuffer buffer) {
		BungeeCord.getInstance().getPluginManager().callEvent(new ServerMessageEvent(channel, buffer));
	}

	@Override
	public void sendPlayer(int player, String server) {
		LoadedPlayer p = Main.getDatenServer().getClient().getPlayer(player);
		if(p != null && p.isLoaded()){
			if(BungeeCord.getInstance().getServerInfo(server) != null)
				BungeeCord.getInstance().getPlayer(p.getName()).connect(BungeeCord.getInstance().getServerInfo(server));
		}
	}

	@Override
	public void settingUpdate(UUID player, Setting setting, String value) {
		if(setting == Setting.LANGUAGE)
			Main.getTranslationManager().updateLanguage(Main.getDatenServer().getClient().getPlayerAndLoad(player));
	}

	@Override
	public void restart(String kickMessage) {
		for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers())
			p.disconnect(kickMessage);
		UtilBungeeCord.restart();
	}
	@Override
	public void stop(String kickMessage) {
		for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers())
			p.disconnect(kickMessage);
		BungeeCord.getInstance().stop();
	}
}
