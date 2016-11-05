package eu.epicpvp.bungee.system.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.bungee.system.report.gui.admin.GuiViewOpenReports;
import eu.epicpvp.bungee.system.report.search.PlayerSearchMenue;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.thread.ThreadFactory;

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
							openSelectReportReasonGui(getPlayer(), name);
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
		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 0, 8);
	}

	public static void openSelectReportReasonGui(Player player, String targetName) {
		LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(targetName);
		GuiSelectPlayerReportReson gui = new GuiSelectPlayerReportReson(targetName, PermissionManager.getManager().hasPermission(player, "report.viewname") ? target.getName() : target.getFinalName());
		gui.setPlayer(player);
		gui.openGui();
	}
}
