package eu.epicpvp.bungee.system.report.gui.admin;

import java.util.ArrayList;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.item.ItemBuilder.ItemClickListener;
import eu.epicpvp.bungee.system.report.search.PlayerTextEnterMenue;
import eu.epicpvp.datenserver.definitions.report.ReportEntity;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;

public class GuiPlayerSupportTools extends Gui {

	private String player;
	private ArrayList<ReportEntity> reports;

	public GuiPlayerSupportTools(String player, ArrayList<ReportEntity> reports) {
		super(1, "§aEdit tool for " + player);
		this.player = player;
		this.reports = reports;
	}

	@Override
	public void build() {
		inv.setItem(1, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((c) -> switchToGui(new GuiViewPlayerReport(player, reports))).build());

		if (Main.getDatenServer().isActive() && Main.getDatenServer().getClient().getPlayerAndLoad(player).isOnlineSync()) {
			inv.setItem(3, ItemBuilder.create(Material.ENDER_PEARL).name("§aTeleport to player").glow().listener((c) -> {
				c.getPlayer().closeInventory();
				c.getPlayer().connect(BungeeCord.getInstance().getServerInfo(Main.getDatenServer().getClient().getPlayerAndLoad(player).getServer().getSync()));
			}).build());

			inv.setItem(5, ItemBuilder.create(Material.GLOWSTONE).name("§aKick Player").glow().listener(new ItemClickListener() {

				@Override
				public void click(Click c) {
					PlayerTextEnterMenue m = new PlayerTextEnterMenue(c.getPlayer()) {
						@Override
						public void textEntered(String name) {
							Main.getDatenServer().getClient().getPlayerAndLoad(player).kickPlayer("§cDu wurdest vom Netzwerk gekickt!\n§6Grund: §e" + ChatColor.translateAlternateColorCodes('&', name));
							c.getPlayer().closeInventory();
							c.getPlayer().sendMessage("§aPlayer kicked.");
						}

						@Override
						public void canceled() {}
					};
					m.open();
				}
			}).build());
		} else {
			inv.setItem(4, ItemBuilder.create(Material.MAGMA_CREAM).name(Main.getDatenServer().isActive() ? "§cDer Spieler ist Offline!" : " §cCant load information").build());
		}

		inv.setItem(7, ItemBuilder.create(Material.LAVA_BUCKET).name("§cClose reports").glow().listener((c) -> {
			switchToGui(new GuiReportCloseReason(reports, player));
		}).build());
		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 0, 6 * 9);
	}
}
