package dev.wolveringer.booster;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.listener.PlayerJoinListener;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.Action;
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
		inv.setItem(4, buildOwnInformations());
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
		builder.name("§aDeine Booster-Zeit: §e"+PlayerJoinListener.getDurationBreakdown(ownBooster.getTime(),"§c0 Seconds"));
		return builder.build();
	}
	
	private Item buildBooster(BoosterType type) {
		ItemBuilder builder = ItemBuilder.create();
		NetworkBooster booster = Main.getBoosterManager().getBooster(type);
		if (booster.isActive()) {
			builder.id(399);
			builder.name("§a" + booster.getType().getDisplayName() + "-Booster von §e" + Main.getDatenServer().getClient().getPlayer(booster.getPlayer()).getName());
			builder.lore("§bBooster hält noch " + PlayerJoinListener.getDurationBreakdown((booster.getStart() + booster.getTime()) - System.currentTimeMillis()));
			builder.lore("§a");
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(booster.getPlayer());
			if (!player.getName().equalsIgnoreCase(getPlayer().getName())) {
				builder.lore("§eKlicke um dich bei dem Spieler zu bedanken.");
				builder.listener((Click c) -> {
					GuiIntegerSelect select = new GuiIntegerSelect(c.getPlayer(), "§aWähle deine Spendensumme", 10, "§b%d §aGems") {
						int max = 0;
						
						{
							max = player.getGemsSync();
						}
						
						@Override
						public void numberEntered(int number) {
							player.changeGems(Action.REMOVE, number);
							LoadedPlayer target = Main.getDatenServer().getClient().getPlayer(booster.getPlayer());
							target.changeCoins(Action.ADD, number);
							c.getPlayer().sendMessage("§aDu hast dem Spieler "+target.getName()+" §e"+number+" §aGems gespendet.");
							Main.getDatenServer().getClient().sendMessage(target.getPlayerId(), "§aEin User hat dir §e"+number+" §aGems gespendet!");
							c.getPlayer().closeInventory();
						}

						@Override
						public boolean isNumberAllowed(int number) {
							return number >= 10 && number <= max;
						}
					};
					select.open();
				});
			}
		} else {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
			NetworkBooster ownBooster = Main.getDatenServer().getClient().getNetworkBoosterInformation(type, player.getPlayerId()).getSync();
			int max = ownBooster.getTime();
			builder.id(289);
			builder.name("§7Kein " + booster.getType() + "-Booster aktiv.");
			if (max > 30 * 60 * 1000) {
				builder.lore("§a");
				builder.lore("§eKicke um einen Booster zu aktivieren.");
				builder.listener((Click c) -> {
					GuiIntegerSelect select = new GuiIntegerSelect(c.getPlayer(), "§aWähle die Laufzeit.", 30, "§e%d min") {
						@Override
						public void numberEntered(int number) {
							Player pplayer = c.getPlayer();
							if (!Main.getBoosterManager().getBooster(type).isActive()) {
								pplayer.sendMessage("§aDu hast den Netzwerkbooster für " + booster.getType().getDisplayName() + " " + PlayerJoinListener.getDurationBreakdown(number * 60 * 1000) + " aktiviert.");
								player.activeNetworkBooster(type, number * 60 * 1000);
								Main.getBoosterManager().reloadBooster(type);
								pplayer.closeInventory();
							} else {
								pplayer.sendMessage("§cLeider hat schon wer anders einen Netzwerk-Booster aktiviert.\n§cVersuche es später erneut.");
								pplayer.closeInventory();
							}
						}

						@Override
						public boolean isNumberAllowed(int number) {
							return number >= 30 && number * 60 * 1000 < max;
						}
					};
					select.open();
				});
			} else
				builder.lore("§a").lore("§cDu brauchst mindestens 30 Booster Minuten um einen Booster zu Aktivieren.");
		}
		return builder.build();
	}
}
