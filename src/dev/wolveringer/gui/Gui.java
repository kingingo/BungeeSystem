package dev.wolveringer.gui;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.gameprofile.SkinFactory;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.itemmeta.SkullMeta;
import dev.wolveringer.animations.inventory.InventoryViewChangeAnimations;
import dev.wolveringer.animations.inventory.InventoryViewChangeAnimations.AnimationType;
import dev.wolveringer.api.inventory.Inventory;
import dev.wolveringer.api.inventory.ItemContainer;
import dev.wolveringer.bs.Main;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.skin.Skin;
import dev.wolveringer.skin.SteveSkin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;

@AllArgsConstructor
public abstract class Gui {
	@Getter
	@Setter
	private Player player;
	@Getter
	protected Inventory inv;
	private ItemContainer container;
	private boolean active = false;
	
	public Gui(int rows, String name) {
		this.inv = new Inventory(rows * 9, name);
	}
	public int fill(Item is) {
		return fill(is, 0);
	}
	public int fill(Item is, int from) {
		return fill(is, from, -1);
	}
	public int fill(Item is, int from, int to) {
		return fill(is, from, to, false);
	}

	public int fill(Item is, int from, int to, boolean ignoreFull) {
		int filled = 0;
		if (to == -1)
			to = 150;
		for (int i = from; i <= Math.min(inv.getSlots() - 1, to); i++) {
			if (ignoreFull || inv.getItem(i) == null) {
				inv.setItem(i, is);
				filled++;
			}
		}
		return filled;
	}

	public void openGui() {
		build();
		active = true;
		active();
		player.openInventory(inv);
	}

	protected Item loadSkin(Item is, String name) {
		if(name == null)
			return is;
		final Item old = new Item(is);
		try{
			Skin skin = Main.getSkinManager().getIfLoaded(name);
			if (skin != null) {
				is.setDurability((short) 3);
				((SkullMeta) is.getItemMeta()).setSkin(toBungeeUtilSkin(skin));
				int s = inv.getSlot(is);
				if (s != -1)
					inv.setItem(s, is);
				s = inv.getSlot(old);
				if (s != -1)
					inv.setItem(s, is);
				if (container != null) {
					s = container.getSlot(old);
					if (s != -1)
						container.setItem(s, is);
					s = container.getSlot(is);
					if (s != -1)
						container.setItem(s, is);
				}
				return is;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				try{
					Skin skin = Main.getSkinManager().getOrLoad(name);
					is.setDurability((short) 3);
					((SkullMeta) is.getItemMeta()).setSkin(toBungeeUtilSkin(skin));
					int s = inv.getSlot(is);
					if (s != -1)
						inv.setItem(s, is);
					s = inv.getSlot(old);
					if (s != -1)
						inv.setItem(s, is);
					if (container != null) {
						s = container.getSlot(old);
						if (s != -1)
							container.setItem(s, is);
						s = container.getSlot(is);
						if (s != -1)
							container.setItem(s, is);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		return is;
	}

	private dev.wolveringer.BungeeUtil.gameprofile.Skin toBungeeUtilSkin(Skin skin) {
		dev.wolveringer.BungeeUtil.gameprofile.Skin _new = SkinFactory.createEmptySkin();
		if (!(skin instanceof SteveSkin)) {
			_new = SkinFactory.createSkin(skin.getRawData(), skin.getSignature());
		}
		return _new;
	}

	public abstract void build();

	public void switchToGui(Gui gui) {
		gui.player = player;
		gui.build();
		ItemContainer items = new ItemContainer(gui.inv.getContains());
		gui.container = items;
		String newName = gui.inv.getName();
		gui.inv = this.inv;
		gui.player = this.player;
		active = false;
		deactive();
		InventoryViewChangeAnimations.runAnimation(AnimationType.SCROLL_UP, this.inv, items, newName, ItemBuilder.create(160).durbility(7).name("§7").build(), 100);
		gui.active = true;
		gui.active();
	}

	public void active() {
	};

	public void deactive() {
	};
	public boolean isActive(){
		return active && getPlayer() != null && getPlayer().getInventoryView() != null;
	}
}