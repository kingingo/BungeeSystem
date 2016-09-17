package dev.wolveringer.guild.gui;

import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.gilde.GildPermissionGroup;
import dev.wolveringer.gilde.GildePermissions;
import dev.wolveringer.gilde.GildePermissions.DisplayItem;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildePermissionGroupAdminPannel extends Gui{
	private static class PermissionItem {
		private Item item;
		private GildPermissionGroup handle;
		private GildePermissions perm;
		
		private static Item buildItem(DisplayItem item){
			return ItemBuilder.create(item.getId()).name(item.getName()).durbility(item.getDurbility()).lore(item.getLore()).build();
		}
		
		public PermissionItem(GildPermissionGroup handle,GildePermissions perm) {
			item = buildItem(perm.getDisplayItem());
			this.handle = handle;
			this.perm = perm;
		}
		
		public Item buildItem(){
			return new ItemBuilder(item).glow(handle.getPermissions().contains(perm.getPermission())).listener((click)->{
				//TODO check gild permission
				if(click.getItem().getItemMeta().hasGlow()){
					click.getItem().getItemMeta().setGlow(false);
					item.getItemMeta().setGlow(false);
					handle.removePermission(perm.getPermission());
				}
				else
				{
					click.getItem().getItemMeta().setGlow(true);	
					item.getItemMeta().setGlow(true);
					handle.addPermission(perm.getPermission());
				}
			}).build();
		}
	}
	
	private static int[] slots = {11,29,  13,31,  15,33};
	private static int movePerClick = 2;
	
	private static final Item ARROW_LEFT = ItemBuilder.create(Material.ARROW).name("§7» §6Nach links").build();
	private static final Item ARROW_RIGHT = ItemBuilder.create(Material.ARROW).name("§7» §6Nach rechts").build();
	
	private GildPermissionGroup group;
	private PermissionItem[] items;
	private int pos = 0;
	
	public GuiGildePermissionGroupAdminPannel(GildPermissionGroup group) {
		super(5, "§a"+ group.getHandle().getHandle().getType().getDisplayName()+" §6» §aGroup §6» §a"+group.getName());
		this.group = group;
	}

	@Override
	public void build() {
		buildItem();
		
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiGildePermissionGroupOverview(group.getHandle()))).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
		
		print();
	}
	
	private void print(){
		inv.setItem(18, ItemBuilder.create(ARROW_LEFT).listener(()->{
			if(pos > 0){
				pos--;
				print();
			}
		}).glow(pos <= 0).build());
		inv.setItem(26, ItemBuilder.create(ARROW_RIGHT).listener(()->{
			if(items.length-slots.length-movePerClick*pos > 0){
				pos++;
				print();
			}
		}).glow(items.length-slots.length-movePerClick*pos <= 0).build());
		
		displayPermissions();
	}
	
	private void buildItem(){
		List<GildePermissions> permissions = group.getEnumPermissions();
		items = new PermissionItem[permissions.size()];
		for(int i = 0;i<items.length;i++)
			items[i] = new PermissionItem(group, permissions.get(0));
	}
	
	public void displayPermissions(){
		for(int i = 0;i<slots.length;i++){
			if(i+pos*movePerClick < items.length)
				inv.setItem(slots[i], items[i+pos*movePerClick].buildItem());
			else
				inv.setItem(slots[i], null);
		}
	}
}
