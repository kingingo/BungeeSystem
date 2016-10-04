package eu.epicpvp.bungee.system.booster;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.api.inventory.Inventory;
import dev.wolveringer.api.inventory.InventoryType;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import lombok.Setter;

public abstract class GuiIntegerSelect {
	private Player player;
	private String title;
	private Inventory inv;
	private int currunt = 0;
	private String centerName = "§e%s";
	@Setter
	private String notPossible = "§cDiese Zahl ist nicht möglich!";
	private int mode = 0;

	public GuiIntegerSelect setMode(int mode) {
		this.mode = mode;
		return this;
	}

	public int getMode() {
		return mode;
	}

	public GuiIntegerSelect(Player player, String title) {
		this.player = player;
		this.title = title;
	}

	public GuiIntegerSelect(Player player, String title, int start) {
		this.player = player;
		this.title = title;
		this.currunt = start;
	}

	public GuiIntegerSelect(Player player, String title, int start, String centername) {
		this.player = player;
		this.title = title;
		this.currunt = start;
		this.centerName = centername;
	}

	public void open() {
		switch (mode) {
		case 0:
			inv = new Inventory(InventoryType.Hopper, title);
			inv.setItem(0, buildItem(77, -10));
			inv.setItem(1, buildItem(143, -1));
			inv.setItem(3, buildItem(143, 1));
			inv.setItem(4, buildItem(77, 10));
			updateCenter();
			player.openInventory(inv);
			break;
		case 1:
			inv = new Inventory(9, title);
			inv.setItem(0, ItemBuilder.create(160).durbility(7).name("§7").build());
			inv.setItem(1, buildItem(337, -100));
			inv.setItem(2, buildItem(77, -10));
			inv.setItem(3, buildItem(143, -1));
			inv.setItem(5, buildItem(143, 1));
			inv.setItem(6, buildItem(77, 10));
			inv.setItem(7, buildItem(337, 100));
			inv.setItem(8, ItemBuilder.create(160).durbility(7).name("§7").build());
			updateCenter();
			player.openInventory(inv);
			break;
		case 2:
			inv = new Inventory(9, title);
			inv.setItem(0, buildItem(341, -1000));
			inv.setItem(1, buildItem(337, -100));
			inv.setItem(2, buildItem(77, -10));
			inv.setItem(3, buildItem(143, -1));
			inv.setItem(5, buildItem(143, 1));
			inv.setItem(6, buildItem(77, 10));
			inv.setItem(7, buildItem(337, 100));
			inv.setItem(8, buildItem(341, 1000));
			updateCenter();
			player.openInventory(inv);
			break;
		default:
			break;
		}
	}

	private void update() {
		switch (mode) {
		case 0:
			inv.setItem(0, buildItem(77, -10));
			inv.setItem(1, buildItem(143, -1));
			inv.setItem(3, buildItem(143, 1));
			inv.setItem(4, buildItem(77, 10));
			updateCenter();
			break;
		case 1:
			inv.setItem(0, ItemBuilder.create(160).durbility(7).name("§7").build());
			inv.setItem(1, buildItem(337, -100));
			inv.setItem(2, buildItem(77, -10));
			inv.setItem(3, buildItem(143, -1));
			inv.setItem(5, buildItem(143, 1));
			inv.setItem(6, buildItem(77, 10));
			inv.setItem(7, buildItem(337, 100));
			inv.setItem(8, ItemBuilder.create(160).durbility(7).name("§7").build());
			updateCenter();
			break;
		case 2:
			inv.setItem(0, buildItem(341, -1000));
			inv.setItem(1, buildItem(337, -100));
			inv.setItem(2, buildItem(77, -10));
			inv.setItem(3, buildItem(143, -1));
			inv.setItem(5, buildItem(143, 1));
			inv.setItem(6, buildItem(77, 10));
			inv.setItem(7, buildItem(337, 100));
			inv.setItem(8, buildItem(341, 1000));
			updateCenter();
			break;
		default:
			break;
		}
	}

	private Item buildItem(int id, int action) {
		return ItemBuilder.create(id).name((action > 0 ? "§a" : "§c") + (isNumberAllowed(currunt + action) ? "" : "§m") + action).glow(!isNumberAllowed(currunt + action)).listener((c) -> {
			if (isNumberAllowed(currunt + action))
				currunt += action;
			update();
		}).build();
	}

	private synchronized void updateCenter() {
		inv.setItem(mode != 0 ? 4 : 2, ItemBuilder.create(399).name(String.format(centerName, currunt)).lore(isNumberAllowed(currunt) ? "§eKlicke um fortzufahren." : notPossible).listener((c) -> {
			if (isNumberAllowed(currunt)) {
				numberEntered(currunt);
			}
		}).build());
	}

	public abstract boolean isNumberAllowed(int number);

	public abstract void numberEntered(int number);
}
