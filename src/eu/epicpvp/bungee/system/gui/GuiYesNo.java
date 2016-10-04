package dev.wolveringer.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemBuilder;
import dev.wolveringer.BungeeUtil.item.ItemStack;

public abstract class GuiYesNo extends Gui{
	private String attantion;
	
	public GuiYesNo(String question,String achtung) {
		super(1, question);
		this.attantion = achtung;
	}

	@Override
	public void build() {
		inv.setItem(0, new ItemStack(ItemBuilder.create(Material.BARRIER).name("§cSchließen").build()){
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
			}
		});
		if(attantion != null)
			inv.setItem(4, ItemBuilder.create(Material.ANVIL).name("§c§lAchtung!").lore(attantion).build());
		
		inv.setItem(7, new ItemStack(ItemBuilder.create(159).name("§cAbbrechen").durbility(14).build()){
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
				onDicition(false);
			}
		});
		inv.setItem(8, ItemBuilder.create(159).name("§aAction bestätigen").listener(()->{
			onDicition(true);
		}).durbility(11).build());
		
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, 9);
	}

	public abstract void onDicition(boolean flag);
}
