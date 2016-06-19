package dev.wolveringer.guild.gui;

import java.util.ArrayList;

import org.apache.commons.lang3.ObjectUtils;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.gamestats.Statistic;
import dev.wolveringer.gilde.GildSection;
import dev.wolveringer.gilde.Gilde;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;
import io.netty.util.internal.ObjectUtil;

public class GuiGildeSection extends Gui {
	private GildSection section;

	public GuiGildeSection(GildSection section) {
		super(6, "§a" + section.getType().getDisplayName() + " §7>> §eOverview");
	}

	@Override
	public void build() {

		inv.setItem(4, buildStats());
		inv.setItem(8, ItemBuilder.create(Material.ARMOR_STAND).name("§7» §6Übersicht").listener((Click c) -> switchToGui(new GuiGildeOverview(section.getHandle()))).build());
		inv.setItem(19, ItemBuilder.create(Material.GOLD_BLOCK).name("§7» §6Clan Bank").lore("§aKlicke hier für weitere Infos").listener((Click c) -> c.getPlayer().sendMessage("§cNot implemented!")).build());
		inv.setItem(21, ItemBuilder.create(Material.SKULL_ITEM).name("§7» §6Clan Mitglieder").lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildeMemberManager(section.getPermission()))).build());
		inv.setItem(23, ItemBuilder.create(Material.SKULL_ITEM).name("§7» §6Clan Gruppen").lore("§aKlicke hier für weitere Infos").listener((Click c) -> c.getPlayer().sendMessage("§cNot implemented!")).build());
		
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cSchließen").listener((Click c) -> c.getPlayer().closeInventory()).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
	}

	private Item buildStats() {
		ItemBuilder builder = new ItemBuilder(339);
		builder.name("§a" + section.getType().getDisplayName() + " Stats");
		builder.glow();
		builder.lore("§cLoading stats");
		Item item = builder.build();

		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				ArrayList<String> lore = new ArrayList<>();
				for (Statistic s : section.getStatsPlayer().getStats(section.getType().getStatsType()).getSync()) {
					lore.add("§a" + s.getStatsKey().getContraction() + " §7» §e" + toString(s.getValue(), "§cNull Value"));
				}
				item.getItemMeta().setLore(lore);
				item.getItemMeta().setGlow(false);
			}
			private String toString(Object obj, String nullStr) {
				return ((obj == null) ? nullStr : obj.toString());
			}
		}).start();
		return item;
	}
}
