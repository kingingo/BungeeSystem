package eu.epicpvp.bungee.system.gui;

import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack;

public abstract class GuiStatusPrint extends Gui{
	private Item item;

	public GuiStatusPrint(int rows, Item message) {
		super(rows, "§c");
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
