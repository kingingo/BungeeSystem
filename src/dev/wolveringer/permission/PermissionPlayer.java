package dev.wolveringer.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import dev.wolveringer.bukkit.permissions.GroupTyp;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

//WolverinDEV=57091d6f-839f-48b7-a4b1-4474222d4ad1
public class PermissionPlayer {
	@Getter
	private Integer playerId;

	private ArrayList<Permission> permissions = new ArrayList<>();
	@Getter
	private ArrayList<Permission> negativePermissions = new ArrayList<>();
	@Getter
	private ArrayList<Group> groups = new ArrayList<>();

	private ArrayList<Permission> finalPermissions;

	private PermissionManager manager;

	public PermissionPlayer(PermissionManager manager, Integer id) {
		int cg = 0;
		this.manager = manager;
		this.playerId = id;
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `pgroup`,`permission`,`grouptyp` FROM `game_perm` WHERE `playerId`='" + playerId + "'", -1);
		for (String[] var : query) {
			if(var[0].startsWith("epicpvp.timer.group."));
			else if (!var[0].equalsIgnoreCase("none")) {
				Group g = manager.getGroup(var[0]);
				cg++;
				if(g == null)
					System.out.println("Cant find group: "+var[0]+" 1");
				if (!groups.contains(g) && g != null){
					groups.add(g);
				}
			} 
			else if (var[1].startsWith("epicpvp.perm.group.")) {
				String group = var[1].replaceFirst("epicpvp.perm.group.", "").split(":")[0]; //mvp+:sky
				Group g = manager.getGroup(group);
				cg++;
				if(g == null)
					System.out.println("Cant find group: "+group+" 2");
				if (!groups.contains(g) && g != null){
					groups.add(g);
				}
			}else {
				Permission p = new Permission(var[1], GroupTyp.get(var[2]));
				if (p.isNegative())
					negativePermissions.add(p);
				else
					permissions.add(p);
			}
			//epicpvp.timer.group.mvp1:1463312423879
		}
		for (String[] var : query) {
			if(var[1].startsWith("epicpvp.timer.group.")){//mvp1:1463312423879
				String[] args = var[1].substring("epicpvp.timer.group.".length()).split(":");
				Group g = manager.getGroup(args[0]);
				if(g == null)
					System.out.println("Timed group "+args[1]+" not found.");
				else
					if(System.currentTimeMillis()>Long.parseLong(args[1])){
						System.out.println("Player group is now outtimed! "+playerId+":"+g.getName());
						removeGroup(g.getName());
						removePermission(var[1]);
						addPermission("!"+args[1]);
					}
			}
		}
		if(groups.size() == 0 && 0 == cg){
			addGroup("default");
			System.out.println("Adding default group for: "+playerId);
		}
		finalPermissions = null;
	}

	public boolean addGroup(String group) {
		for (Group g : groups)
			if(g != null)
				if (g.getName().equalsIgnoreCase(group))
					return false;
		if (manager.getGroup(group) == null) {
			System.out.println("Cant find group: " + group);
			return false;
		}
		removeGroup("default");
		groups.add(manager.getGroup(group));
		MySQL.getInstance().command("INSERT INTO `game_perm`(`playerId`, `prefix`, `permission`, `pgroup`, `grouptyp`) VALUES ('" + playerId + "','none','none','" + group + "','all')",new MySQL.Callback<Boolean>(){
			@Override
			public void done(Boolean obj, Throwable ex) {
				manager.updatePlayer(playerId);
			}
		});
		return true;
	}

	public boolean removeGroup(String group) {
		Group gg = null;
		for (Group g : groups)
			if (g != null)
				if (g.getName().equalsIgnoreCase(group))
					gg = g;
		if (gg == null)
			return false;
		groups.remove(gg);
		MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `playerId`='" + playerId + "' AND `pgroup`='" + group + "'",new MySQL.Callback<Boolean>(){
			@Override
			public void done(Boolean obj, Throwable ex) {
				manager.updatePlayer(playerId);
			}
		});
		return true;
	}

	public boolean addPermission(String permission) {
		return addPermission(permission, GroupTyp.ALL);
	}

	public boolean addPermission(String permission, GroupTyp type) {
		Permission p = new Permission(permission, type);
		if ((!permissions.contains(p) && !p.isNegative()) || (p.isNegative() && !negativePermissions.contains(p))) {
			if (p.isNegative())
				negativePermissions.add(p);
			else
				permissions.add(p);
			MySQL.getInstance().command("INSERT INTO `game_perm`(`playerId`, `prefix`, `permission`, `pgroup`, `grouptyp`) VALUES ('" + playerId + "','none','" + permission + "','none','" + type.getName() + "')",new MySQL.Callback<Boolean>(){
				@Override
				public void done(Boolean obj, Throwable ex) {
					manager.updatePlayer(playerId);
				}
			});
		}
		else
			return false;
		finalPermissions = null;
		return true;
	}

	public boolean removePermission(String permission) {
		return removePermission(permission, GroupTyp.ALL);
	}

	public boolean removePermission(String permission, GroupTyp type) {
		int removed = 0;
		for (Permission p : new ArrayList<>(permission.startsWith("-") ? negativePermissions : permissions))
			if (p.getPermission().equalsIgnoreCase(permission) && (type == GroupTyp.ALL || p.getGroup() == type)) {
				permissions.remove(p);
				negativePermissions.remove(p);
				MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `playerId`='" + playerId + "' AND `permission`='" + p.getPermission() + "' AND `grouptyp`='" + p.getGroup().getName() + "'",new MySQL.Callback<Boolean>(){
					@Override
					public void done(Boolean obj, Throwable ex) {
						manager.updatePlayer(playerId);
					}
				});
				removed++;
			}
		finalPermissions = null;
		return removed != 0;
	}

	public boolean hasPermission(String permission) {
		return hasPermission(permission, GroupTyp.ALL);
	}

	public boolean hasPermission(String permission, GroupTyp type) {
		for (Permission p : negativePermissions)
			if (p.acceptPermission(permission) && (p.getGroup() == type || p.getGroup() == GroupTyp.ALL))
				return false;
		for (Permission p : new ArrayList<>(permissions))
			if ((type == GroupTyp.ALL || p.getGroup() == type) && p.acceptPermission(permission))
				return true;
		for (Group group : groups)
			if (group.hasPermission(permission, type))
				return true;
		return false;
	}

	public ArrayList<Permission> getPermissions() {
		if (finalPermissions == null) {
			ArrayList<Permission> perms = new ArrayList<>();
			ploop: for (Permission p : permissions) {
				for (Permission np : negativePermissions) {
					if (np.acceptPermission(p.getPermission()))
						continue ploop;
				}
				perms.add(p);
			}
			finalPermissions = perms;
		}
		return finalPermissions;
	}
}
