package eu.epicpvp.bungee.system.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import net.md_5.bungee.BungeeCord;

public class GuiReportConfirm extends Gui {

	private String target;
	private String reason;
	private String moreInfo;
	private String targetNickname;

	public GuiReportConfirm(String target, String targetNickname, String reason, String moreInfo) {
		super(1, "§aReporte §a" + targetNickname);
		this.target = target;
		this.reason = reason;
		this.moreInfo = moreInfo;
		this.targetNickname = targetNickname;
	}

	@Override
	public void build() {
		inv.setItem(0, new ItemStack(ItemBuilder.create(Material.BARRIER).name("§cSchließen").build()) {
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
			}
		});
		inv.setItem(1, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).durability(1).name("§6" + targetNickname).lore("§aGrund » §e" + reason).lore("§aWeitere Info » §e" + (moreInfo == null ? "§cNone" : moreInfo)).build(), targetNickname));
		inv.setItem(4, ItemBuilder.create(Material.ANVIL).name("§c§lAchtung!").lore("§cTicket-Missbrach kann zum Ban führen!").build());

		inv.setItem(7, new ItemStack(ItemBuilder.create(159).name("§cAbbrechen").durability(14).build()) {
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
				getPlayer().sendMessage("§cAktion abgebrochen.");
			}
		});
		inv.setItem(8, ItemBuilder.create(159).name("§a§mBestätigen").lore("§cBitte warte noch§6 5§e Sekunden bevor du").lore("§cden Report bestätigen kannst.").durability(11).build());

		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 0, 9);
	}

	@Override
	public void active() {
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			public void run() {
				int loop = 6;
				while (--loop > 0) {
					inv.setItem(8, ItemBuilder.create(159).name("§a§mBestätigen").lore("§cBitte warte noch §6" + loop + " §cSekunden bevor du").lore("§cden Report bestätigen kannst.").durability(11).build());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				inv.setItem(8, new ItemStack(ItemBuilder.create(159).name("§aBestätigen").durability(13).build()) {
					@Override
					public void click(Click c) {
						getPlayer().closeInventory();
						Main.getDatenServer().getClient().createReport(Main.getDatenServer().getClient().getPlayerAndLoad(c.getPlayer().getName()).getPlayerId(), Main.getDatenServer().getClient().getPlayerAndLoad(target).getPlayerId(), reason, moreInfo);
						getPlayer().sendMessage("§aDu hast den Spieler §e" + target + " §areportet!");
						Main.getDatenServer().getClient().broadcastMessage("report.alert", "§6Der Spieler §e" + c.getPlayer().getName() + " §6hat den Spieler §e" + target + " §6wegen §e" + reason + (moreInfo == null ? "§6" : " §6(§e" + moreInfo + "§6)") + " reportet!");
					}
				});
			}
		});
	}
}
