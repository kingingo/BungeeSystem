package dev.wolveringer.report.search;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.bs.Main;
import dev.wolveringer.gui.SearchMenue;
import dev.wolveringer.item.ItemBuilder;

public abstract class PlayerSearchMenue extends SearchMenue {

	public PlayerSearchMenue(Player player) {
		super(player);
		setAvariableEntities(Main.getDatenServer().getPlayers());
	}

	@Override
	protected void updateInv() {
		gui.setCenterItem(ItemBuilder.create(Material.GOLDEN_APPLE).durbility(1).name("§eEnter a playername").build());
		gui.setColorPrefix("§a");
		gui.setOutputItem(ItemBuilder.create(Material.LAVA_BUCKET).name("§cCancel").lore("§aClick to cancel").glow().build());
	}

	public abstract void playerEntered(String name);

	@Override
	public abstract void canceled();

	@Override
	protected Item createEntity(String name, int slot) {
		return loadSkin(ItemBuilder.create(Material.SKULL_ITEM).durbility(1).name("§e"+name).build(), name, slot);
	}

	@Override
	public void inputEntered(String name) {
		playerEntered(name);
	}
}
