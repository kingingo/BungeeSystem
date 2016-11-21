package eu.epicpvp.bungee.system.bs.servermanager;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.client.event.ServerMessageEvent;
import eu.epicpvp.bungee.system.mysql.MySQL;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerManager implements Listener {

	public static ServerInfo DEFAULT_HUB;

	private static ServerManager manager;

	public static ServerManager getManager() {
		return manager;
	}

	public static void setManager(ServerManager manager) {
		ServerManager.manager = manager;
	}

	private List<ServerInfo> servers = new ArrayList<>();
	private List<ServerInfo> lobbyServers = new ArrayList<>();
	private List<ServerInfo> loginServer = new ArrayList<>();
	private List<ServerInfo> premiumServer = new ArrayList<>();
	private int lobbyServerPos = 0;
	private int premiumLobbyServerPos = 0;
	private int loginServerPos = 0;

	public ServerManager() {
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
		DEFAULT_HUB = getOrCreateServerInfo("hub", "localhost", 1000);
	}

	public void loadServers() {
		ArrayList<String[]> server = MySQL.getInstance().querySync("SELECT name,adress,port FROM `BG_Server`", -1);
		Map<String, ServerInfo> servers = BungeeCord.getInstance().getServers();
		for (String[] s : server) {
			ServerInfo serverInfo = BungeeCord.getInstance().constructServerInfo(s[0], new InetSocketAddress(s[1], Integer.parseInt(s[2])), "error", false);
			this.servers.add(serverInfo);
			servers.put(s[0], serverInfo);
		}
		recalculateLobbies();
		System.out.println("Loaded Servers: " + this.servers.size() + " Lobbies: " + lobbyServers.size());
//		addServer("hub", "null", 1000);
	}

	private ServerInfo getOrCreateServerInfo(String name, String ip, int port) {
		for (ServerInfo server : new ArrayList<>(servers)) {
			String serverName = server.getName();
			if (serverName != null && serverName.equalsIgnoreCase(name)) {
				return server;
			}
		}
		ServerInfo info = BungeeCord.getInstance().constructServerInfo(name, new InetSocketAddress(ip, port), "error", false);
		BungeeCord.getInstance().getServers().put(name, info);
		servers.add(info);
		if (name.startsWith("lobby") || (name.startsWith("hub") && !name.equalsIgnoreCase("hub"))) {
			recalculateLobbies();
		}
		return info;
	}

	public boolean addServer(String name, String ip, int port) {
		if (!MySQL.getInstance().querySync("SELECT * FROM `BG_Server` WHERE `name`='" + name + "'", 1).isEmpty())
			return false;
		MySQL.getInstance().command("INSERT INTO `BG_Server`(`name`, `adress`, `port`) VALUES ('" + name + "','" + ip + "','" + port + "')");
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "servers", new DataBuffer().writeByte(0).writeString(name).writeString(ip).writeInt(port));
		getOrCreateServerInfo(name, ip, port);
		return true;
	}

	public boolean delServer(String name) {
		ServerInfo info = null;
		for (ServerInfo s : new ArrayList<>(servers))
			if (s.getName().equalsIgnoreCase(name)) {
				info = s;
				break;
			}
		if (info == null)
			return false;
		MySQL.getInstance().command("DELETE FROM `BG_Server` WHERE `name`='" + info.getName() + "'");
		servers.remove(info);
		BungeeCord.getInstance().getServers().remove(info.getName());
		if (name.startsWith("lobby") || (name.startsWith("hub") && !name.equalsIgnoreCase("hub"))) {
			recalculateLobbies();
		}
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "servers", new DataBuffer().writeByte(1).writeString(name));
		return true;
	}

	private void recalculateLobbies() {
		for (ServerInfo s : servers)
			if (s.getName().startsWith("lobby") || (s.getName().startsWith("hub") && !s.getName().equalsIgnoreCase("hub")))
				lobbyServers.add(s);

		loginServer.clear();
		for (ServerInfo s : servers)
			if (s.getName().startsWith("login"))
				loginServer.add(s);

		premiumServer.clear();
		for (ServerInfo s : servers)
			if (s.getName().startsWith("premium"))
				premiumServer.add(s);
	}

	public Queue<String> buildLoginQueue() {
		int size = loginServer.size();
		int pos = loginServerPos = (loginServerPos + 1) % size;

		LinkedList<String> result = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			result.add(loginServer.get((pos + i) % size).getName());
		}
		return result;
	}

	public Queue<String> buildPremiumQueue() {
		int size = premiumServer.size();
		int pos = premiumLobbyServerPos = (premiumLobbyServerPos + 1) % size;

		LinkedList<String> result = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			result.add(premiumServer.get((pos + i) % size).getName());
		}
		result.addAll(buildLoginQueue());
		return result;
	}

	public Queue<String> buildLobbyQueue() {
		int size = lobbyServers.size();
		int pos = lobbyServerPos = (lobbyServerPos + 1) % size;

		LinkedList<String> result = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			result.add(lobbyServers.get((pos + i) % size).getName());
		}
		return result;
	}

	public ServerInfo nextPremiumLobby() {
		if (premiumServer.isEmpty())
			return nextLobby();
		int size = premiumServer.size();
		int pos = premiumLobbyServerPos = (premiumLobbyServerPos + 1) % size;

		return premiumServer.get(pos);
	}

	public ServerInfo nextLobby() {
		if (lobbyServers.isEmpty()) {
			System.err.println("No lobby found");
			return null;
		}
		int size = lobbyServers.size();
		int pos = lobbyServerPos = (lobbyServerPos + 1) % size;

		return lobbyServers.get(pos);
	}

	public ServerInfo nextLoginLobby() {
		int size = lobbyServers.size();
		int pos = lobbyServerPos = (lobbyServerPos + 1) % size;

		return lobbyServers.get(pos);
	}

	@EventHandler
	public void onServerMessage(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("servers")) {
			byte action = e.getBuffer().readByte();
			if (action == 0) {
				getOrCreateServerInfo(e.getBuffer().readString(), e.getBuffer().readString(), e.getBuffer().readInt());
			} else if (action == 1) {
				String name = e.getBuffer().readString();
				delServer(name);
			}
		}
	}

	public boolean isLobbyServer(Server server) {
		return server.getInfo().getName().startsWith("hub") || server.getInfo().getName().startsWith("premium") || server.getInfo().getName().startsWith("login");
	}
}
