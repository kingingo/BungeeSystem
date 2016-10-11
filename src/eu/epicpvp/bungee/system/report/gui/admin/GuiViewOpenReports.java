package eu.epicpvp.bungee.system.report.gui.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.listener.PlayerJoinListener;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.report.gui.GuiPlayerMenue;
import eu.epicpvp.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import eu.epicpvp.datenclient.client.PacketHandleErrorException;
import eu.epicpvp.datenserver.definitions.hashmaps.InitHashMap;
import eu.epicpvp.datenserver.definitions.report.ReportEntity;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class GuiViewOpenReports extends Gui implements Runnable{
	private static final int REPORTS_PER_SIDE = 36;
	private ScheduledTask pid;
	private Item fillItem = ItemBuilder.create(160).durability(7).name("§7").build();
	private ReportEntity[] entites;
	private List<Entry<Integer, ArrayList<ReportEntity>>> reportEntities;
	private HashMap<Integer, Long> minTimes;
	private int side;

	public GuiViewOpenReports() {
		super(6, "§aOpen reports");
	}
	Item is;
	@Override
	public void build() {
		inv.setItem(0, new ItemStack(ItemBuilder.create(Material.BARRIER).name("§cZurück").build()){
			@Override
			public void click(Click c) {
				switchToGui(new GuiPlayerMenue());
			}
		});

		inv.setItem(4, is = ItemBuilder.create(Material.EMERALD).name("§6Open reports: Loading....").build());
		//11 13 15
		//29 31 33
		//47 49 51

		fill(fillItem, 0, 6*9);
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				try{
					afterload(Main.getDatenServer().getClient().getReportEntity(RequestType.OPEN_REPORTS, -1).getSync());
				}catch(Exception e){
					if(e instanceof PacketHandleErrorException){
						PacketHandleErrorException ex = (PacketHandleErrorException) e;
						ItemBuilder builder = ItemBuilder.create(160).durability(14).name("§cError while loading report list!");
						builder.lore("§cErrors: ");
						for(int i = 0;i<ex.getErrors().length;i++)
							builder.lore(" §7- §c"+ex.getErrors()[i].getId()+" -> "+ex.getErrors()[i].getMessage());
						fill(builder.build(), 1*9, -1,true);
					}
					else
						fill(ItemBuilder.create(160).durability(14).name("§cError while loading report list!").lore("§cException: "+e.getMessage()).build(), 1*9, -1,true);
				}
			}
		});
	}

	private void afterload(ReportEntity...entities){
		is.setType(Material.DIAMOND);
		is.getItemMeta().setDisplayName("§6Open reports: "+entities.length);
		setItemLater(4, ItemBuilder.create(Material.DIAMOND).name("§6Open reports: "+entities.length).build());
		this.entites = entities;

		HashMap<Integer, ArrayList<ReportEntity>> amauths = new InitHashMap<Integer, ArrayList<ReportEntity>>() {
			@Override
			public ArrayList<ReportEntity> defaultValue(Integer key) {
				return new ArrayList<>();
			}
		};
		for(ReportEntity e : entites)
			amauths.get(e.getTarget()).add(e);
		List<Entry<Integer, ArrayList<ReportEntity>>> reports = new ArrayList<>(amauths.entrySet());
		minTimes = new HashMap<>();

		//Calculate -> open until
		for(Entry<Integer, ArrayList<ReportEntity>> e : reports){
			long min = System.currentTimeMillis();
			for(ReportEntity re : e.getValue())
				if(re.getTime()<min)
					min = re.getTime();
			minTimes.put(e.getKey(), min);
		}

		//Sort
		Collections.sort(reports, new Comparator<Entry<Integer, ArrayList<ReportEntity>>>() {
			@Override
			public int compare(Entry<Integer, ArrayList<ReportEntity>> o1, Entry<Integer, ArrayList<ReportEntity>> o2) {
				if(o1.getValue().size() == o2.getValue().size()){
					return Long.compare(minTimes.get(o2.getKey()), minTimes.get(o1.getKey()));
				}
				else
					return Integer.compare(o2.getValue().size(), o1.getValue().size());
			}
		});
		this.reportEntities = reports;

		printReportItems();
	}
	private void printReportItems(){
		if(entites == null || !isActive())
			return;
		inv.disableUpdate();


		int start = 9;
		int pos = 0;
		List<Entry<Integer, ArrayList<ReportEntity>>> reports = reportEntities.subList(side*REPORTS_PER_SIDE, Math.min(((side+1)*REPORTS_PER_SIDE), reportEntities.size()));
		//print
		for(Entry<Integer, ArrayList<ReportEntity>> e : reports){
			setItemLater(pos+start, createReportInfo(e.getKey(), e.getValue(), minTimes.get(e.getKey())));
			pos++;
		}
		fill(fillItem, start+pos, inv.getSlots()-10, true);

		setItemLater(45, new ItemStack(ItemBuilder.create(Material.ARROW).name("§aVorherige seite").build()){
			@Override
			public void click(Click c) {
				if(side > 0){
					side--;
					printReportItems();
					getItemMeta().setGlow((side > 0));
				}
			}
		});
		setItemLater(53, new ItemStack(ItemBuilder.create(Material.ARROW).name("§aNächste seite").build()){
			@Override
			public void click(Click c) {
				if(reportEntities.size()>(side+1)*REPORTS_PER_SIDE){
					side++;
					printReportItems();
					getItemMeta().setGlow((reportEntities.size()>(side+1)*36));
				}
			}
		});

		inv.enableUpdate();
	}
	private Item createReportInfo(int playerId,ArrayList<ReportEntity> reports,long minTime){
		if(playerId == -1)
			return ItemBuilder.create(160).durability(14).name("§cPlayerId -> "+playerId).build();
		String player = Main.getDatenServer().getClient().getPlayerAndLoad(playerId).getName();
		int workerAmount = reports.get(0).getWorkers().size();
		ItemBuilder builder = ItemBuilder.create(Material.SKULL_ITEM)
				.name("§a" + reports.size() + " Report" + (reports.size() == 1 ? "" : "s") + " gegen §e" + player)
				.amouth(reports.size())
				.listener((c) -> switchToGui(new GuiViewPlayerReport(player, reports)))
				.lore("§aOffen seit: §e" + PlayerJoinListener.getDurationBreakdown(System.currentTimeMillis() - minTime))
				.lore("§aBearbeiter: " + (workerAmount == 0 ? "§ckeine" : "§e" + workerAmount));
		for (ReportEntity report : reports)
			builder.lore("§6" + report.getReson()).lore("§a  Reporter §7» §e" + Main.getDatenServer().getClient().getPlayerAndLoad(report.getReporter()).getName());
		return loadSkin(builder.build(), player);
	}

	@Override
	public void run() {
		while (isActive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if(!isInAnimation())
				printReportItems();
		}
	}

	@Override
	public void active() {
		pid = BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), this);
	}
	@Override
	public void deactive() {
		pid.cancel();
	}
}
