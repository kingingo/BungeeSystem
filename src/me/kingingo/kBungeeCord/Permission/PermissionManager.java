package me.kingingo.kBungeeCord.Permission;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.mysql.MySQL;
import me.kingingo.kBungeeCord.Language.Language;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PermissionManager implements Listener {
	private static PermissionManager manager;

	public static PermissionManager getManager() {
		return manager;
	}

	public static void setManager(PermissionManager manager) {
		PermissionManager.manager = manager;
	}

	private ArrayList<Group> groups = new ArrayList<>();
	private HashMap<Integer, PermissionPlayer> user = new HashMap<>();

	public PermissionManager() {
		if(BungeeCord.getInstance() != null){ //Testing Bungee=Null
			BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
			BungeeCord.getInstance().registerChannel("permission");
		}
	}
	
	public void loadGroups(){
		ArrayList<String[]> qurry = MySQL.getInstance().querySync("SELECT DISTINCT `pgroup` FROM `game_perm` WHERE `pgroup`!='none' AND `playerId`='-2' ",-1);
		long start = System.currentTimeMillis();
		System.out.println("Loading permission groups...");
		for(String[] var : qurry)
			groups.add(new Group(this,var[0]));
		for(Group g : groups)
			g.initPerms();
		System.out.println("Done ("+(System.currentTimeMillis()-start)+")");
	}

	public void loadPlayer(Integer player) {
		if (!user.containsKey(player))
			user.put(player, new PermissionPlayer(this, player));
	}
	
	public void loadPlayer(UUID player) {
		LoadedPlayer p = Main.getDatenServer().getClient().getPlayerAndLoad(player);
		loadPlayer(p.getPlayerId());
	}

	public PermissionPlayer getPlayer(Integer player) {
		if(user.get(player) == null)
			loadPlayer(player);
		return user.get(player);
	}
	
	public PermissionPlayer getPlayer(UUID player) {
		LoadedPlayer p = Main.getDatenServer().getClient().getPlayerAndLoad(player);
		return getPlayer(p.getPlayerId());
	}

	public boolean hasPermission(ProxiedPlayer player, String permission) {
		return hasPermission(player.getUniqueId(), permission);
	}
	public boolean hasPermission(ProxiedPlayer player, PermissionType teamMessage) {
		return hasPermission(player.getUniqueId(), teamMessage.getPermissionToString());
	}
	public boolean hasPermission(ProxiedPlayer player, PermissionType teamMessage,boolean message) {
		return hasPermission(player, teamMessage.getPermissionToString(), message);
	}
	public boolean hasPermission(CommandSender player, PermissionType teamMessage,boolean message) {
		if(player instanceof ProxiedPlayer)
			return hasPermission((ProxiedPlayer)player, teamMessage.getPermissionToString(), message);
		return true;
	}
	
	public boolean hasPermission(ProxiedPlayer player, String permission, boolean message) {
		if (!LoginManager.getManager().isLoggedIn(player))
			return false; //Not logged in Player cant have perms
		boolean perm = hasPermission(player.getUniqueId(), permission);
		if (message && !perm) 
			player.sendMessage(Language.getText(player, "PREFIX") + "§cYou don't have permission to do that.");
		return perm;
	}

	public boolean hasPermission(UUID uuid, PermissionType permission) {
		return hasPermission(uuid, permission.getPermissionToString());
	}

	public boolean hasPermission(UUID uuid, String permission) {
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(uuid);
		return hasPermission(player.getPlayerId(), permission);
	}
	
	public boolean hasPermission(Integer playerId, String permission) {
		if(!user.containsKey(playerId))
			user.put(playerId, new PermissionPlayer(this, playerId));
		return user.get(playerId).hasPermission(permission);
	}

	public Group getGroup(String name) {
		for(Group g : groups)
			if(g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public void addGroup(String name){
		for(Group g : groups)
			if(g.getName().equalsIgnoreCase(name))
				return;
		groups.add(new Group(this,name));
	}
	public void removeGroup(String name){
		Group gg = null;
		for(Group g : groups)
			if(g.getName().equalsIgnoreCase(name))
				gg = g;
		if(gg == null)
			return;
		groups.remove(gg);
		gg.delete();
	}
	
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("permission")){
			byte action = e.getBuffer().readByte();
			if(action == 0){ //Remove cached player
				user.remove(e.getBuffer().readUUID());
			}
			else if(action == 1){
				String group = e.getBuffer().readString();
				for(Group g : groups)
					if(g.getName().equalsIgnoreCase(group)){
						System.out.println("Reload permission group: "+g.getName());
						g.reload();
					}
			}
		}
	}
	
	protected void updatePlayer(int player){
		Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "permission", new DataBuffer().writeByte(0).writeInt(player));
	}
	protected void updateGroup(Group group){
		Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "permission", new DataBuffer().writeByte(1).writeString(group.getName()));
	}
	
	@EventHandler
	public void a(PluginMessageEvent e){
		if(e.getSender() instanceof ProxiedPlayer && !e.getTag().startsWith("MC") && !e.getTag().startsWith("WECUI") && !e.getTag().startsWith("bungeecord")){
			e.setCancelled(true);
			System.out.print("Player "+((ProxiedPlayer)e.getSender()).getName()+" try to send a plugin message on Channel: "+e.getTag());
			return;
		}
 		if(e.getTag().equalsIgnoreCase("permission")){
			try{
				DataInputStream stream = new DataInputStream(new ByteArrayInputStream(e.getData()));
				//Aufbau ([UUID (Packet UUID)] [INTEGER (Action)] [Data (variable length)])
				DataBuffer buffer = new DataBuffer(stream);
				UUID packetUUID = buffer.readUUID();
				
				ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
				int action = buffer.readByte();
				if(action == 0){ //INFO Get player permissions ([UUID (player)])
					PermissionPlayer p = getPlayer(buffer.readInt());
					if(p == null)
						sendToBukkit(packetUUID,new DataBuffer().writeInt(-1).writeString("Player not found"), player.getServer().getInfo()); //Response (Player not found) [UUID (packet)] [INT -1] [STRING reson]
					else{
						DataBuffer out = new DataBuffer();
						out.writeInt(p.getGroups().size());
						for(Group group : p.getGroups()){
							out.writeString(group.getName());
						}
						out.writeInt(p.getPermissions().size());
						for(Permission perm : p.getPermissions()){
							out.writeString(perm.getPermission());
							out.writeByte(perm.getGroup().ordinal());
						}
						sendToBukkit(packetUUID, out, player.getServer().getInfo()); //Response (Permissions) [UUID (packet)] [INT Group-Length] [STRING[] groups] [INT perms-Length] [STRING[] perms]
					}
					
				}
				else if(action == 1){//INFO Get group permissions ([String (group)])
					String group = buffer.readString();
					Group g = getGroup(group);
					if(g == null)
						sendToBukkit(packetUUID,new DataBuffer().writeInt(-1).writeString("Group not found"), player.getServer().getInfo()); //Response (Group not found) [UUID (packet)] [INT -1] [STRING reson]
					else
					{
						DataBuffer out = new DataBuffer();
						ArrayList<Permission> permissions = g.getPermissionsDeep();
						out.writeInt(permissions.size());
						
						for(Permission perm : permissions){
							if(perm == null || perm.getGroup() == null){
								System.err.println("Permissiongroupo for: "+perm+" is null!");
								out.writeString("anUndefinedPermissionThankAnNullPointerException").writeByte(GroupTyp.ALL.ordinal());
								continue;
							}
							out.writeString(perm.getPermission());
							out.writeByte(perm.getGroup().ordinal());
						}
						out.writeString(g.getPrefix());
						sendToBukkit(packetUUID, out, player.getServer().getInfo()); //Response (Permissions) [UUID (packet)] [INT perms-Length] [STRING[] perms] [STRING name]
					}
				}
				else if(action == 2){//Addgroup <long[2] UUID> <string Group> <byte Grouptype>
					Integer target = buffer.readInt();
					PermissionPlayer p = getPlayer(target);
					if(p == null)
						sendToBukkit(packetUUID,new DataBuffer().writeInt(-1).writeString("Player not found"), player.getServer().getInfo()); //Response (Player not found) [UUID (packet)] [INT -1] [STRING reson]
					else
						p.addPermission(buffer.readString(),GroupTyp.values()[buffer.readByte()]);
					sendToBukkit(packetUUID, new DataBuffer(), player.getServer().getInfo());
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	public void sendToBukkit(UUID uuid, DataBuffer data, ServerInfo server) {
		DataBuffer buffer = new DataBuffer();
		buffer.writeUUID(uuid);

		byte[] cbuffer = new byte[data.writerIndex()];
		System.arraycopy(data.array(), 0, cbuffer, 0, data.writerIndex());
		buffer.writeBytes(cbuffer);
		data.release();
		
		byte[] bbuffer = new byte[buffer.writerIndex()];
		System.arraycopy(buffer.array(), 0, bbuffer, 0, buffer.writerIndex());
		server.sendData("permission", bbuffer);
		buffer.release();
	}
	
	public static void main(String[] args) {
		MySQL.setInstance(new MySQL("148.251.143.2", "3306", "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT"));
		PermissionManager manager = new PermissionManager();
		manager.loadGroups();

		//manager.addGroup("tdev");
		//manager.getGroup("tdev").setPrefix("��9Test-Dev ��7| ��9");
		UUID name = UUID.fromString("94c432ae-2b50-4820-8da0-9e3e8832743b");
		//name = UUID.nameUUIDFromBytes( ( "OfflinePlayer:" + "wolverindev" ).getBytes( Charsets.UTF_8 ) );
		manager.loadPlayer(name);
		PermissionPlayer player = manager.getPlayer(name);
		//player.addGroup("tdev");
		
		System.out.println("Own permissions:");
		for(Permission p : player.getPermissions())
			System.out.println("  "+p.getPermission()+"-"+p.getGroup());
		System.out.println("Group permissions:");
		for(Group g : player.getGroups()){
			if(g == null)
				continue;
			System.out.println("  Group: "+g.getName()+"["+g.getPrefix()+"]");
			for(Group f1 : g.getInstances())
				System.out.println("    "+f1.getName());
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void printGroup(Group g,String premif){
		
	}
}
