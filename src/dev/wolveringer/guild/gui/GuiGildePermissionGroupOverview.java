package dev.wolveringer.guild.gui;

import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.gilde.GildPermissionGroup;
import dev.wolveringer.gilde.GildSectionPermission;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.guild.gui.section.SectionRegestry;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildePermissionGroupOverview extends Gui{
	private static final Item ARROW_LEFT = ItemBuilder.create(Material.ARROW).name("§7» §6Nach links").build();
	private static final Item ARROW_RIGHT = ItemBuilder.create(Material.ARROW).name("§7» §6Nach rechts").build();
	
	GildSectionPermission permission;
	public GuiGildePermissionGroupOverview(GildSectionPermission perms) {
		super(4, "§a"+ perms.getHandle().getType().getDisplayName()+" §6» §aGroups");
		this.permission =  perms;
	}

	private Item[] groups;
	private int pos = 0;
	
	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(SectionRegestry.getInstance().createGildeSection(permission.getHandle().getType(), permission.getHandle()))).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
		
		inv.setItem(8, ItemBuilder.create(Material.NETHER_STAR).name("§cErstelle eine Gruppe").listener((click)->{
			click.getPlayer().sendMessage("§cTodo!");
		}).build());
		
		buildGroups();
		drawGroup();
	}

	private void buildGroups(){
		List<String> groupsName = permission.getGroups();
		groups = new Item[groupsName.size()];
		int index = 0;
		for(String g : groupsName){
			GildPermissionGroup group = permission.getGroup(g);
			ItemBuilder builder = ItemBuilder.create(Math.min(group.getItemId(), 1));
			
			builder.name("§a"+group.getName()).lore("§aClick to edit group");
			builder.listener((c)->{
				GuiGildePermissionGroupOverview.this.switchToGui(new GuiGildePermissionGroupAdminPannel(group));
			});
			groups[index++] = builder.build();
		}
	}
	
	private void drawGroup(){
		int baseSlot = 11;
		inv.setItem(10, ItemBuilder.create(ARROW_LEFT).listener((Click c)->{
			if(pos > 0){
				pos--;
				drawGroup();
			}
		}).glow(pos <= 0).build());
		inv.setItem(17, ItemBuilder.create(ARROW_RIGHT).listener((Click c)->{
			if(groups.length-pos-5 > 0){
				pos++;
				drawGroup();
			}
		}).glow(groups.length-pos-5 < 0).build());
		
		for(int i = 0;i<5;i++){
			if(i+pos >= groups.length)
				inv.setItem(baseSlot+i, null);
			else
				inv.setItem(baseSlot+i, groups[i+pos]);
		}
	}
}
