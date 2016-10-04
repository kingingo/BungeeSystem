package dev.wolveringer.report.search;

import java.util.ArrayList;
import java.util.HashMap;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import eu.epicpvp.bungee.system.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import eu.epicpvp.bungee.system.gui.SearchMenue;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import dev.wolveringer.thread.ThreadFactory;

public abstract class PlayerSearchMenue extends SearchMenue {
	private HashMap<String, String> nicks = new HashMap<>();
	public PlayerSearchMenue(Player player) {
		super(player);
		ArrayList<String> out = new ArrayList<>();
		boolean unnicked = PermissionManager.getManager().hasPermission(player, "searchplayer.unnicked");
		ArrayList<String> loadafter = new ArrayList<>();

		for(String s : Main.getDatenServer().getPlayers()){
			LoadedPlayer lp = Main.getDatenServer().getClient().getPlayer(s);
			if(lp.isLoaded()){
				if(lp.hasNickname()){
					out.add(lp.getNickname());
					if(unnicked)
						out.add(lp.getName());
					nicks.put(lp.getNickname(), lp.getName());
				}
				else
					out.add(lp.getName());
			}else
				loadafter.add(s);
		}

		setAvariableEntities(out);

		ThreadFactory.getFactory().createThread(()->{
			int i = 0;
			for(String s : loadafter){
				LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(s);
				if(lp.hasNickname()){
					out.add(lp.getNickname());
					if(unnicked)
						out.add(lp.getName());
					nicks.put(lp.getNickname(), lp.getName());
				}
				else
					out.add(lp.getName());
				i++;
				if (i >= 10) {
					i = 0;
					PlayerSearchMenue.this.updateSelection();
					PlayerSearchMenue.this.redrawInventory();
				}
			}
			PlayerSearchMenue.this.updateSelection();
			PlayerSearchMenue.this.redrawInventory();
		}).start();
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
