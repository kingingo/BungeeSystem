package dev.wolveringer.guild.gui.section;

import java.util.ArrayList;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.api.gui.AnvilGui;
import dev.wolveringer.api.gui.AnvilGuiListener;
import dev.wolveringer.booster.GuiIntegerSelect;
import eu.epicpvp.bungee.system.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.gamestats.Statistic;
import dev.wolveringer.gilde.GildSection;
import dev.wolveringer.gilde.GildePermissions;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.gui.GuiStatusPrint;
import dev.wolveringer.gui.GuiWaiting;
import dev.wolveringer.guild.gui.GuiGildeAdminOverview;
import dev.wolveringer.guild.gui.GuiGildeMemberInvatations;
import dev.wolveringer.guild.gui.GuiGildeMemberManager;
import dev.wolveringer.guild.gui.GuiGildeMoneyOverview;
import dev.wolveringer.guild.gui.GuiGildePermissionGroupOverview;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.thread.ThreadFactory;
import net.md_5.bungee.api.ChatColor;

public class GuiGildeSection extends Gui {
	private GildSection section;

	public GuiGildeSection(GildSection section) {
		super(6, "§a" + section.getType().getDisplayName() + " §7>> §eOverview");
		this.section = section;
	}

	@Override
	public void build() {
		buildDefault();
	}

