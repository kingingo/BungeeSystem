package dev.wolveringer.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.item.ItemBuilder;

public class GildPermissionMapping {
	public static Item getDisplayItem(String permission){
		return ItemBuilder.create(Material.STONE).name("§aPermission: "+permission).build();
	}
	public static String getPermissionDisplayName(String permission){
		return permission;
	}
}
