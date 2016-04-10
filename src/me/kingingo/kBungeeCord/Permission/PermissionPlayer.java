package me.kingingo.kBungeeCord.Permission;

import java.util.ArrayList;
import java.util.UUID;

import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

//WolverinDEV=57091d6f-839f-48b7-a4b1-4474222d4ad1
public class PermissionPlayer {
	@Getter
	private UUID uuid;

	private ArrayList<Permission> permissions = new ArrayList<>();
	@Getter
	private ArrayList<Permission> negativePermissions = new ArrayList<>();
	@Getter
	private ArrayList<Group> groups = new ArrayList<>();

	private ArrayList<Permission> finalPermissions;

	private PermissionManager manager;

	public PermissionPlayer(PermissionManager manager, UUID uuid) {
		//
		this.manager = manager;
		this.uuid = uuid;
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `pgroup`,`permission`,`grouptyp` FROM `game_perm` WHERE `uuid`='" + uuid + "'", -1);
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
		if (groups.size() == 0)
			groups.add(manager.getGroup("default"));
		finalPermissions = null;
	}

	public void addGroup(String group) {
		for (Group g : groups)
			if (g.getName().equalsIgnoreCase(group))
				return;
		groups.add(manager.getGroup(group));
		MySQL.getInstance().command("INSERT INTO `game_perm`(`prefix`, `permission`, `pgroup`, `grouptyp`, `uuid`) VALUES ('none','none','" + group + "','all','" + uuid.toString() + "')");
		manager.updatePlayer(uuid);
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
		System.out.println("[MySQL] -> " + "DELETE FROM `game_perm` WHERE `uuid`='" + uuid.toString() + "' AND `pgroup`='" + group + "'");
		MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `uuid`='" + uuid.toString() + "' AND `pgroup`='" + group + "'");
		manager.updatePlayer(uuid);
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
			MySQL.getInstance().command("INSERT INTO `game_perm`(`prefix`, `permission`, `pgroup`, `grouptyp`, `uuid`) VALUES ('none','" + permission + "','none','" + type.getName() + "','" + uuid.toString() + "')");
			manager.updatePlayer(uuid);
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
				System.out.println("[MySQL] -> " + "DELETE FROM `game_perm` WHERE `uuid`='" + uuid.toString() + "' AND `permission`='" + p.getPermission() + "' AND `grouptyp`='" + p.getGroup().getName() + "'");
				MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `uuid`='" + uuid.toString() + "' AND `permission`='" + p.getPermission() + "' AND `grouptyp`='" + p.getGroup().getName() + "'");
				manager.updatePlayer(uuid);
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
