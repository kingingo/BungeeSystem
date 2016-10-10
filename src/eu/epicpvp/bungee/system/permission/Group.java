package eu.epicpvp.bungee.system.permission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import eu.epicpvp.bungee.system.mysql.MySQL;
import eu.epicpvp.datenserver.definitions.permissions.GroupTyp;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@SuppressWarnings("unchecked")
@EqualsAndHashCode(of = "name")
public class Group {

	@Getter
	private String prefix = "undefined";
	@Getter
	private String name = "undefined";
	private ArrayList<Permission> permissions = new ArrayList<>();
	@Getter
	private ArrayList<Permission> negativePerms = new ArrayList<>();
	private ArrayList<Permission> finalPermissions = null;
	private ArrayList<Group> instances = new ArrayList<>();
	private int importance = 0;

	private PermissionManager handle;

	public Group(PermissionManager handle, String name) {
		this.name = name;
		this.handle = handle;
		init();
	}

	private void init() {
		permissions.clear();
	}

	public boolean addPermission(String permission) {
		return addPermission(permission, GroupTyp.ALL);
	}

	public boolean addPermission(String permission, GroupTyp type) {
		Permission p = new Permission(permission, type);
		if ((!permissions.contains(p) && !p.isNegative()) || (!negativePerms.contains(p) && p.isNegative())) {
			if (p.isNegative())
				negativePerms.add(p);
			else
				permissions.add(new Permission(permission, type));
			MySQL.getInstance().command("INSERT INTO `game_perm`(`playerId`,`prefix`, `permission`, `pgroup`, `grouptyp`) VALUES ('-2','none','" + permission + "','" + name + "','" + type.getName() + "')", new MySQL.Callback<Boolean>() {
				@Override
				public void done(Boolean obj, Throwable ex) {
					handle.updateGroup(Group.this);
				}
			});
		} else
			return false;
		finalPermissions = null;
		return true;
	}

	public boolean removePermission(String permission) {
		return removePermission(permission, GroupTyp.ALL);
	}

