package dev.wolveringer.bs.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.connection.Client;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeecordDatenClient {
	private Client client;
	private ClientWrapper wclient;
	private int onlineCount = -2;
	
	private ScheduledTask infoUpdater;
	private boolean active = false;
	
	private String name;
	private SocketAddress target;
	
	public BungeecordDatenClient(String name, SocketAddress target) {
		super();
		this.name = name;
		this.target = target;
	}

	public ClientWrapper getClient() {
		return wclient;
	}
	
	public int getPlayerCount(){
		return onlineCount;
	}
	
	public void teamMessage(String message){
		for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers())
			if(PermissionManager.getManager().hasPermission(player, PermissionType.TEAM_MESSAGE))
				player.sendMessage(message);
		wclient.brotcastMessage(PermissionType.TEAM_MESSAGE.getPermissionToString(), message);
	}
	
	
	public void start(String password) throws Exception{
		client = Client.createBungeecordClient(name, (InetSocketAddress) target, new ClientExternalHandler(), new ClientInfoManager());
		client.connect(password.getBytes());
		wclient = new ClientWrapper(client);
		active = true;
		infoUpdater = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				while (active && client.isConnected()) {
					try{
						onlineCount = wclient.getServerStatus(PacketOutServerStatus.Action.GENERAL, null, false).getSync().getPlayer();
					}catch(Exception e){
						e.printStackTrace();
					}
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				}
			}
		});
	}
	
	public boolean isActive() {
		return active && client.isConnected();
	}
	
	public void stop(){
		active = false;
		infoUpdater.cancel();
	}

	public SocketAddress getAddress() {
		return target;
	}
}
