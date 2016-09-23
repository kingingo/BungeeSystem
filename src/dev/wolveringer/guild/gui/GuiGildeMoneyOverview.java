package dev.wolveringer.guild.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gilde.GildSectionMoney;
import dev.wolveringer.gilde.MoneyLogRecord;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildeMoneyOverview extends Gui{
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss");
	
	private static enum SortType {
		NAME,
		TIME_DECREASING,
		TIME_INCREASING,
		VALUE_DECREASING,
		VALUE_INCREASING;
	}
	
	private final GildSectionMoney money;
	
	private SortType type;
	
	private int side = 0;
	private List<MoneyLogRecord> records;
	
	public GuiGildeMoneyOverview(GildSectionMoney money) {
		super(6,"§a"+money.getHandle().getType().getDisplayName()+" §6» §aMoney");
		this.money = money;
		this.records = new ArrayList<>(money.getHistory());
	}

	@Override
	public void build() {
		updateSortType(SortType.TIME_DECREASING);
		updateInventory();
	}
	
	private void updateInventory(){
		drawBorder();
		drawSection();
	}
	
	private void drawBorder(){
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(),36,45);
		inv.setItem(46, ItemBuilder.create(Material.ARROW).name("§7Previous side "+(side != 0 ? "("+(side-1)+")":"")).listener(()-> {
			if(side > 0){
				side--;
				updateInventory();
			}
		}).glow(side == 0).build());
		
		inv.setItem(48, ItemBuilder.create(175).name("§aShort elements by name").glow(type == SortType.NAME).listener((c)->{
			if(!c.getItem().getItemMeta().hasGlow()){
				updateSortType(SortType.NAME);
				updateInventory();
				c.getPlayer().sendMessage("§cTODO");
			}
		}).build());
		
		inv.setItem(49, ItemBuilder.create(175).name("§aShort elements by time (decrasing)").glow(type == SortType.TIME_DECREASING).listener((c)->{
			if(!c.getItem().getItemMeta().hasGlow()){
				updateSortType(SortType.TIME_DECREASING);
				updateInventory();
			}
		}).build());
		
		inv.setItem(48, ItemBuilder.create(175).name("§aShort elements by time (increasing)").glow(type == SortType.TIME_INCREASING).listener((c)->{
			if(!c.getItem().getItemMeta().hasGlow()){
				updateSortType(SortType.TIME_INCREASING);
				updateInventory();
			}
		}).build());
		
		inv.setItem(48, ItemBuilder.create(175).name("§aShort elements by money (decrasing)").glow(type == SortType.VALUE_DECREASING).listener((c)->{
			if(!c.getItem().getItemMeta().hasGlow()){
				updateSortType(SortType.VALUE_DECREASING);
				updateInventory();
				c.getPlayer().sendMessage("§cTODO");
			}
		}).build());
		
		inv.setItem(48, ItemBuilder.create(175).name("§aShort elements by money (increasing)").glow(type == SortType.VALUE_INCREASING).listener((c)->{
			if(!c.getItem().getItemMeta().hasGlow()){
				updateSortType(SortType.VALUE_INCREASING);
				updateInventory();
				c.getPlayer().sendMessage("§cTODO");
			}
		}).build());
		
		inv.setItem(54, ItemBuilder.create(Material.ARROW).name("§7Next side "+(side*4*9 > records.size() ? "("+(side+1)+")":"")).listener(()-> {
			if(side > 0){
				side++;
				updateInventory();
			}
		}).glow(side*4*9 > records.size()).build());
	}
	
	private void drawSection(){
		for(int i = 0;i<5*9;i++)
			if(side*5*9 < records.size())
				inv.setItem(i, buildItem(records.get(side*5*9+i)));
	}
	
	private Item buildItem(MoneyLogRecord record){
		ItemBuilder builder = ItemBuilder.create(Material.SKULL_ITEM).durbility(3);
		LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(record.getPlayerId());
		builder.name("§aPlayer: "+lp.getName());
		builder.lore("§aAmount: "+record.getAmount());
		builder.lore("§aDate: "+FORMAT.format(new Date(record.getDate())));
		builder.lore("§aMessage: "+record.getMessage());
		builder.lore("§cTODO format to fancy!"); //TODO
		return loadSkin(builder.build(), lp.getName());
	}
	
	private void updateSortType(SortType _new){
		if(type == _new)
			return;
		type = _new;
		switch (type) {
		case TIME_DECREASING:
			Collections.sort(records,new Comparator<MoneyLogRecord>() {
				@Override
				public int compare(MoneyLogRecord o1, MoneyLogRecord o2) {
					return Long.compare(o1.getDate(), o2.getDate());
				}
			});
			break;
		case TIME_INCREASING:
			Collections.sort(records,new Comparator<MoneyLogRecord>() {
				@Override
				public int compare(MoneyLogRecord o1, MoneyLogRecord o2) {
					return Long.compare(o2.getDate(), o1.getDate());
				}
			});
			break;
			//TODO add all other options
		default:
			break;
		}
	}
}
