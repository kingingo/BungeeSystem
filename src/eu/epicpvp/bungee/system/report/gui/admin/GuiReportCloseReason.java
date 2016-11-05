package eu.epicpvp.bungee.system.report.gui.admin;

import java.util.ArrayList;

import dev.wolveringer.BungeeUtil.Material;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.report.ReportEntity;
import eu.epicpvp.datenserver.definitions.report.ReportState;

public class GuiReportCloseReason extends Gui {

	private ArrayList<ReportEntity> reports;
	private String player;

	public GuiReportCloseReason(ArrayList<ReportEntity> reports, String player) {
		super(1, "§cClose report");
		this.reports = reports;
		this.player = player;
	}

	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cSchließen").listener((c) -> getPlayer().closeInventory()).build());
		inv.setItem(2, ItemBuilder.create(159).name(reports.size() == 1 ? "§aBerechtigter Report" : "§aBerechtigte Reports").listener((c) -> {
			closeReports(ReportState.POSITIVE_REPORT_CLOSED.ordinal());
		}).durability(5).build());
		inv.setItem(4, ItemBuilder.create(159).name(reports.size() == 1 ? "§eNicht nachprüfbarer Report" : "§aNicht nachprüfbare Reports").listener((c) -> {
			closeReports(ReportState.NOT_PROOFABLE_CLOSED.ordinal());
		}).durability(4).build());
		inv.setItem(6, ItemBuilder.create(159).name(reports.size() == 1 ? "§cUnberechtigter Report" : "§cUnberechtigte Reports").listener((c) -> {
			closeReports(ReportState.NEGATIVE_REPORT_CLOSED.ordinal());
		}).durability(14).build());
		inv.setItem(8, ItemBuilder.create(Material.GLOWSTONE_DUST).name("§aSchließe die Reports gegen §e" + player).listener((c) -> getPlayer().closeInventory()).build());
		fill(ItemBuilder.create(160).durability(7).name("§7").build());
	}

	private void closeReports(int state) {
		getPlayer().closeInventory();
		LoadedPlayer own = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
		for (ReportEntity e : reports) {
			Main.getDatenServer().getClient().closeReport(e, state);
			Main.getDatenServer().getClient().closeReportWorker(e.getReportId(), own.getPlayerId());
			Main.getDatenServer().getClient().sendMessage(e.getReporter(), "§aDein Spielerreport gegen §e" + player + " §awurde von §e" + getPlayer().getName() + " §abearbeitet.");
		}
		String reason = "???";
		if (state == ReportState.POSITIVE_REPORT_CLOSED.ordinal()) {
			reason = "berechtigt";
		} else if (state == ReportState.NOT_PROOFABLE_CLOSED.ordinal()) {
			reason = "nicht nachprüfbar";
		} else if (state == ReportState.NEGATIVE_REPORT_CLOSED.ordinal()) {
			reason = "unberechtigt";
		}
		Main.getDatenServer().getClient().broadcastMessage("report.alert", "§6Der Spieler §e" + getPlayer().getName() + "§6 hat den Report gegen §e" + player + "§6 mit dem Grund §e" + reason + "§c geschlossen§6.");
		getPlayer().sendMessage(reports.size() == 1 ? "§aReport closed." : "§aReports closed.");
	}
}
