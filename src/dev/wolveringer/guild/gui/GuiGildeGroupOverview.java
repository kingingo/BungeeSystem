package dev.wolveringer.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.gilde.GildSection;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.guild.gui.section.SectionRegestry;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildeGroupOverview extends Gui{
	GildSection section;
	String[] groups;
	int index = 0;
	
	public GuiGildeGroupOverview(GildSection section) {
		super(4, "§a"+section.getType().getDisplayName()+" §6» §aGroups §cTODO");
		this.section = section;
	}
	
	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(SectionRegestry.getInstance().createGildeSection(section))).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
	}
}