	protected void buildDefault(){
		inv.setItem(4, buildStats());
		final LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
		if(section.getHandle().getOwnerId() == lplayer.getPlayerId())
			inv.setItem(8, ItemBuilder.create(Material.STAINED_GLASS_PANE).durbility(14).name("§7» §cBereich Deaktivieren").listener((Click c) -> {
				GuiWaiting waiting = new GuiWaiting("§aDeaktivieren gild section", "§aPlease wait");
				waiting.setPlayer(getPlayer()).openGui();
				ThreadFactory.getFactory().createThread(()->{
					if(lplayer.getPlayerId() != section.getHandle().getOwnerId()){
						waiting.waitForMinwait(1500);
						new GuiStatusPrint(5,ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cYou must be the gilden owner!").build()) {
							@Override
							public void onContinue() {
								switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
							}
						}.setPlayer(getPlayer()).openGui();
						return;
					}
					if(section.isActive()){
						section.setActive(false);
					}
					waiting.waitForMinwait(1500);
					new GuiStatusPrint(5, ItemBuilder.create().material(Material.EMERALD).name("§aGild-Section deaktiviert.").build()) {
						@Override
						public void onContinue() {
							new GuiGildeAdminOverview(getPlayer(), section.getHandle()).setPlayer(getPlayer()).openGui();
						}
					}.setPlayer(getPlayer()).openGui();
				}).start();
			}).build());
		//TODO sort
		inv.setItem(19, ItemBuilder.create(Material.GOLD_BLOCK).name("§7» §6Clan Bank").lore("§aMoney: §b"+section.getMoney().getCurrentMoney()).lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildeMoneyOverview(section.getMoney()))).build());
		inv.setItem(21, ItemBuilder.create(Material.SKULL_ITEM).name("§7» §6Clan Mitglieder").lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildeMemberManager(section.getPermission()))).build());
		inv.setItem(23, ItemBuilder.create(Material.SKULL_ITEM).name("§7» §6Clan Gruppen").lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildePermissionGroupOverview(section.getPermission()))).build());

		inv.setItem(25, ItemBuilder.create(Material.GOLD_NUGGET).name("§7» §6Lade die Clan Bank auf").lore("§aKlicke hier um Geld aufzuladen").listener((c)->{
			if(!section.getPermission().hasPermission(lplayer, GildePermissions.BANK_DEPOSIT)){
				new GuiStatusPrint(5,ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cYou dont have permission for deposit.").build()) {
					@Override
					public void onContinue() {
						switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			final int money = lplayer.getCoinsSync();
			new GuiIntegerSelect(getPlayer(),"§aWähle einen Betrag", Math.min(100, money/2)) {

				@Override
				public void numberEntered(int number) {
					AnvilGui gui = new AnvilGui(getPlayer());
					gui.addListener(new AnvilGuiListener() {
						@Override
						public void onMessageChange(AnvilGui guy, String newMessage) {
					    	//Update output item ;)
							Item item = new Item(Material.ENCHANTED_BOOK);
					    	item.getItemMeta().setDisplayName("§aDeposit reason: §e" + (newMessage.length() == 0 ? "§cNo reason" : newMessage));
					    	guy.setOutputItem(item);
						}

						@Override
						public void onConfirmInput(AnvilGui guy, String message) {
							GuiWaiting waiting = new GuiWaiting("§aÜberweise Geld", "§aBitte warte");
							waiting.setPlayer(getPlayer()).openGui();
							ThreadFactory.getFactory().createThread(()->{
								section.getMoney().addMoney(number);
								section.getMoney().log(lplayer.getPlayerId(), number, ChatColor.stripColor(message.length() == 0 ? "No reason" : message));
								lplayer.changeCoins(PacketInStatsEdit.Action.REMOVE, number);
								waiting.waitForMinwait(1500);
								new GuiStatusPrint(5,ItemBuilder.create().material(Material.EMERALD).name("§aMoney überwiesen.").build()) {
									@Override
									public void onContinue() {
										switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
									}
								}.setPlayer(getPlayer()).openGui();
							}).start();
						}

						@Override
						public void onClose(AnvilGui guy) {
							switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
							getPlayer().sendMessage("§cAction canceled.");
						}
					});
					gui.setColorPrefix("§a");
					gui.open();
					gui.setBackgroundMessage("Enter you reason");
				}

				@Override
				public boolean isNumberAllowed(int number) {
					return number > 0 && money >= number;
				}
			}.setMode(2).open();
		}).build());
		inv.setItem(37, ItemBuilder.create(Material.DIAMOND).name("§7» §6Entnehme was von der Clan Bank").listener(()->{
			if(!section.getPermission().hasPermission(lplayer, GildePermissions.BANK_WITHDRAW)){
				new GuiStatusPrint(5,ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cDu hast keine Berechtigung etwas abzuheben!").build()) {
					@Override
					public void onContinue() {
						switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			final int money = section.getMoney().getCurrentMoney();
			if(money < 10){
				new GuiStatusPrint(5,ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cDas Guthaben auf der Bank ist unter 10! Du kannst nichts abheben!").build()) {
					@Override
					public void onContinue() {
						switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			new GuiIntegerSelect(getPlayer(),"§aWähle einen Betrag", Math.min(100, money/2)) {

				@Override
				public void numberEntered(int number) {
					AnvilGui gui = new AnvilGui(getPlayer());
					gui.addListener(new AnvilGuiListener() {
						@Override
						public void onMessageChange(AnvilGui guy, String newMessage) {
					    	//Update output item ;)
							Item item = new Item(Material.ENCHANTED_BOOK);
					    	item.getItemMeta().setDisplayName("§aWithdraw reason: §e" + (newMessage.length() == 0 ? "§cNo reason" : newMessage));
					    	guy.setOutputItem(item);
						}

						@Override
						public void onConfirmInput(AnvilGui guy, String message) {
							GuiWaiting waiting = new GuiWaiting("§aÜberweise geld", "§aBitte warte");
							waiting.setPlayer(getPlayer()).openGui();
							ThreadFactory.getFactory().createThread(()->{
								if(section.getMoney().getCurrentMoney() < number){
									waiting.waitForMinwait(1500);
									new GuiStatusPrint(5,ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cAuf der Gilden-Bank ist nicht genug Geld!").build()) {
										@Override
										public void onContinue() {
											switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
										}
									}.setPlayer(getPlayer()).openGui();
									return;
								}
								section.getMoney().removeMoney(number);
								section.getMoney().log(lplayer.getPlayerId(), -number, ChatColor.stripColor(message.length() == 0 ? "No reason" : message));
								lplayer.changeCoins(PacketInStatsEdit.Action.ADD, number);
								waiting.waitForMinwait(1500);
								new GuiStatusPrint(5,ItemBuilder.create().material(Material.EMERALD).name("§aGeld überwiesen.").build()) {
									@Override
									public void onContinue() {
										switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
									}
								}.setPlayer(getPlayer()).openGui();
							}).start();
						}

						@Override
						public void onClose(AnvilGui guy) {
							switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
							getPlayer().sendMessage("§cAction canceled.");
						}
					});
					gui.setColorPrefix("§a");
					gui.open();
					gui.setBackgroundMessage("Enter you reason");
				}

				@Override
				public boolean isNumberAllowed(int number) {
					return number > 0 && money >= number;
				}
			}.setMode(2).open();
		}).build());

		inv.setItem(39, ItemBuilder.create(Material.SKULL_ITEM).durbility(3).name("§aMitglieds anfragen ("+section.getRequestedPlayer().size()+")").listener(()->{
			switchToGui(new GuiGildeMemberInvatations(section));
		}).build());

		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cSchließen").listener((Click c) -> c.getPlayer().closeInventory()).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
	}

	private Item buildStats() {
		ItemBuilder builder = new ItemBuilder(339);
		builder.name("§a" + section.getType().getDisplayName() + " Stats");
		builder.glow();
		builder.lore("§cLoading stats");
		Item item = builder.build();

		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				ArrayList<String> lore = new ArrayList<>();
				for (Statistic s : section.getStatsPlayer().getStats(section.getType().getStatsType()).getSync()) {
					lore.add("§a" + s.getStatsKey().getContraction() + " §7» §e" + toString(s.getValue(), "§cNull Value"));
				}
				item.getItemMeta().setLore(lore);
				item.getItemMeta().setGlow(false);
			}
			private String toString(Object obj, String nullStr) {
				return ((obj == null) ? nullStr : obj.toString());
			}
		}).start();
		return item;
	}
}
