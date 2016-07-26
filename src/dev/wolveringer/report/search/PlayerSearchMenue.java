package dev.wolveringer.report.search;

import java.util.ArrayList;
import java.util.HashMap;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gui.SearchMenue;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.permission.PermissionManager;

public abstract class PlayerSearchMenue extends SearchMenue {
	private HashMap<String, String> nicks = new HashMap<>();
	public PlayerSearchMenue(Player player) {
		super(player);
		ArrayList<String> out = new ArrayList<>();
		boolean unnicked = PermissionManager.getManager().hasPermission(player, "searchplayer.unnicked");
		for(String s : Main.getDatenServer().getPlayers()){
			LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(s);
			if(lp.hasNickname()){
				out.add(lp.getNickname());
				if(unnicked)
					out.add(lp.getName());
				nicks.put(lp.getNickname(), lp.getName());
			}
			else
				out.add(lp.getName());
		}
		setAvariableEntities(out);
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
		return loadSkin(ItemBuilder.create(Material.SKULL_ITEM).durbility(1).name("§e"+(nicks.containsKey(name.toLowerCase()) ? nicks.get(name.toLowerCase()) : name)).build(), nicks.containsKey(name.toLowerCase()) ? nicks.get(name.toLowerCase()) : name, slot);
	}

	@Override
	public void inputEntered(String name) {
		playerEntered(nicks.containsKey(name.toLowerCase()) ? nicks.get(name.toLowerCase()) : name);
	}
}
