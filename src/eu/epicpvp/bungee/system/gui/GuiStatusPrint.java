package eu.epicpvp.bungee.system.gui;

import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack;

public abstract class GuiStatusPrint extends Gui{
	private Item item;

	public GuiStatusPrint(int rows, String title, Item message) {
		super(rows, title);
		this.item = new ItemStack(message) {
			@Override
			public void click(Click arg0) {
				onContinue();
			}
		};
	}

	@Override
	public void build() {
		fill(item);
	}
	public abstract void onContinue();
}
