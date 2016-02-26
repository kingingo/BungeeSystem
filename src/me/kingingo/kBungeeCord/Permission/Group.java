package me.kingingo.kBungeeCord.Permission;

import java.util.ArrayList;

import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

//TODO get all groups: SELECT DISTINCT `pgroup` FROM `game_perm` WHERE `pgroup`!='none' AND `uuid`='none' 
public class Group {

	@Getter
	private String prefix = "undefined";
	@Getter
	private String name = "undefined";
	private ArrayList<Permission> perms = new ArrayList<>();

	public Group(String name) {
		System.out.println("Load group: "+name);
		this.name = name;
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `prefix`,`permission`,`grouptyp` FROM `game_perm` WHERE `pgroup`='"+name+"' AND `uuid`='none'", -1);
		for(String[] var : query){
			if(var[1].equalsIgnoreCase("none"))
				prefix = var[0];
			else
				perms.add(new Permission(var[1], GroupTyp.get(var[2])));
		}
		
	}

	public void addPermission(String permission){
		addPermission(permission, GroupTyp.ALL);
	}
	public void addPermission(String permission,GroupTyp type){
		if(!perms.contains(new Permission(permission,type))){
			perms.add(new Permission(permission,type));
			MySQL.getInstance().command("INSERT INTO `game_perm`(`prefix`, `permission`, `pgroup`, `grouptyp`, `uuid`) VALUES ('none','"+permission+"','"+name+"','"+type.getName()+"','none')");
		}
	}
	public void removePermission(String permission){
		removePermission(permission, GroupTyp.ALL);
	}
	public void removePermission(String permission,GroupTyp type){
		for(Permission p : new ArrayList<>(perms))
			if((type == GroupTyp.ALL || p.getGroup() == type) && p.acceptPermission(permission)){
				perms.remove(p);
				MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `pgroup`='"+name+"' AND `permission`='"+p.getPermission()+"' AND `grouptype`='"+p.getGroup().getName()+"'");
			}
	}
	
	public void setPrefix(String prefix) {
		if(this.prefix.equalsIgnoreCase("undefined")){
			MySQL.getInstance().command("INSERT INTO `game_perm`(`prefix`, `permission`, `pgroup`, `grouptyp`, `uuid`) VALUES ('"+prefix+"','none','"+name+"','none','none')");
		}
		else
		{
			MySQL.getInstance().command("UPDATE `game_perm` SET `prefix`=[value-1],`permission`=[value-2],`pgroup`=[value-3],`grouptyp`=[value-4],`uuid`=[value-5] WHERE `pgroup`='"+name+"' AMD `permission`='none' AND `uuid`='none'");
		}
		this.prefix = prefix;
	}
	
	public boolean hasPermission(String permission){
		return hasPermission(permission, GroupTyp.ALL);
	}
	public boolean hasPermission(String permission,GroupTyp type){
		for(Permission p : new ArrayList<>(perms))
			if((type == GroupTyp.ALL || p.getGroup() == type) && p.acceptPermission(permission))
				return true;
		return false;
	}

	public ArrayList<Permission> getPermissions() {
		return perms;
	}
}
