package dev.wolveringer.bs.servermanager;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.mysql.MySQL;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerManager implements Listener{
	public static ServerInfo DEFAULT_HUB;
			
	private static class BungeecordServerInfo extends BungeeServerInfo {
		private String name;
		private InetSocketAddress addr;
		public BungeecordServerInfo(String name,String ip,int port) {
			this(name,new InetSocketAddress(ip, port));
		}
		public BungeecordServerInfo(String name,InetSocketAddress addr) {
			super(name, addr, "error", false);
			this.name = name;
			this.addr = addr;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((addr == null) ? 0 : addr.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			BungeecordServerInfo other = (BungeecordServerInfo) obj;
			if (addr == null) {
				if (other.addr != null)
					return false;
			} else if (!addr.equals(other.addr))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
	
	private static ServerManager manager;
	
	public static ServerManager getManager() {
		return manager;
	}
	public static void setManager(ServerManager manager) {
		ServerManager.manager = manager;
	}
	
	private ArrayList<BungeecordServerInfo> server = new ArrayList<>();
	private BungeecordServerInfo[] lobbies = new BungeecordServerInfo[0];
	private BungeecordServerInfo[] loginServer = new BungeecordServerInfo[0];
	private BungeecordServerInfo[] permiumServer = new BungeecordServerInfo[0];
	private int lobbyWitch = 0;
	private int plobbyWitch = 0;
	private int loginWitch = 0;
	
	public ServerManager() {
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
		DEFAULT_HUB = createServerInfo("hub", "localhost", 1000);
	}
	
	public void loadServers() {
		ArrayList<String[]> server = MySQL.getInstance().querySync("SELECT name,adress,port FROM `BG_Server`", -1);
		for(String[] s : server){
			BungeecordServerInfo info;
			this.server.add(info = new BungeecordServerInfo(s[0], new InetSocketAddress(s[1], Integer.parseInt(s[2]))));
			BungeeCord.getInstance().getServers().put(s[0], info);
		}
		recalculateLobbies();
		System.out.println("Loaded Servers: "+this.server.size()+" Lobbies: "+lobbies.length);
//		addServer("hub", "null", 1000);
	}

	private ServerInfo createServerInfo(String name,String ip,int port){
		BungeecordServerInfo info = new BungeecordServerInfo(name, ip, port);
		for(BungeecordServerInfo s : new ArrayList<>(server))
			if(s.name != null)
				if(s.name.equalsIgnoreCase(name)){
					return s;
				}
		BungeeCord.getInstance().getServers().put(name, info);
		server.add(info);
		if(name.startsWith("lobby") || (name.startsWith("hub") && !name.equalsIgnoreCase("hub"))){
			recalculateLobbies();
		}
		return info;
	}
	
	public boolean addServer(String name, String ip, int port) {
		if(MySQL.getInstance().querySync("SELECT * FROM `BG_Server` WHERE `name`='"+name+"'",1).size() > 0)
			return false;
		MySQL.getInstance().command("INSERT INTO `BG_Server`(`name`, `adress`, `port`) VALUES ('"+name+"','"+ip+"','"+port+"')");
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "servers", new DataBuffer().writeByte(0).writeString(name).writeString(ip).writeInt(port));
		createServerInfo(name, ip, port);
		return true;
	}
	
	public boolean delServer(String name) {
		BungeecordServerInfo info = null;
		for(BungeecordServerInfo s : new ArrayList<>(server))
			if(s.name.equalsIgnoreCase(name)){
				info = s;
				break;
			}
		if(info == null)
			return false;
		MySQL.getInstance().command("DELETE FROM `BG_Server` WHERE `name`='"+info.getName()+"'");
		server.remove(info);
		BungeeCord.getInstance().getServers().remove(info.getName());
		if(name.startsWith("lobby") || (name.startsWith("hub") && !name.equalsIgnoreCase("hub"))){
			recalculateLobbies();
		}
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "servers", new DataBuffer().writeByte(1).writeString(name));
		return true;
	}
	
	private void recalculateLobbies(){
		ArrayList<BungeecordServerInfo> l = new ArrayList<>();
		for(BungeecordServerInfo i : server)
			if(i.name.startsWith("lobby") || (i.name.startsWith("hub") && !i.name.equalsIgnoreCase("hub")))
				l.add(i);
		lobbies = l.toArray(new BungeecordServerInfo[0]);
		
		l.clear();
		for(BungeecordServerInfo i : server)
			if(i.name.startsWith("login"))
				l.add(i);
		loginServer = l.toArray(new BungeecordServerInfo[0]);
		
		l.clear();
		for(BungeecordServerInfo i : server)
			if(i.name.startsWith("premium"))
				l.add(i);
		permiumServer = l.toArray(new BungeecordServerInfo[0]);
	}
	
	public Queue<String> buildLoginQueue(){
		lobbyWitch = loginWitch++%loginServer.length;
		ArrayList<ServerInfo> i = new ArrayList<>();
		i.addAll(Arrays.asList(Arrays.copyOfRange(loginServer, loginWitch%loginServer.length, loginServer.length)));
		i.addAll(Arrays.asList(Arrays.copyOfRange(loginServer, 0, loginWitch%loginServer.length)));
		LinkedList<String> x = new LinkedList<>();
		for(ServerInfo y: i)
			x.add(y.getName());
		return x;
	}
	
	public Queue<String> buildPremiumQueue(){
		plobbyWitch = plobbyWitch++%permiumServer.length;
		ArrayList<ServerInfo> i = new ArrayList<>();
		i.addAll(Arrays.asList(Arrays.copyOfRange(permiumServer, plobbyWitch%permiumServer.length, permiumServer.length)));
		i.addAll(Arrays.asList(Arrays.copyOfRange(permiumServer, 0, plobbyWitch%permiumServer.length)));
		LinkedList<String> x = new LinkedList<>();
		for(ServerInfo y: i)
			x.add(y.getName());
		x.addAll(buildLobbyQueue());
		return x;
	}
	
	public Queue<String> buildLobbyQueue(){
		lobbyWitch = lobbyWitch++%lobbies.length;
		ArrayList<ServerInfo> i = new ArrayList<>();
		i.addAll(Arrays.asList(Arrays.copyOfRange(lobbies, lobbyWitch%lobbies.length, lobbies.length)));
		i.addAll(Arrays.asList(Arrays.copyOfRange(lobbies, 0, lobbyWitch%lobbies.length)));
		LinkedList<String> x = new LinkedList<>();
		for(ServerInfo y: i)
			x.add(y.getName());
		return x;
	}
	
	public ServerInfo nextPremiumLobby(){
		if(permiumServer.length == 0)
			return nextLobby();
		if(plobbyWitch>=permiumServer.length)
			plobbyWitch = 0;
		return permiumServer[plobbyWitch++%permiumServer.length];
	}
	
	public ServerInfo nextLobby(){
		if(lobbies.length == 0){
			System.out.println("No lobbies found");
			return null;
		}
		if(lobbyWitch>=lobbies.length)
			lobbyWitch = 0;
		return lobbies[lobbyWitch++%lobbies.length];
	}
	public ServerInfo nextLoginLobby(){
		if(loginWitch>=loginServer.length)
			loginWitch = 0;
		return loginServer[loginWitch++%loginServer.length];
	}
	
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("servers")){
			byte action = e.getBuffer().readByte();
			if(action == 0){
				BungeecordServerInfo info = new BungeecordServerInfo(e.getBuffer().readString(), e.getBuffer().readString(), e.getBuffer().readInt());
				addServer(info.getName(), info.getAddress().getHostName()+"", info.getAddress().getPort());
			}
			else if(action == 1){
				String name = e.getBuffer().readString();
				delServer(name);
			}
		}
	}
}
