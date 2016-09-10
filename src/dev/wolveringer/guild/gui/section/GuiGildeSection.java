package dev.wolveringer.guild.gui.section;

import java.util.ArrayList;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gamestats.Statistic;
import dev.wolveringer.gilde.GildSection;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.gui.GuiStatusPrint;
import dev.wolveringer.gui.GuiWaiting;
import dev.wolveringer.guild.gui.GuiGildeAdminOverview;
import dev.wolveringer.guild.gui.GuiGildeGroupOverview;
import dev.wolveringer.guild.gui.GuiGildeMemberManager;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.thread.ThreadFactory;

public class GuiGildeSection extends Gui {
	private GildSection section;

	public GuiGildeSection(GildSection section) {
		super(6, "§a" + section.getType().getDisplayName() + " §7>> §eOverview");
		this.section = section;
	}
	
	@Override
	public void build() {
		buildDefault();
	}

	protected void buildDefault(){
		inv.setItem(4, buildStats());
		final LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
		if(section.getHandle().getOwnerId() == lplayer.getPlayerId())
			inv.setItem(8, ItemBuilder.create(Material.STAINED_GLASS_PANE).durbility(14).name("§7» §cBereich Deaktivieren").listener((Click c) -> {
				GuiWaiting waiting = new GuiWaiting("§aActivate gild section", "§aPlease wait");
				waiting.setPlayer(getPlayer()).openGui();
				ThreadFactory.getFactory().createThread(()->{
					if(section.isActive()){
						section.setActive(false);
					}
					waiting.waitForMinwait(1500);
					new GuiStatusPrint(5, ItemBuilder.create().material(Material.EMERALD).name("§aGild-Section deaktiviert.").build()) {
						@Override
						public void onContinue() {
							new GuiGildeAdminOverview(getPlayer(), section.getHandle()).setPlayer(getPlayer()).openGui();
						}
					}.setPlayer(getPlayer()).openGui();
				}).start();
			}).build());
		inv.setItem(19, ItemBuilder.create(Material.GOLD_BLOCK).name("§7» §6Clan Bank").lore("§aKlicke hier für weitere Infos").listener((Click c) -> c.getPlayer().sendMessage("§cNot implemented!")).build());
		inv.setItem(21, ItemBuilder.create(Material.SKULL_ITEM).name("§7» §6Clan Mitglieder").lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildeMemberManager(section.getPermission()))).build());
		inv.setItem(23, ItemBuilder.create(Material.SKULL_ITEM).name("§7» §6Clan Gruppen").lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildeGroupOverview(section))).build());
		
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
