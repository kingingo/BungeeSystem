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
	private ArrayList<Permission> permissions = new ArrayList<>();
	@Getter
	private ArrayList<Permission> negativePerms = new ArrayList<>();
	private ArrayList<Permission> finalPermissions = new ArrayList<>();
	private ArrayList<Group> instances = new ArrayList<>();

	private PermissionManager handle;

	public Group(PermissionManager handle, String name) {
		this.name = name;
		this.handle = handle;
		init();
	}

	private void init() {
		permissions.clear();
	}

	public void addPermission(String permission) {
		addPermission(permission, GroupTyp.ALL);
	}

	public void addPermission(String permission, GroupTyp type) {
		Permission p = new Permission(permission, type);
		if ((!permissions.contains(p) && !p.isNegative()) || (!negativePerms.contains(p) && p.isNegative())) {
			if (p.isNegative())
				negativePerms.add(p);
			else
				permissions.add(new Permission(permission, type));
			MySQL.getInstance().command("INSERT INTO `game_perm`(`prefix`, `permission`, `pgroup`, `grouptyp`, `uuid`) VALUES ('none','" + permission + "','" + name + "','" + type.getName() + "','none')");
			handle.updateGroup(this);
		}
		finalPermissions = null;
	}

	public void removePermission(String permission) {
		removePermission(permission, GroupTyp.ALL);
	}

	public void removePermission(String permission, GroupTyp type) {
		for (Permission p : new ArrayList<>(permission.startsWith("-") ? negativePerms : permissions))
			if ((type == GroupTyp.ALL || p.getGroup() == type) && p.getPermission().equalsIgnoreCase(permission)) {
				permissions.remove(p);
				negativePerms.remove(p);
				System.out.println("[MySQL] -> DELETE FROM `game_perm` WHERE `pgroup`='" + name + "' AND `permission`='" + p.getPermission() + "' AND `grouptype`='" + p.getGroup().getName() + "'");
				MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `pgroup`='" + name + "' AND `permission`='" + p.getPermission() + "' AND `grouptype`='" + p.getGroup().getName() + "'");
				handle.updateGroup(this);
			}
		finalPermissions = null;
	}

	public void setPrefix(String prefix) {
		if (this.prefix.equalsIgnoreCase("undefined")) {
			MySQL.getInstance().command("INSERT INTO `game_perm`(`prefix`, `permission`, `pgroup`, `grouptyp`, `uuid`) VALUES ('" + prefix + "','none','" + name + "','ALL','none')");
			handle.updateGroup(this);
		} else {
			System.out.println("[MySQL] -> UPDATE `game_perm` SET `prefix`='" + prefix + "',`grouptyp`='ALL' WHERE `pgroup`='" + name + "' AND `permission`='none' AND `uuid`='none'");
			MySQL.getInstance().command("UPDATE `game_perm` SET `prefix`='" + prefix + "' WHERE `pgroup`='" + name + "' AND `permission`='none' AND `uuid`='none'");
			handle.updateGroup(this);
		}
		this.prefix = prefix;
	}

	public boolean hasPermission(String permission) {
		return hasPermission(permission, GroupTyp.ALL);
	}

	public boolean hasPermission(String permission, GroupTyp type) {
		for (Permission p : getPermissionsDeep())
			if ((type == GroupTyp.ALL || p.getGroup() == type) && p.acceptPermission(permission))
				return true;
		return false;
	}

	public void reload() {
		init();
	}

	public ArrayList<Permission> getPermissions() {
		if (finalPermissions == null) {
			finalPermissions = new ArrayList<>();
			ploop: for (Permission p : permissions) {
				for (Permission np : negativePerms) {
					if (np.acceptPermission(p.getPermission()))
						continue ploop;
				}
				finalPermissions.add(p);
			}
		}

		return finalPermissions;
	}

	public ArrayList<Group> getInstances() {
		return instances;
	}

	public ArrayList<Permission> getPermissionsDeep() {
		return getPermissionsDeep(new ArrayList<>());
	}

	protected ArrayList<Permission> getPermissionsDeep(ArrayList<Group> checked) {
		//epicpvp.perm.group.
		ArrayList<Permission> perms = new ArrayList<>();
		perms.addAll(getPermissions());
		for (Group g : instances) {
			if (checked.contains(g))
				continue;
			perms.addAll(g.getPermissionsDeep(checked));
			checked.add(g);
		}
		return perms;
	}

	public void delete() {
		MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `pgroup`='" + name + "'");
	}

	protected void initPerms() {
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `prefix`,`permission`,`grouptyp` FROM `game_perm` WHERE `pgroup`='" + name + "' AND `uuid`='none'", -1);
		for (String[] var : query) {
			if (var[1].equalsIgnoreCase("none"))
				prefix = var[0];
			else if (var[1].startsWith("epicpvp.perm.group.")) {
				String group = var[1].replaceFirst("epicpvp.perm.group.", "").split(":")[0]; //mvp+:sky
				Group g = handle.getGroup(group);
				if (g == null) {
					System.err.println("Cant find group instance (" + var[1] + "|" + group + ")");
					continue;
				}
				instances.add(g);
			} else
				permissions.add(new Permission(var[1], GroupTyp.get(var[2])));
		}
	}
}
