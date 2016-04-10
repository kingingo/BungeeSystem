package me.kingingo.kBungeeCord.Permission;

import lombok.Getter;

@Getter
public class Permission {
	private String permission;
	private String finalPermission;
	private GroupTyp group;
	private int starIndex = -1;
	@Getter
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

	@Override
	public String toString() {
		return "Permission [permission=" + permission + ", group=" + group + ", starIndex=" + starIndex + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + (negative ? 1231 : 1237);
		result = prime * result + ((permission == null) ? 0 : permission.hashCode());
		result = prime * result + starIndex;
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Permission other = (Permission) obj;
		if (group != other.group)
			return false;
		if (negative != other.negative)
			return false;
		if (permission == null) {
			if (other.permission != null)
				return false;
		} else if (!permission.equals(other.permission))
			return false;
		if (starIndex != other.starIndex)
			return false;
		return true;
	}
}
