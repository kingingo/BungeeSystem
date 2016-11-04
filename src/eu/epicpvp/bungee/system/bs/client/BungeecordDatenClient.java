package eu.epicpvp.bungee.system.bs.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.dataserver.protocoll.packets.PacketOutServerStatus;
import eu.epicpvp.datenclient.client.ClientWrapper;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.client.connection.Client;
import eu.epicpvp.datenclient.client.debug.Debugger;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeecordDatenClient {
	private Client client;
	private ClientWrapper wclient;
	private int onlineCount = -2;
	private List<String> players;
	@Getter
	private Set<String> playersSet = new HashSet<>();

	private ScheduledTask infoUpdater;
	private boolean active = false;

	private String name;
	private SocketAddress target;

	private boolean tryConnecting = false;

	private ClientExternalHandler externalHandler = new ClientExternalHandler();
	private ClientInfoManager infoSender = new ClientInfoManager();
	private String password;

	public BungeecordDatenClient(String name, SocketAddress target) {
		super();
		this.name = name;
		this.target = target;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public ClientWrapper getClient() {
		return wclient;
	}

	public int getPlayerCount() {
		return onlineCount;
	}

	public void teamMessage(String message) {
		wclient.broadcastMessage(PermissionType.TEAM_MESSAGE.getPermissionToString(), message);
	}

	public boolean isConnecting() {
		return tryConnecting;
	}

	public void start() throws Exception {
		System.out.println("start() called. Active -> "+isActive()+". Is conencting: "+tryConnecting);
		while (tryConnecting) {
			Thread.sleep(10);
		}
		if (isActive())
			return;

		tryConnecting = true;
		if (client == null)
			client = Client.createBungeecordClient(name, (InetSocketAddress) target, externalHandler, infoSender);
		if (wclient == null)
			wclient = new ClientWrapper(client);
		try {
			client.connect(password.getBytes());
		} catch (Exception e) {
			throw e;
		} finally {
			tryConnecting = false;
		}
		if(Main.getGildeManager() != null)
			Main.getGildeManager().clear();
		active = true;
		client.getInfoSender().setSleepTime(1000);// Beschleunigt die aktualisierung der Spielerzahl.

		infoUpdater = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				int count = 0;
				while (isActive()) {
					try {
						count++;
						if (count % 3 == 0) { //Update player names only all 4.5 seconds!
							PacketOutServerStatus r = wclient.getServerStatus(PacketOutServerStatus.Action.GENERAL, null, true).getSync();
							onlineCount = r.getPlayer();
							List<String> playerList = r.getPlayers();
							if (playerList != null) {
								players = playerList;
								playersSet = new HashSet<>();
								for (String name : playerList) {
									playersSet.add(name.toLowerCase());
								}
							} else {
								Debugger.debug("Got a strange null for the playerlist");
							}
						} else {
							onlineCount = wclient.getServerStatus(PacketOutServerStatus.Action.GENERAL, null, false).getSync().getPlayer();
						}
					} catch (Exception e) {
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
		for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
			LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(player.getName());
			lplayer.setServerSync(player.getServer().getInfo().getName()); //Loading player
			lplayer.setIp(player.getPendingConnection().getAddress().getAddress().getHostAddress());
		}
		System.out.println("players loaded");
	}

	public boolean isActive() {
		return active && client.isConnected() && client.isHandshakeCompleted() && wclient != null;
	}

	public void stop() {
		active = false;
		infoUpdater.cancel();
	}

	public SocketAddress getAddress() {
		return target;
	}

	public List<String> getPlayers() {
		return players;
	}
}
