package me.kingingo.kBungeeCord.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.mysql.MySQL;
import me.kingingo.kBungeeCord.Language.Language;
import net.md_5.bungee.BungeeCord;
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
	private HashMap<UUID, PermissionPlayer> user = new HashMap<>();

	public PermissionManager() {
		if(BungeeCord.getInstance() != null)
			BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}
	
	public void loadGroups(){
		ArrayList<String[]> qurry = MySQL.getInstance().querySync("SELECT DISTINCT `pgroup` FROM `game_perm` WHERE `pgroup`!='none' AND `uuid`='none' ",-1);
		long start = System.currentTimeMillis();
		System.out.println("Loading permission groups...");
		for(String[] var : qurry)
			groups.add(new Group(var[0]));
		System.out.println("Done ("+(System.currentTimeMillis()-start)+")");
	}

	public void loadPlayer(UUID player) {
		if (!user.containsKey(player))
			user.put(player, new PermissionPlayer(this, player));
	}

	public PermissionPlayer getPlayer(UUID player) {
		return user.get(player);
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
		if(!user.containsKey(uuid))
			user.put(uuid, new PermissionPlayer(this, uuid));
		return user.get(uuid).hasPermission(permission);
	}

	public Group getGroup(String name) {
		for(Group g : groups)
			if(g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("permission")){
			byte action = e.getBuffer().readByte();
			if(action == 0){
				user.remove(e.getBuffer().readUUID());
			}
		}
	}
	
	@EventHandler
	public void a(PluginMessageEvent e){
		if(e.getTag().equalsIgnoreCase("Bungeecord")){
			
		}
	}
	
	public static void main(String[] args) {
		MySQL.setInstance(new MySQL("148.251.143.2", "3306", "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT"));
		PermissionManager manager = new PermissionManager();
		manager.loadGroups();
		
		UUID name = UUID.fromString("57091d6f-839f-48b7-a4b1-4474222d4ad1");
		manager.loadPlayer(name);
		
		PermissionPlayer player = manager.getPlayer(name);
		player.addGroup("admin");
		//player.addPermission("epicpvp.*");
		
		System.out.println("Own permissions:");
		for(Permission p : player.getPermissions())
			System.out.println("  "+p.getPermission()+"-"+p.getGroup());
		System.out.println("Group permissions:");
		for(Group g : player.getGroups()){
			if(g == null)
				continue;
			System.out.println("  Group: "+g.getName()+"["+g.getPrefix()+"]");
			for(Permission p : g.getPermissions())
				System.out.println("    "+p.getPermission()+"-"+p.getGroup());
		}
	}
}
