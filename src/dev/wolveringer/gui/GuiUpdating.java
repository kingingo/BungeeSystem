package dev.wolveringer.gui;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.api.inventory.Inventory;
import dev.wolveringer.api.inventory.ItemContainer;
import dev.wolveringer.thread.ThreadFactory;
import dev.wolveringer.thread.ThreadRunner;

public abstract class GuiUpdating extends Gui{
	private ThreadRunner pid;
	private int waitTime = 1000;
	
	public GuiUpdating(int rows, String name) {
		super(rows, name);
	}

	public GuiUpdating(Player player, Inventory inv, ItemContainer container, boolean active, long animationend) {
		super(player, inv, container, active, animationend);
	}

	@Override
	public void active() {
		pid = ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				while (isActive()) {
					try {
						Thread.sleep(waitTime);
					} catch (InterruptedException e) {
					}
					if(isActive())
					updateInventory();
				}
				System.out.println("Stop updating");
			}
		});
		pid.start();
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
		pid.stop();
	}
}
