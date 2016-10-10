package eu.epicpvp.bungee.system.permission;

import eu.epicpvp.datenserver.definitions.permissions.GroupTyp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = {"permission", "group"})
public class Permission {
	private String permission;
	private String finalPermission;
	private GroupTyp group;
	private int starIndex = -1;
	private boolean negative = false;

	public boolean acceptPermission(String perm){
		if(starIndex != -1){
			return perm.substring(0,Math.min(starIndex, perm.length())).equalsIgnoreCase(finalPermission.substring(0,starIndex));
		}
		return finalPermission.equalsIgnoreCase(perm);
	}

	public Permission(String permission, GroupTyp group) {
		if(permission.startsWith("-")){
			negative = true;
			finalPermission = permission.substring(1);
		}
		else
			finalPermission = permission;
		this.permission = permission;
		this.group = group;
		starIndex = finalPermission.indexOf("*");
	}
}
