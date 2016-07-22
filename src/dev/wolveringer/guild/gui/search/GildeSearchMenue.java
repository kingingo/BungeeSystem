package dev.wolveringer.guild.gui.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.Callback;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.gui.SearchMenue;
import dev.wolveringer.item.ItemBuilder;

public abstract class GildeSearchMenue extends SearchMenue {

	private HashMap<UUID, String> gilden = new HashMap<>();

	public GildeSearchMenue(Player player, GildeType type) {
		super(player);
		load(type);
	}

	public void load(GildeType type) {
		Main.getDatenServer().getClient().getAvailableGilde(type).getAsync(new Callback<HashMap<UUID, String>>() {
			@Override
			public void call(HashMap<UUID, String> obj, Throwable exception) {
				gilden = obj;
				ArrayList<String> uuids = new ArrayList<>();
				for (UUID uuid : obj.keySet())
					uuids.add(uuid.toString());
				setAvariableEntities(uuids);
				if (isActive()) {
					updateSelection();
					redrawInventory();
				}
			}
		});
	}

	@Override
	protected void updateInv() {
		gui.setCenterItem(ItemBuilder.create(Material.GOLDEN_APPLE).durbility(1).name("§eEnter the GildName").build());
		gui.setColorPrefix("§a");
		gui.setOutputItem(ItemBuilder.create(Material.LAVA_BUCKET).name("§cCancel").lore("§aClick to cancel").glow().build());
	}

	@Override
	protected Item createEntity(String name, int slot) {
		return ItemBuilder.create().id(370).name("§aGilde: §6" + gilden.get(name)).lore("§aGilden UUID: §6" + name).build();
	}

	@Override
	public void inputEntered(String name) {
		gildeEntered(UUID.fromString(name));
	}

	@Override
	public void canceled() {

	}

	public abstract void gildeEntered(UUID gilde);

}