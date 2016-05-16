package dev.wolveringer.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import dev.wolveringer.bs.Main;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;
import net.md_5.bungee.BungeeCord;

public class GuiReportConfirm extends Gui{
	private String target;
	private String reson;
	private String moreInfo;
	
	public GuiReportConfirm(String target, String reson, String moreInfo) {
		super(1, "§aReporte §a"+target);
		this.target = target;
		this.reson = reson;
		this.moreInfo = moreInfo;
	}

	@Override
	public void build() {
		inv.setItem(0, new ItemStack(ItemBuilder.create(Material.BARRIER).name("§cSchließen").build()){
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
			}
		});
		inv.setItem(1, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).durbility(1).name("§6"+target).lore("§aGrund » §e"+reson).lore("§aWeitere Info » §e"+(moreInfo== null ? "§cNone":moreInfo)).build(), target));
		inv.setItem(4, ItemBuilder.create(Material.ANVIL).name("§c§lAchtung!").lore("§cTicket-Missbrach kann zum Ban führen!").build());
		
		inv.setItem(7, new ItemStack(ItemBuilder.create(159).name("§cAbbrechen").durbility(14).build()){
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
				getPlayer().sendMessage("§cAction canceled.");
			}
		});
		inv.setItem(8, ItemBuilder.create(159).name("§a§mBestätigen").lore("§cBitte warte noch 5 sekunden befor du").lore("§cden Report Bestätigen kannst.").durbility(11).build());
		
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, 9);
	}
	
	@Override
	public void active() {
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			public void run() {
				int loop = 6;
				while (loop-->0) {
					inv.setItem(8, ItemBuilder.create(159).name("§a§mBestätigen").lore("§cBitte warte noch "+loop+" secunden bevor du").lore("§cden Report Bestätigen kannst.").durbility(11).build());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				inv.setItem(8, new ItemStack(ItemBuilder.create(159).name("§aBestätigen").durbility(13).build()){
					@Override
					public void click(Click c) {
						getPlayer().closeInventory();
						Main.getDatenServer().getClient().createReport(Main.getDatenServer().getClient().getPlayerAndLoad(c.getPlayer().getName()).getPlayerId(), Main.getDatenServer().getClient().getPlayerAndLoad(target).getPlayerId(), reson, moreInfo);
						getPlayer().sendMessage("§aDu hast den Spieler §e"+target+" §areportet!");
						Main.getDatenServer().getClient().brotcastMessage("report.alert", "§aDer Spieler §e"+c.getPlayer().getName()+" §ahat den Spieler §e"+target+" §awegen §6"+reson+" "+(moreInfo == null ? "":"("+moreInfo+") reportet!"));
					}
				});
			}
		});
	}
}