package dev.wolveringer.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.item.ItemBuilder;

public class GuiWaiting extends GuiUpdating{
	private static final Item[] ROW_ITEMS = new Item[]{
		ItemBuilder.create(Material.WOOL).build(),
		ItemBuilder.create(Material.WOOL).build(),
		ItemBuilder.create(Material.WOOL).durbility(8).build(),
		ItemBuilder.create(Material.WOOL).durbility(7).build(),
		ItemBuilder.create(Material.WOOL).durbility(15).build(),
		ItemBuilder.create(Material.WOOL).build(),
		ItemBuilder.create(Material.WOOL).build()
	};
	
	private String message;
	private long start = System.currentTimeMillis();
	
	public GuiWaiting(String message,String title) {
		super(3, message);
		this.message = message;
		this.setWaitTime(150);
	}
	
	int step = 0;
	@Override
	public void updateInventory() {
		Item[] items = new Item[ROW_ITEMS.length];
		System.arraycopy(ROW_ITEMS, step%ROW_ITEMS.length, items, 0, ROW_ITEMS.length-(step%ROW_ITEMS.length));
		System.arraycopy(ROW_ITEMS, 0, items, ROW_ITEMS.length-(step%ROW_ITEMS.length), step%ROW_ITEMS.length);
		for(int i = 0;i<ROW_ITEMS.length;i++)
			inv.setItem(10+i, ItemBuilder.create(items[i]).name(message).build());
		step--;
		if(step < 0)
			step = ROW_ITEMS.length-1;
	}

	@Override
	public void build() {
		start = System.currentTimeMillis();
		step = (int) ((ROW_ITEMS.length-1)*Math.random());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
		updateInventory();
	}
	
	public void waitForMinwait(int ms){
		while (start+ms > System.currentTimeMillis()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	}
}
