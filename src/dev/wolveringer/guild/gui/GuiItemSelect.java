package dev.wolveringer.guild.gui;

import java.util.ArrayList;
import java.util.Arrays;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;

public abstract class GuiItemSelect extends Gui{
	private ArrayList<Item> items = new ArrayList<>();
	
	private int select = 0;
	
	public GuiItemSelect(String name) {
		super(2, name);
	}
	
	public void setActive(int select){
		this.select = select;
		rebuild();
	}
	
	public void addSelectable(Item item) {
		items.add(item);
	}
	
	public void addSelectables(Item... items) {
		this.items.addAll(Arrays.asList(items));
	}
	
	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.NETHER_STAR.getId()).name("§aBack").listener((c)->{
			if (select - 1 < 0) select = items.size();
			select--;
			rebuild();
		}).build());
		
		inv.setItem(8, ItemBuilder.create(Material.NETHER_STAR.getId()).name("§aNext").listener((c)->{
			select++;
			select = select % items.size();
			rebuild();
		}).build());
		
		Item success = ItemBuilder.create().id(160).durbility(5).name("§aSave").listener((c)->{
			select(select);
		}).build();
		for (int i = 0; i < 5; i++)
			inv.setItem(9 + i, success);
		
		inv.setItem(13, ItemBuilder.create().id(160).durbility(8).name("§6Currunt Selection").glow().build());
		
		Item cancel = ItemBuilder.create().id(160).durbility(5).name("§cCancel").listener((c)->{
			cancel();
		}).build();
		for (int i = 5; i < 9; i++)
			inv.setItem(9 + i, cancel);
		rebuild();
	}
	private void rebuild() {
		inv.disableUpdate();
		for (int i = 1; i < 8; i++)
			inv.setItem(i, null);
		try {
			// 4 = CENTER (SELECTED)
			// +-3 VALUES
			
			// BUILD -
			int center = 4;
			
			int length = select - 3 < 0 ? select : 3;
			for (int x = center - length; x < center; x++){
				Item item = items.get(select + (x - center));
				item.setAmount(0);
				item.getItemMeta().setGlow(false);
				inv.setItem(x, item);
			}
			Item i = items.get(select);
			i.getItemMeta().setGlow(true);
			i.setAmount(1);
			inv.setItem(center, i);
			
			length = select + 3 > items.size() - 1 ? items.size() - select - 1 : 3;
			for (int x = center + 1; x < center + length + 1; x++){
				Item item = items.get(select + (x - center));
				item.setAmount(0);
				item.getItemMeta().setGlow(false);
				inv.setItem(x, item);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		inv.enableUpdate();
	}
	public abstract void select(int select);
	
	public abstract void cancel();
}
