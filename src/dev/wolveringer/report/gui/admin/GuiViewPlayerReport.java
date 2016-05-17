package dev.wolveringer.report.gui.admin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.bs.Main;
import dev.wolveringer.report.ReportEntity;
import dev.wolveringer.report.ReportWorker;

public class GuiViewPlayerReport extends Gui{
	private static final DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	String name;
	ArrayList<ReportEntity> reports;
	public GuiViewPlayerReport(String name, ArrayList<ReportEntity> reports) {
		super(6,"§eReports for: "+name);
		this.name = name;
		this.reports = reports;
	}
	
	@Override
	public void build() {
		LoadedPlayer own = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiViewOpenReports())).build());
		
		String server;
		ItemBuilder builder = ItemBuilder.create(Material.SKULL_ITEM).name("§6" + name).lore("§e");
		if((server = online())!=null){
			builder.lore("§aOnline auf Server §e"+server);
		}
		else
		{
			builder.lore("§cOffline");
			inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).name("§6" + name).lore("§e").lore("§cOffline").build(), name));
		}
		builder.lore("§aCurrunt workers:");
		for(ReportWorker w : reports.get(0).getWorkers()){
			builder.lore(" §7- §e"+Main.getDatenServer().getClient().getPlayer(w.getPlayerId()).getName());
		}
		inv.setItem(4, loadSkin(builder.build(), name));
		
		boolean alredyWorking = false;
		for(ReportWorker e : reports.get(0).getWorkers())
			if(e.getPlayerId() == own.getPlayerId() && e.getEnd() == -1)
				alredyWorking = true;
		if(alredyWorking){
			inv.setItem(8, ItemBuilder.create(159).name("§aReport tools").durbility(14).listener((c)-> switchToGui(new GuiPlayerSupportTools(name,reports))).build());
		}
		else
		{
			inv.setItem(8, ItemBuilder.create(159).name("§aReport annehmen").durbility(13).listener((c)->{
				for(ReportEntity e : reports){
					Main.getDatenServer().getClient().addReportWorker(e.getReportId(), own.getPlayerId());
					e.getWorkers().add(new ReportWorker(e.getReportId(), own.getPlayerId(), System.currentTimeMillis(), -1));
					Main.getDatenServer().getClient().sendMessage(e.getReporter(), "§aDein Spielerreport gegen §e"+name+" §awird von §e"+getPlayer().getName()+" §abearbeitet.");
				}
				Main.getDatenServer().getClient().brotcastMessage("report.alert", "§aDer Spieler §e"+c.getPlayer().getName()+" §ahat den report gegen §e"+name+" §aangenommen.");
				switchToGui(new GuiPlayerSupportTools(name,reports));
			}).build());
		}
		
		int pos = 9;
		for(ReportEntity e : reports){
			inv.setItem(pos++, createItem(e));
		}
		
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, 6*9);
	}
	
	private Item createItem(ReportEntity e){
		return ItemBuilder.create(Material.PAPER).name("§6"+e.getReson()).lore("§aExtra info §7» §e"+e.getInfos()).lore("§aDatum/Zeit §7» §e"+formatter.format(new Date(e.getTime()))).lore("§aReporter §7» §e"+Main.getDatenServer().getClient().getPlayerAndLoad(e.getReporter()).getName()).build();
	}
	
	private String online(){
		String server;
		boolean online =  (server = Main.getDatenServer().getClient().getPlayerAndLoad(name).getServer().getSync()) != null;
		return server;
	}
}
