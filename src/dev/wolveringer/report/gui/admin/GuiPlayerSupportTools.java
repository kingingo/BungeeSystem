package dev.wolveringer.report.gui.admin;

import java.util.ArrayList;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.item.ItemBuilder.ItemClickListener;
import dev.wolveringer.bs.Main;
import dev.wolveringer.report.ReportEntity;
import dev.wolveringer.report.search.PlayerTextEnterMenue;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;

public class GuiPlayerSupportTools extends Gui {
	private String player;
	private ArrayList<ReportEntity> reports;
	
	public GuiPlayerSupportTools(String player,ArrayList<ReportEntity> reports) {
		super(1,"§aEdit tool for "+player);
		this.player = player;
		this.reports = reports;
	}
	
	@Override
	public void build() {
		inv.setItem(1, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((c)->switchToGui(new GuiViewPlayerReport(player, reports))).build());
		inv.setItem(3, ItemBuilder.create(Material.ENDER_PEARL).name("§aTeleport to player").glow().listener((c)->{
			c.getPlayer().closeInventory();
			c.getPlayer().connect(BungeeCord.getInstance().getServerInfo(Main.getDatenServer().getClient().getPlayerAndLoad(player).getServer().getSync()));
		}).build());
		
		inv.setItem(5, ItemBuilder.create(Material.GLOWSTONE).name("§aKick Player").glow().listener(new ItemClickListener() {

			@Override
			public void click(Click c) {
				PlayerTextEnterMenue m = new PlayerTextEnterMenue(c.getPlayer()) {
					@Override
					public void textEntered(String name) {
						Main.getDatenServer().getClient().getPlayerAndLoad(player).kickPlayer("§cDu wurdest vom Netzwerk gekickt!\n§6Grund: §e"+ChatColor.translateAlternateColorCodes('&', name));
						c.getPlayer().closeInventory();
						c.getPlayer().sendMessage("§aPlayer kicked.");
					}
					@Override
					public void canceled() {}
				};
				m.open();
			}
			
		}).build());
		inv.setItem(7, ItemBuilder.create(Material.LAVA_BUCKET).name("§cClose reports").glow().listener((c)->{
			c.getPlayer().closeInventory();
			LoadedPlayer own = Main.getDatenServer().getClient().getPlayerAndLoad(c.getPlayer().getName());
			for(ReportEntity e : reports){
				Main.getDatenServer().getClient().closeReport(e);
				Main.getDatenServer().getClient().closeReportWorker(e.getReportId(), own.getPlayerId());
				Main.getDatenServer().getClient().sendMessage(e.getReporter(), "§aDein Spielerreport gegen §e"+player+" §awurde von §e"+getPlayer().getName()+" §abearbeitet.");
			}
			Main.getDatenServer().getClient().brotcastMessage("report.alert", "§aDer Spieler §e"+getPlayer().getName()+" §ahat den Report gegen §e"+player+" §ageschlossen.");
			c.getPlayer().sendMessage("§aReport closed.");
		}).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, 6*9);
	}
}
