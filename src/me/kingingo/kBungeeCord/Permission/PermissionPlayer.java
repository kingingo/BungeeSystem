package me.kingingo.kBungeeCord.Permission;

import java.util.ArrayList;
import java.util.UUID;

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

	private boolean tempDefault = false;

	public PermissionPlayer(PermissionManager manager, Integer id) {
		//
		this.manager = manager;
		this.playerId = id;
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `pgroup`,`permission`,`grouptyp` FROM `game_perm` WHERE `playerId`='" + playerId + "'", -1);
		for (String[] var : query) {
			if (!var[0].equalsIgnoreCase("none")) {
				Group g = manager.getGroup(var[0]);
				if (!groups.contains(g) && g != null)
					groups.add(g);
			} else {
				Permission p = new Permission(var[1], GroupTyp.get(var[2]));
				if (p.isNegative())
					negativePermissions.add(p);
				else
					permissions.add(p);
			}
		}
		if (groups.size() == 0) {
			tempDefault = true;
			groups.add(manager.getGroup("default"));
		}
		finalPermissions = null;
	}

	public void addGroup(String group) {
		for (Group g : groups)
			if (g.getName().equalsIgnoreCase(group))
				return;
		if (manager.getGroup(group) == null) {
			System.out.println("Cant find group: " + group);
		}
		if(tempDefault)
			groups.clear();
		groups.add(manager.getGroup(group));
		MySQL.getInstance().command("INSERT INTO `game_perm`(`playerId`, `prefix`, `permission`, `pgroup`, `grouptyp`) VALUES ('" + playerId + "','none','none','" + group + "','all')");
		manager.updatePlayer(playerId);
	}

	public void removeGroup(String group) {
		Group gg = null;
		for (Group g : groups)
			if (g != null)
				if (g.getName().equalsIgnoreCase(group))
					gg = g;
		if (gg == null)
			return;
		groups.remove(gg);
		MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `playerId`='" + playerId + "' AND `pgroup`='" + group + "'");
		manager.updatePlayer(playerId);
	}

	public void addPermission(String permission) {
		addPermission(permission, GroupTyp.ALL);
	}

	public void addPermission(String permission, GroupTyp type) {
		Permission p = new Permission(permission, type);
		if ((!permissions.contains(p) && !p.isNegative()) || (p.isNegative() && !negativePermissions.contains(p))) {
			if (p.isNegative())
				negativePermissions.add(p);
			else
				permissions.add(p);
			MySQL.getInstance().command("INSERT INTO `game_perm`(`playerId`, `prefix`, `permission`, `pgroup`, `grouptyp`) VALUES ('" + playerId + "','none','" + permission + "','none','" + type.getName() + "')");
			manager.updatePlayer(playerId);
		}
		finalPermissions = null;
	}

	public void removePermission(String permission) {
		removePermission(permission, GroupTyp.ALL);
	}

	public void removePermission(String permission, GroupTyp type) {
		for (Permission p : new ArrayList<>(permission.startsWith("-") ? negativePermissions : permissions))
			if (p.getPermission().equalsIgnoreCase(permission) && (type == GroupTyp.ALL || p.getGroup() == type)) {
				permissions.remove(p);
				negativePermissions.remove(p);
				MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `playerId`='" + playerId + "' AND `permission`='" + p.getPermission() + "' AND `grouptyp`='" + p.getGroup().getName() + "'");
				manager.updatePlayer(playerId);
			}
		finalPermissions = null;
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
