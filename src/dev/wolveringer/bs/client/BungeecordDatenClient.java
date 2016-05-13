package dev.wolveringer.bs.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.connection.Client;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeecordDatenClient {
	private Client client;
	private ClientWrapper wclient;
	private int onlineCount = -2;
	private List<String> players;
	
	private ScheduledTask infoUpdater;
	private boolean active = false;
	
	private String name;
	private SocketAddress target;
	
	private boolean tryConnecting = false;
	
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
		/*
		for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers())
			if(PermissionManager.getManager().hasPermission(player, PermissionType.TEAM_MESSAGE))
				player.sendMessage(message);
		*/
		wclient.brotcastMessage(PermissionType.TEAM_MESSAGE.getPermissionToString(), message);
	}
	
	
	public synchronized void start(String password) throws Exception {
		if(isActive())
			return;
		tryConnecting = true;
		if(client == null)
			client = Client.createBungeecordClient(name, (InetSocketAddress) target, new ClientExternalHandler(), new ClientInfoManager());
		if(wclient == null)
			wclient = new ClientWrapper(client);
		try{
			client.connect(password.getBytes());
		}finally {
			tryConnecting = false;
		}
		active = true;
		infoUpdater = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				int count = 0;
				while (isActive()) {
					try{
						count++;
						if(count%4 == 0){ //Update player names only all 6 seconds!
							PacketOutServerStatus r = wclient.getServerStatus(PacketOutServerStatus.Action.GENERAL, null, true).getSync();
							onlineCount =  r.getPlayer();
							players = r.getPlayers();
						}
						else
						{
							onlineCount = wclient.getServerStatus(PacketOutServerStatus.Action.GENERAL, null, false).getSync().getPlayer();
						}
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
		System.out.println("Loading players");
		for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers()){
			Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()).setServerSync(player.getServer().getInfo().getName());; //Loading player
		}
		System.out.println("players loaded");
	}
	
	public boolean isActive() {
		return active && client.isConnected() && client.isHandschakeCompleded() && wclient != null;
	}
	
	public void stop(){
		active = false;
		infoUpdater.cancel();
	}

	public SocketAddress getAddress() {
		return target;
	}
	
	public static void main(String[] args) {
		try{
			throw new RuntimeException();
		}finally{
			System.out.println("Runtime");
		}
	}
	
	public List<String> getPlayers() {
		return players;
	}
}
