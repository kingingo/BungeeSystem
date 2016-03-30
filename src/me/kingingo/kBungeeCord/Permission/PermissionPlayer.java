package me.kingingo.kBungeeCord.Permission;

import java.util.ArrayList;
import java.util.UUID;

import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

//WolverinDEV=57091d6f-839f-48b7-a4b1-4474222d4ad1
public class PermissionPlayer {
	@Getter
	private UUID uuid;
	
	@Getter
	private ArrayList<Permission> permissions = new ArrayList<>();
	@Getter
	private ArrayList<Group> groups = new ArrayList<>();
	
	private PermissionManager manager;
	
	public PermissionPlayer(PermissionManager manager,UUID uuid){
		//
		this.manager = manager;
		this.uuid = uuid;
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `pgroup`,`permission`,`grouptyp` FROM `game_perm` WHERE `uuid`='"+uuid+"'", -1);
		for(String[] var : query){
			if(!var[0].equalsIgnoreCase("none")){
				Group g = manager.getGroup(var[0]);
				if(!groups.contains(g) && g != null)
					groups.add(g);
			}else
				permissions.add(new Permission(var[1], GroupTyp.get(var[2])));
		}
	}
	
	public void addGroup(String group){
		for(Group g : groups)
			if(g.getName().equalsIgnoreCase(group))
				return;
		groups.add(manager.getGroup(group));
		MySQL.getInstance().command("INSERT INTO `game_perm`(`prefix`, `permission`, `pgroup`, `grouptyp`, `uuid`) VALUES ('none','none','"+group+"','all','"+uuid.toString()+"')");
	}
	
	public void removeGroup(String group){
		Group gg = null;
		for(Group g : groups)
			if(g != null)
				if(g.getName().equalsIgnoreCase(group))
					gg = g;
		if(gg == null)
			return;
		groups.remove(gg);
		System.out.println("[MySQL] -> "+"DELETE FROM `game_perm` WHERE `uuid`='"+uuid.toString()+"' AND `pgroup`='"+group+"'");
		MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `uuid`='"+uuid.toString()+"' AND `pgroup`='"+group+"'");
	}
	
	public void addPermission(String permission){
		addPermission(permission, GroupTyp.ALL);
	}
	public void addPermission(String permission,GroupTyp type){
		if(!permissions.contains(new Permission(permission,type))){
			permissions.add(new Permission(permission,type));
			MySQL.getInstance().command("INSERT INTO `game_perm`(`prefix`, `permission`, `pgroup`, `grouptyp`, `uuid`) VALUES ('none','"+permission+"','none','"+type.getName()+"','"+uuid.toString()+"')");
		}
	}
	public void removePermission(String permission){
		removePermission(permission, GroupTyp.ALL);
	}
	public void removePermission(String permission,GroupTyp type){
		for(Permission p : new ArrayList<>(permissions))
			if(p.acceptPermission(permission) && (type == GroupTyp.ALL || p.getGroup() == type)){
				permissions.remove(p);
				System.out.println("[MySQL] -> "+"DELETE FROM `game_perm` WHERE `uuid`='"+uuid.toString()+"' AND `permission`='"+p.getPermission()+"' AND `grouptype`='"+p.getGroup().getName()+"'");
				MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `uuid`='"+uuid.toString()+"' AND `permission`='"+p.getPermission()+"' AND `grouptype`='"+p.getGroup().getName()+"'");
			}
	}
	public boolean hasPermission(String permission){
		return hasPermission(permission, GroupTyp.ALL);
	}
	public boolean hasPermission(String permission,GroupTyp type){
		for(Permission p : new ArrayList<>(permissions))
			if((type == GroupTyp.ALL || p.getGroup() == type) && p.acceptPermission(permission))
				return true;
		for(Group group : groups)
			if(group.hasPermission(permission, type))
				return true;
		return false;
	}
}
