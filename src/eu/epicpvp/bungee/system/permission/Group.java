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
	private Set<Permission> permissions = new HashSet<>();
	@Getter
	private Set<Permission> negativePerms = new HashSet<>();
	private Set<Permission> finalPermissions = null;
	private Set<Group> inheritFrom = new HashSet<>();
	private int importance = 0;

	private PermissionManager handle;

	public Group(PermissionManager handle, String name) {
		this.name = name;
		this.handle = handle;
		clear();
	}

	private void clear() {
//		new Throwable("Clearing permissions of group " + name).printStackTrace();
		permissions.clear();
		negativePerms.clear();
		finalPermissions = null;
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
		clear();
		initPerms();
	}

	public Set<Permission> getPermissions() {
		if (true || finalPermissions == null) { //recalc permissions always due to possible changes in inherit groups
//			System.out.println("Group " + name + " getPermissions() calc");
			try {
				finalPermissions = new HashSet<>();
				ploop:
				for (Permission p : permissions) {
					for (Permission np : negativePerms) {
						if ((np.getGroup() == p.getGroup() || np.getGroup() == GroupTyp.ALL) && np.acceptPermission(p.getPermission())) {
//							System.out.println("  Skipping " + p.getPermission() + " [" + p.getGroup() + "] due to " + np.getPermission() + " [" + np.getGroup() + "]");
							continue ploop;
						} else {
							if (!finalPermissions.contains(np)) {
//								System.out.println("  Adding negative perm " + np.getPermission() + " [" + np.getGroup() + "]");
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

//			System.out.println("  finalPermissions:");
//			for (Permission p : finalPermissions) {
//				System.out.println("    " + p.getPermission() + " [" + p.getGroup() + "]");
//			}
		}

		return finalPermissions;
	}

	public Set<Group> getInheritFrom() {
		return inheritFrom;
	}

	public Set<Permission> getPermissionsDeep() {
		return getPermissionsDeep(new ArrayList<>());
	}

	protected Set<Permission> getPermissionsDeep(ArrayList<Group> checked) {
		//epicpvp.perm.group.
//		System.out.println("Group " + name + " getPermissionsDeep() calc");
//		System.out.println("Group " + name + " checked: " + checked.stream().map(Group::getName).collect(Collectors.toList()));
		Set<Permission> finalPerms = new HashSet<>();
		finalPerms.addAll(getPermissions());
		for (Group inheritGroup : inheritFrom) {
			if (checked.contains(inheritGroup)) {
//				System.out.println("Skipping group " + inheritGroup.getName() + " because it was already checked");
				continue;
			}
//			System.out.println("Group " + name + " checked: " + checked.stream().map(Group::getName).collect(Collectors.toList()));
			Set<Permission> groupPerms = inheritGroup.getPermissionsDeep(checked);
//			System.out.println("permissionDeep from " + inheritGroup.getName() + " (being in " + name + "):");
//			for (Permission groupPerm : groupPerms) {
//				System.out.println("  " + groupPerm.getPermission() + " [" + groupPerm.getGroup() + "]");
//			}
//			System.out.println("Group " + name + " getPermissionsDeep() exclusions - checking group " + inheritGroup.getName());

			outer:
			for (Permission groupPerm : groupPerms) {
//				System.out.println(" Checking " + groupPerm.getPermission() + " [" + groupPerm.getGroup() + "] in " + name);
				if (finalPerms.contains(groupPerm)) {
//					System.out.println("  Skipping " + groupPerm.getPermission() + " [" + groupPerm.getGroup() + "]" + " due to already being present from higher group #3");
					continue;
				}
				for (Permission alreadyPerm : finalPerms) {
					if (alreadyPerm.isNegative()) {
						if (alreadyPerm.getFinalPermission().equalsIgnoreCase(groupPerm.getFinalPermission())) {
//							System.out.println("  Skipping " + groupPerm.getPermission() + " [" + groupPerm.getGroup() + "]" + " due to " + alreadyPerm.getPermission() + " [" + alreadyPerm.getGroup() + "]" + " already being present #1");
							continue outer; //do not add any permission if its negative part was found
						}
					} else if (groupPerm.isNegative()) {
						if (alreadyPerm.getFinalPermission().equalsIgnoreCase(groupPerm.getFinalPermission())) {
//							System.out.println("  Skipping " + groupPerm.getPermission() + " due to " + alreadyPerm.getPermission() + " [" + alreadyPerm.getGroup() + "]" + " already being present #2");
							continue outer; //do not add a negative permission if any matching permission is already available
						}
					}
				}
//				System.out.println("  Taking perm " + groupPerm.getPermission() + " [" + groupPerm.getGroup() + "]");
				finalPerms.add(groupPerm);
			}
//			perms.addAll(groupPerms);
			checked.add(inheritGroup);
		}
//		System.out.println("Group " + name + " getPermissionsDeep() final return");
//		for (Permission p : finalPerms) {
//			System.out.println("  " + p.getPermission() + " [" + p.getGroup() + "]");
//		}
		return finalPerms;
	}

	public Set<Group> getGroupsDeep() {
		return getGroupsDeep(new HashSet<>());
	}

	private Set<Group> getGroupsDeep(Set<Group> groups) {
		if (groups.contains(this)) { //no endless loops
			return groups;
		}
		for (Group group : inheritFrom) {
			if (groups.contains(group)) { //no endless loops
				continue;
			}
			group.getGroupsDeep(groups);
		}
		groups.addAll(inheritFrom);
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
				if (!inheritFrom.contains(g)) {
					inheritFrom.add(g);
				}
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
//		System.out.println("---------------------------");
//		System.out.println("Gruppe " + name + ":");
//		System.out.println("Importance: " + importance);
//		System.out.println("Prefix: " + prefix);
//		System.out.println("Inherit from: " + inheritFrom);
//		System.out.println("Permissions:");
//		for (Permission p : permissions) {
//			System.out.println("  " + p.getPermission() + " [" + p.getGroup() + "]");
//		}
//		System.out.println("Negative permissions:");
//		for (Permission p : negativePerms) {
//			System.out.println("  " + p.getPermission() + " [" + p.getGroup() + "]");
//		}
//		System.out.println("---------------------------");
	}

	@Override
	public String toString() {
		return "Group [prefix=" + prefix + ", name=" + name + ", importance=" + importance + ", handle=" + handle + "]";
	}
}