	public boolean removePermission(String permission, GroupTyp type) {
		int count = 0;
		for (Permission p : new ArrayList<>(permission.startsWith("-") ? negativePerms : permissions))
			if ((type == GroupTyp.ALL || p.getGroup() == type) && p.getPermission().equalsIgnoreCase(permission)) {
				permissions.remove(p);
				negativePerms.remove(p);
				MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `pgroup`='" + name + "' AND `permission`='" + p.getPermission() + "' AND `grouptype`='" + p.getGroup().getName() + "'", new MySQL.Callback<Boolean>() {
					@Override
					public void done(Boolean obj, Throwable ex) {
						handle.updateGroup(Group.this);
					}
				});
				count++;
			}
		finalPermissions = null;
		return count != 0;
	}

	public void setPrefix(String prefix) {
		if (this.prefix.equalsIgnoreCase("undefined")) {
			MySQL.getInstance().command("INSERT INTO `game_perm`(`playerId`,`prefix`, `permission`, `pgroup`, `grouptyp`) VALUES ('-2','" + prefix + "','none','" + name + "','ALL')", new MySQL.Callback<Boolean>() {
				@Override
				public void done(Boolean obj, Throwable ex) {
					handle.updateGroup(Group.this);
				}
			});
		} else {
			MySQL.getInstance().command("UPDATE `game_perm` SET `prefix`='" + prefix + "' WHERE `pgroup`='" + name + "' AND `permission`='none' AND `playerId`='-2'", new MySQL.Callback<Boolean>() {
				@Override
				public void done(Boolean obj, Throwable ex) {
					handle.updateGroup(Group.this);
				}
			});
		}
		this.prefix = prefix;
	}

	public boolean hasPermission(String permission) {
		return hasPermission(permission, GroupTyp.ALL);
	}

	public boolean hasPermission(String permission, GroupTyp type) {
		for (Permission p : getPermissionsDeep())
			if (p == null) {
				continue;
			} else if ((type == GroupTyp.ALL || p.getGroup() == type) && p.acceptPermission(permission))
				return true;
		return false;
	}

	public void reload() {
		init();
	}

	public ArrayList<Permission> getPermissions() {
		if (finalPermissions == null) {
			try {
				finalPermissions = new ArrayList<>();
				ploop:
				for (Permission p : permissions) {
					for (Permission np : negativePerms) {
						if (np.acceptPermission(p.getPermission()))
							continue ploop;
						else {
							if (!finalPermissions.contains(np)) {
								finalPermissions.add(np);
							}
						}
					}
					finalPermissions.add(p);
				}
			} catch (Exception e) {
				e.printStackTrace();
				finalPermissions = null;
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
			ArrayList<Permission> groupPerms = g.getPermissionsDeep(checked);
			outer: for (Permission perm : groupPerms) {
				if (!perms.contains(perm)) {
					for (Permission alreadyPerm : perms) {
						if (alreadyPerm.isNegative()) {
							if (alreadyPerm.getFinalPermission().equalsIgnoreCase(perm.getFinalPermission())) {
								break outer; //do not add any permission if its negative part was found
							}
						} else if (perm.isNegative()) {
							if (alreadyPerm.getFinalPermission().equalsIgnoreCase(perm.getFinalPermission())) {
								break outer; //do not add a negative permission if any matching permission is already available
							}
						}
					}
					perms.add(perm);
				}
			}
//			perms.addAll(groupPerms);
			checked.add(g);
		}
		return perms;
	}

	public Set<Group> getGroupsDeep() {
		return getGroupsDeep(new HashSet<>());
	}

	private Set<Group> getGroupsDeep(Set<Group> groups) {
		if (groups.contains(this)) { //no endless loops
			return groups;
		}
		for (Group group : instances) {
			if (groups.contains(group)) { //no endless loops
				continue;
			}
			group.getGroupsDeep(groups);
		}
		groups.addAll(instances);
		return groups;
	}

	public void delete() {
		MySQL.getInstance().command("DELETE FROM `game_perm` WHERE `pgroup`='" + name + "'");
	}

	public int getImportance() {
		return importance;
	}

	public void setImportance(int importance) {
		this.importance = importance;
		if (MySQL.getInstance().querySync("SELECT prefix FROM game_perm WHERE playerId='-1' AND pgroup='" + name + "' AND prefix='importance'", 1).size() == 0)
			MySQL.getInstance().command("INSERT INTO `game_perm`(`playerId`,`prefix`, `permission`, `pgroup`, `grouptyp`) VALUES ('-2','importance','importance." + importance + "','" + name + "','ALL')", new MySQL.Callback<Boolean>() {
				@Override
				public void done(Boolean obj, Throwable ex) {
					handle.updateGroup(Group.this);
				}
			});
		else
			MySQL.getInstance().command("UPDATE `game_perm` SET `permission`='importance." + importance + "' WHERE `pgroup`='" + name + "' AND `prefix`='importance' AND `playerId`='-2'", new MySQL.Callback<Boolean>() {
				@Override
				public void done(Boolean obj, Throwable ex) {
					handle.updateGroup(Group.this);
				}
			});
	}

	public void initPerms() {
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `prefix`,`permission`,`grouptyp` FROM `game_perm` WHERE `pgroup`='" + name + "' AND `playerId`='-2'", -1);
		for (String[] var : query) {
			if (var[1].startsWith("epicpvp.perm.group.")) {
				String group = var[1].replaceFirst("epicpvp.perm.group.", "").split(":")[0]; //mvp+:sky
				Group g = handle.getGroup(group);
				if (g == null) {
					System.err.println("Cant find group instance (" + var[1] + "|" + group + ")");
					continue;
				}
				if (!instances.contains(g))
					instances.add(g);
			} else if (var[1].startsWith("importance.")) {
				try {
					importance = Integer.parseInt(var[1].replaceFirst("importance.", ""));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (var[1].equalsIgnoreCase("none")) {
				prefix = var[0];
			} else {
				Permission p = new Permission(var[1], GroupTyp.get(var[2]));
				if (p.isNegative())
					negativePerms.add(p);
				else
					permissions.add(p);
			}
		}
	}

	@Override
	public String toString() {
		return "Group [prefix=" + prefix + ", name=" + name + ", importance=" + importance + ", handle=" + handle + "]";
	}
}
