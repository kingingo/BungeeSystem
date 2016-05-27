package dev.wolveringer.report.gui.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.client.PacketHandleErrorException;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.report.ReportEntity;
import dev.wolveringer.report.gui.GuiPlayerMenue;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class GuiViewOpenReports extends Gui implements Runnable{
	private ScheduledTask pid;
	private Item fillItem = ItemBuilder.create(160).durbility(7).name("§7").build();
	private ReportEntity[] entites;
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
						ItemBuilder builder = ItemBuilder.create(160).durbility(14).name("§cError while loading report list!");
						builder.lore("§cErrors: ");
						for(int i = 0;i<ex.getErrors().length;i++)
							builder.lore(" §7- §c"+ex.getErrors()[i].getId()+" -> "+ex.getErrors()[i].getMessage());
						fill(builder.build(), 1*9, -1,true);
					}
					else
						fill(ItemBuilder.create(160).durbility(14).name("§cError while loading report list!").lore("§cException: "+e.getMessage()).build(), 1*9, -1,true);
				}
			}
		});
	}
	
	private void afterload(ReportEntity...entities){
		is.setType(Material.DIAMOND);
		is.getItemMeta().setDisplayName("§6Open reports: "+entities.length);
		inv.setItem(4, ItemBuilder.create(Material.DIAMOND).name("§6Open reports: "+entities.length).build());
		this.entites = entities;
		printReportItems();
	}
	private void printReportItems(){
		if(entites == null)
			return;
		inv.disableUpdate();
		HashMap<Integer, ArrayList<ReportEntity>> amauths = new InitHashMap<Integer, ArrayList<ReportEntity>>() {
			@Override
			public ArrayList<ReportEntity> defaultValue(Integer key) {
				return new ArrayList<>();
			}
		};
		for(ReportEntity e : entites)
			amauths.get(e.getTarget()).add(e);
		int start = 9;
		int pos = 0;
		List<Entry<Integer, ArrayList<ReportEntity>>> reports = new ArrayList<>(amauths.entrySet());
		HashMap<Integer, Long> minTimes = new HashMap<>();
		
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
		
		//print
		for(Entry<Integer, ArrayList<ReportEntity>> e : reports){
			inv.setItem(pos+start, createReportInfo(e.getKey(), e.getValue(),minTimes.get(e.getKey())));
			pos++;
		}
		fill(fillItem, start+entites.length, inv.getSlots(), true);
		inv.enableUpdate();
	}
	private Item createReportInfo(int playerId,ArrayList<ReportEntity> reports,long minTime){
		String player = Main.getDatenServer().getClient().getPlayerAndLoad(playerId).getName();
		return loadSkin(ItemBuilder.create(Material.SKULL_ITEM).name("§a"+reports.size()+" Report"+(reports.size()==1?"":"s")+" gegen §e"+player).amouth(reports.size()).listener((c)-> switchToGui(new GuiViewPlayerReport(player, reports))).lore("§aOffen seit: "+PlayerJoinListener.getDurationBreakdown(System.currentTimeMillis()-minTime)).build(), player);
	}
	
	@Override
	public void run() {
		while (isActive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
//			System.out.println("Reprint");
			printReportItems();
		}
//		System.out.println("Breake update!");
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
