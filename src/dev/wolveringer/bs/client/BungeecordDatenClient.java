package dev.wolveringer.bs.client;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.connection.Client;
import dev.wolveringer.dataclient.protocoll.packets.PacketInServerStatus.Action;
import lombok.AccessLevel;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.scheduler.BungeeTask;

public class BungeecordDatenClient {
	private Client client;
	private ClientWrapper wclient;
	private int onlineCount = -2;
	
	private ScheduledTask infoUpdater;
	private boolean active = false;
	public ClientWrapper getClient() {
		return wclient;
	}
	
	public int getPlayerCount(){
		return onlineCount; //TODO get
	}
	
	public void teamMessage(String message){
		for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers())
			if(PermissionManager.getManager().hasPermission(player, PermissionType.TEAM_MESSAGE))
				player.sendMessage(message);
		wclient.brotcastMessage(PermissionType.TEAM_MESSAGE.getPermissionToString(), message);
	}
	
	
	public void start(){
		active = true;
		infoUpdater = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				while (active && client.isConnected()) {
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
					onlineCount = wclient.getServerStatus(Action.GENERAL, null, false).getSync().getPlayer();
				}
			}
		});
	}
	public void stop(){
		active = false;
		infoUpdater.cancel();
	}
}
