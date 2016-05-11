package dev.wolveringer.booster;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gui.GuiUpdating;
import dev.wolveringer.item.ItemBuilder;

public class GuiBoosterMenue extends GuiUpdating{
	public GuiBoosterMenue() {
		super(1, "§eNetwork Booster");
	}
	
	@Override
	public void build() {
		inv.setItem(2, buildBooster(BoosterType.SKY));
		inv.setItem(6, buildBooster(BoosterType.ARCADE));
		inv.setItem(3, buildOwnInformations());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
	}
	@Override
	public void updateInventory() {
		inv.setItem(2, buildBooster(BoosterType.SKY));
		inv.setItem(6, buildBooster(BoosterType.ARCADE));
	}
	
	private Item buildOwnInformations(){
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
		NetworkBooster ownBooster = Main.getDatenServer().getClient().getNetworkBoosterInformation(BoosterType.NONE, player.getPlayerId()).getSync();
		ItemBuilder builder = ItemBuilder.create(175);
		builder.name("§aDeine Booster-Zeit: §e"+PlayerJoinListener.getDurationBreakdown(ownBooster.getTime()));
		return builder.build();
	}
	
	private Item buildBooster(BoosterType type){
		ItemBuilder builder = ItemBuilder.create();
		NetworkBooster booster = Main.getBoosterManager().getBooster(type);
		if(booster.isActive()){
			builder.id(399);
			builder.name("§a"+booster.getType().getDisplayName()+"-Booster von §e"+Main.getDatenServer().getClient().getPlayer(booster.getPlayer()).getName());
			builder.lore("§bBooster hält noch "+PlayerJoinListener.getDurationBreakdown((booster.getStart()+booster.getTime())-System.currentTimeMillis()));
			builder.lore("§a");
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(booster.getPlayer());
			if(!player.getName().equalsIgnoreCase(getPlayer().getName())){
				builder.lore("§eKlicke um dich bei dem Spieler zu bedanken.");
				builder.listener((c)->{
					c.getPlayer().closeInventory();
					c.getPlayer().sendMessage("§aOperation not supported yet.");
				});
			}
		}
		else
		{
			builder.id(289);
			builder.name("§7Kein "+booster.getType()+"-Booster aktiv.");
			builder.lore("§a");
			builder.lore("§eKicke um einen Booster zu aktivieren.");
			builder.listener((Click c)->{
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(c.getPlayer().getName());
				Player pplayer = c.getPlayer();
				NetworkBooster ownBooster = Main.getDatenServer().getClient().getNetworkBoosterInformation(type, player.getPlayerId()).getSync();
				int max = ownBooster.getTime();
				GuiIntegerSelect select = new GuiIntegerSelect(c.getPlayer(),"§aWähle die Laufzeit.",30,"§e%d min") {
					@Override
					public void numberEntered(int number) {
						if(!Main.getBoosterManager().getBooster(type).isActive()){
							pplayer.sendMessage("§aDu hast den Netzwerkbooster für "+booster.getType().getDisplayName()+" "+PlayerJoinListener.getDurationBreakdown(number*60*1000)+" activiert.");
							player.activeNetworkBooster(type, number*60*1000);
							pplayer.closeInventory();
						}
						else
						{
							pplayer.sendMessage("§cLeider hat schon wer anders einen Netzwerk-Booster aktiviert.\n§cVersuche es später erneut.");
							pplayer.closeInventory();
						}
					}
					
					@Override
					public boolean isNumberAllowed(int number) {
						return number>30 && number*60*1000<max;
					}
				};
				select.open();
			});
		}
		return builder.build();
	}
}
