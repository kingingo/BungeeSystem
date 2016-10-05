package eu.epicpvp.bungee.system.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.listener.PlayerJoinListener;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import eu.epicpvp.datenclient.client.PacketHandleErrorException;
import eu.epicpvp.datenserver.definitions.report.ReportEntity;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class GuiViewOwnReports extends Gui implements Runnable{
	private ScheduledTask pid;
	private Item fillItem = ItemBuilder.create(160).durbility(7).name("§7").build();
	private ReportEntity[] entites;

	public GuiViewOwnReports() {
		super(6, "§aYour reports");
	}

	Item openReports = null;

	@Override
	public void build() {
		inv.setItem(0, new ItemStack(ItemBuilder.create(Material.BARRIER).name("§cZurück").build()){
			@Override
			public void click(Click c) {
				switchToGui(new GuiPlayerMenue());
			}
		});
		inv.setItem(4, openReports = ItemBuilder.create(Material.EMERALD).name("§6Open reports: Loading....").build());
		//11 13 15
		//29 31 33
		//47 49 51

		fill(fillItem, 0, 6*9);
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				try{
					afterload(Main.getDatenServer().getClient().getReportEntity(RequestType.PLAYER_OPEN_REPORTS, Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName()).getPlayerId()).getSync());
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
		openReports.setType(Material.DIAMOND);
		openReports.getItemMeta().setDisplayName("§6Open reports: "+entities.length);
		inv.setItem(4, ItemBuilder.create(Material.DIAMOND).name("§6Open reports: "+entities.length).build());
		this.entites = entities;
		printReportItems();
	}
	private void printReportItems(){
		if(entites == null)
			return;
		inv.disableUpdate();
		int start = 9;
		for(int i = 0;i<entites.length;i++){
			inv.setItem(i+start, createReportInfo(entites[i]));
		}
		fill(fillItem, start+entites.length, inv.getSlots(), true);
		inv.enableUpdate();
	}
	private Item createReportInfo(ReportEntity e){
		return ItemBuilder.create(Material.PAPER).name("§e"+Main.getDatenServer().getClient().getPlayerAndLoad(e.getTarget()).getName()).lore("§aGrund: §e"+e.getReson()).lore("§aOffen seit: "+PlayerJoinListener.getDurationBreakdown(System.currentTimeMillis()-e.getTime())).build();
	}

	@Override
	public void run() {
		while (isActive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
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
