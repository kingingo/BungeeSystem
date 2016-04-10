package dev.wolveringer.bs.client;

import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.external.BungeeCordActionListener;
import dev.wolveringer.dataserver.player.LanguageType;
import dev.wolveringer.dataserver.player.Setting;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ClientExternalHandler implements BungeeCordActionListener{

	@Override
	public void sendMessage(UUID player, String message) {
		System.out.println("Sendmessage: "+player+" Message: "+message);
		ProxiedPlayer p = BungeeCord.getInstance().getPlayer(player);
		if(p != null)
			p.sendMessage(message);
	}

	@Override
	public void brotcast(String permission, String message) {
		for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers()){
			if(permission == null || PermissionManager.getManager().hasPermission(player, permission, false))
				player.sendMessage(message);
		}
		BungeeCord.getInstance().getConsole().sendMessage("§7[Brotcast] §r"+message);
	}

	@Override
	public void kickPlayer(UUID player, String message) {
		ProxiedPlayer p = BungeeCord.getInstance().getPlayer(player);
		if(p != null)
			p.disconnect(message);
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
	public void sendPlayer(UUID player, String server) {
		ProxiedPlayer p = BungeeCord.getInstance().getPlayer(player);
		if(p != null){
			if(BungeeCord.getInstance().getServerInfo(server) != null)
				p.connect(BungeeCord.getInstance().getServerInfo(server));
		}
	}

	@Override
	public void settingUpdate(UUID player, Setting setting, String value) {
		if(setting == Setting.LANGUAGE)
			Language.updateLanguage(BungeeCord.getInstance().getPlayer(player), LanguageType.getLanguageFromName(value));
	}

}
