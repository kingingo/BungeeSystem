package dev.wolveringer.gui;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.api.inventory.Inventory;
import dev.wolveringer.api.inventory.ItemContainer;
import dev.wolveringer.bs.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public abstract class GuiUpdating extends Gui{
	private ScheduledTask pid;
	private int waitTime = 1000;
	
	public GuiUpdating(int rows, String name) {
		super(rows, name);
	}

	public GuiUpdating(Player player, Inventory inv, ItemContainer container, boolean active) {
		super(player, inv, container, active);
	}

	
	@Override
	public void active() {
		pid = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				while (isActive()) {
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
					}
					updateInventory();
				}
			}
		});
	}
	
	public abstract void updateInventory();
	
	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}
	public int getWaitTime() {
		return waitTime;
	}
	
	@Override
	public void deactive() {
		pid.cancel();
	}
}
