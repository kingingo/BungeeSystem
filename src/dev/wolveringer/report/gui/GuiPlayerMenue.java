package dev.wolveringer.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.report.gui.admin.GuiViewOpenReports;
import dev.wolveringer.report.search.PlayerSearchMenue;
import dev.wolveringer.thread.ThreadFactory;
import net.md_5.bungee.api.ProxyServer;

public class GuiPlayerMenue extends Gui {

	public GuiPlayerMenue() {
		super(1,"§eReport");
	}
	
	@Override
	public void build() {
		inv.setItem(2, new ItemStack(ItemBuilder.create(372).name("§aReport a player").lore("§7Click to report a player.").glow().build()) {
			@Override
			public void click(Click c) {
				inv.setItem(2, ItemBuilder.create(372).name("§aPlease wait while we're loading all the players.").lore("§aBitte warte, während wir alle Spieler laden.").glow().build());
				ThreadFactory.getFactory().createThread(() -> {
					PlayerSearchMenue m = new PlayerSearchMenue(getPlayer()) {
						@Override
						public void playerEntered(String name) {
							if(name.equalsIgnoreCase(getPlayer().getName())){
								getPlayer().sendMessage("§cYou cant report yourself!");
								return;
							}
							LoadedPlayer splayer = Main.getDatenServer().getClient().getPlayerAndLoad(name);
							GuiSelectPlayerReportReson gui = new GuiSelectPlayerReportReson(name, splayer.hasNickname() ? PermissionManager.getManager().hasPermission(getPlayer(), "report.viewname") ? splayer.getName() : splayer.getNickname() : splayer.getName());
							gui.setPlayer(getPlayer());
							gui.openGui();
						}

						@Override
						public void canceled() {}
					};
					m.open();
				}).start();
			}
		});
		
		if(PermissionManager.getManager().hasPermission(getPlayer(), "report.help",false)){
			inv.setItem(4, ItemBuilder.create(Material.BOOK).name("§aView open reports").glow().lore("§7Click to see all open reports.").listener((c)->{
				Gui gui = new GuiViewOpenReports();
				//gui.setPlayer(c.getPlayer());
				//gui.active();
				//gui.openGui();
				switchToGui(gui);
			}).build());
		}
		
		inv.setItem(6, new ItemStack(ItemBuilder.create(Material.WATCH).name("§aView your open reports").lore("§7Click to see your open reports.").build()) {
			@Override
			public void click(Click c) {
				switchToGui(new GuiViewOwnReports());
			}
		});
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, 8);
	}
}
