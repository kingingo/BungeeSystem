package eu.epicpvp.bungee.system.guild.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.gilde.GildSectionMoney;
import eu.epicpvp.datenserver.definitions.gilde.MoneyLogRecord;

public class GuiGildeMoneyOverview extends Gui {

	public static interface LogFilter {

		public boolean accept(MoneyLogRecord record);
	}

	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss");

	private static enum SortType {
		NAME,
		TIME_DECREASING,
		TIME_INCREASING,
		VALUE_DECREASING,
		VALUE_INCREASING;
	}

	private final GildSectionMoney money;

	private SortType type;

	private int page = 0;
	private List<MoneyLogRecord> records;
	private LogFilter filter = null;

	public GuiGildeMoneyOverview(GildSectionMoney money) {
		super(6, "§a" + money.getHandle().getType().getDisplayName() + " §6» §aMoney");
		this.money = money;
		this.records = new ArrayList<>(money.getHistory());
	}

	public GuiGildeMoneyOverview(GildSectionMoney money, LogFilter filter) {
		this(money);
		this.filter = filter;
		if (filter != null) {
			ArrayList<MoneyLogRecord> nr = new ArrayList<>();
			for (MoneyLogRecord r : records)
				if (filter.accept(r))
					nr.add(r);
			records = nr;
		}
	}

	@Override
	public void build() {
		updateSortType(SortType.TIME_DECREASING);
		updateInventory();
	}

	private void updateInventory() {
		drawBorder();
		drawSection();
	}

	private void drawBorder() {
		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 36, 44);
		inv.setItem(45, ItemBuilder.create(Material.ARROW).name("§7Vorherige Seite " + (page != 0 ? "(" + (page - 1) + ")" : "")).listener(() -> {
			if (page > 0) {
				page--;
				updateInventory();
			}
		}).glow(page == 0).build());

		inv.setItem(47, ItemBuilder.create(101).name("§aElemente nach Namen sortiert").glow(type == SortType.NAME).listener((c) -> {
			if (c.getItem() == null)
				return;
			if (!c.getItem().getItemMeta().hasGlow()) {
				updateSortType(SortType.NAME);
				updateInventory();
			}
		}).build());

		inv.setItem(48, ItemBuilder.create(101).name("§aElemente nach Zeit sortiert (abnehmend)").glow(type == SortType.TIME_DECREASING).listener((c) -> {
			if (c.getItem() == null)
				return;
			if (!c.getItem().getItemMeta().hasGlow()) {
				updateSortType(SortType.TIME_DECREASING);
				updateInventory();
			}
		}).build());

		inv.setItem(49, ItemBuilder.create(101).name("§aElemente nach Zeit sortiert (zunehmend)").glow(type == SortType.TIME_INCREASING).listener((c) -> {
			if (c.getItem() == null)
				return;
			if (!c.getItem().getItemMeta().hasGlow()) {
				updateSortType(SortType.TIME_INCREASING);
				updateInventory();
			}
		}).build());

		inv.setItem(50, ItemBuilder.create(101).name("§aElemente nach Geld sortiert (abnehmend)").glow(type == SortType.VALUE_DECREASING).listener((c) -> {
			if (c.getItem() == null)
				return;
			if (!c.getItem().getItemMeta().hasGlow()) {
				updateSortType(SortType.VALUE_DECREASING);
				updateInventory();
			}
		}).build());

		inv.setItem(51, ItemBuilder.create(101).name("§aElemente nach Geld sortiert (zunehmend)").glow(type == SortType.VALUE_INCREASING).listener((c) -> {
			if (c.getItem() == null)
				return;
			if (!c.getItem().getItemMeta().hasGlow()) {
				updateSortType(SortType.VALUE_INCREASING);
				updateInventory();
			}
		}).build());

		inv.setItem(53, ItemBuilder.create(Material.ARROW).name("§7Nächste Seite " + (page * 4 * 9 > records.size() ? "(" + (page + 1) + ")" : "")).listener(() -> {
			if (page > 0) {
				page++;
				updateInventory();
			}
		}).glow((page + 1) * 4 * 9 > records.size()).build());
	}

	private void drawSection() {
		for (int i = 0; i < 4 * 9; i++)
			if (page * 4 * 9 + i < records.size())
				inv.setItem(i, buildItem(records.get(page * 4 * 9 + i)));
	}

	private Item buildItem(MoneyLogRecord record) {
		ItemBuilder builder = ItemBuilder.create(Material.SKULL_ITEM).durability(3);
		LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(record.getPlayerId());
		builder.name("§aSpieler: §b" + lp.getName());
		builder.lore("§aBetrag: §b" + record.getAmount());
		builder.lore("§aDatum: §b" + FORMAT.format(new Date(record.getDate())));
		builder.lore("§aGrund: §b" + record.getMessage());
		return loadSkin(builder.build(), lp.getName());
	}

	private void updateSortType(SortType _new) {
		if (type == _new)
			return;
		type = _new;
		switch (type) {
			case TIME_DECREASING:
				Collections.sort(records, (o1, o2) -> Long.compare(o2.getDate(), o1.getDate()));
				break;
			case TIME_INCREASING:
				Collections.sort(records, (o1, o2) -> Long.compare(o1.getDate(), o2.getDate()));
				break;
			case NAME:
				Collections.sort(records, (o1, o2) -> Main.getDatenServer().getClient().getPlayerAndLoad(o1.getPlayerId()).getName().compareTo(Main.getDatenServer().getClient().getPlayerAndLoad(o2.getPlayerId()).getName()));
				break;
			case VALUE_DECREASING:
				Collections.sort(records, (o1, o2) -> Long.compare(o2.getAmount(), o1.getAmount()));
				break;
			case VALUE_INCREASING:
				Collections.sort(records, (o1, o2) -> Long.compare(o2.getAmount(), o1.getAmount()));
				break;
			default:
				break;
		}
	}
}
