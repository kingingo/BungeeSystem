package dev.wolveringer.booster;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.api.inventory.Inventory;
import dev.wolveringer.api.inventory.InventoryType;
import dev.wolveringer.item.ItemBuilder;

public abstract class GuiIntegerSelect {
	private Player player;
	private String title;
	private Inventory inv;
	private int currunt = 0;
	private String centerName = "§e%s";
	
	public GuiIntegerSelect(Player player, String title) {
		this.player = player;
		this.title = title;
	}
	public GuiIntegerSelect(Player player, String title,int start) {
		this.player = player;
		this.title = title;
		this.currunt = start;
	}
	public GuiIntegerSelect(Player player, String title,int start,String centername) {
		this.player = player;
		this.title = title;
		this.currunt = start;
		this.centerName = centername;
	}
	
	public void open(){
		inv = new Inventory(InventoryType.Hopper, title);
		inv.setItem(0, buildItem(77, -10));
		inv.setItem(1, buildItem(143, -1));
		inv.setItem(3, buildItem(143, 1));
		inv.setItem(4, buildItem(77, 10));
		updateCenter();
		player.openInventory(inv);
	}
	
	private Item buildItem(int id,int action){
		return ItemBuilder.create(id).name((action>0?"§a":"§c")+action).glow(!isNumberAllowed(currunt+action)).listener((c)->{
			if(isNumberAllowed(currunt+action))
				currunt += action;
			updateCenter();
		}).build();
	}
	
	private synchronized void updateCenter(){
		inv.setItem(2, ItemBuilder.create(399).name(String.format(centerName, currunt)).lore(isNumberAllowed(currunt)?"§eKlicke um fortzufahren.":"§cDiese Zahl ist nicht möglich!").listener((c)->{
			if(isNumberAllowed(currunt)){
				numberEntered(currunt);
			}
		}).build());
	}
	
	public abstract boolean isNumberAllowed(int number);
	public abstract void numberEntered(int number);
}
