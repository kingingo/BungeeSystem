package eu.epicpvp.bungee.system.guild.gui.section;

import java.util.ArrayList;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.gui.GuiStatusPrint;
import eu.epicpvp.bungee.system.gui.GuiWaiting;
import eu.epicpvp.bungee.system.guild.gui.GuiGildeAdminOverview;
import eu.epicpvp.bungee.system.guild.gui.GuiGildeMemberInvitations;
import eu.epicpvp.bungee.system.guild.gui.GuiGildeMemberManager;
import eu.epicpvp.bungee.system.guild.gui.GuiGildePermissionGroupOverview;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.item.ItemBuilder.ItemClickListener;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.gilde.GildSection;
import eu.epicpvp.datenserver.definitions.gamestats.Statistic;
import eu.epicpvp.thread.ThreadFactory;

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

	protected void buildDefault() {
		inv.setItem(4, buildStats());
		final LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
		if (section.getHandle().getOwnerId() == lplayer.getPlayerId())
			inv.setItem(8, ItemBuilder.create(Material.STAINED_GLASS_PANE).durability(14).name("§7» §cBereich deaktivieren").listener((Click c) -> {
				GuiWaiting waiting = new GuiWaiting("§aDeaktiviere Clanbereich", "§aPlease wait");
				waiting.setPlayer(getPlayer()).openGui();
				ThreadFactory.getFactory().createThread(() -> {
					if (lplayer.getPlayerId() != section.getHandle().getOwnerId()) {
						waiting.waitForMinwait(1500);
						new GuiStatusPrint(5, "§cNur der Clanbesitzer kann das machen!", ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cNur der Clanbesitzer kann das machen!").build()) {
							@Override
							public void onContinue() {
								switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
							}
						}.setPlayer(getPlayer()).openGui();
						return;
					}
					if (section.isActive()) {
						section.setActive(false);
					}
					waiting.waitForMinwait(1500);
					new GuiStatusPrint(5, "§aClanbereich deaktiviert.", ItemBuilder.create().material(Material.EMERALD).name("§aClanbereich deaktiviert.").build()) {
						@Override
						public void onContinue() {
							new GuiGildeAdminOverview(getPlayer(), section.getHandle()).setPlayer(getPlayer()).openGui();
						}
					}.setPlayer(getPlayer()).openGui();
				}).start();
			}).build());
		//TODO sort
//		inv.setItem(19, ItemBuilder.create(Material.GOLD_BLOCK).name("§7» §6Clan Bank").lore("§aMoney: §b" + section.getMoney().getCurrentMoney()).lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildeMoneyOverview(section.getMoney()))).build());
		inv.setItem(21, ItemBuilder.create(Material.SKULL_ITEM).name("§7» §6Clan Mitglieder").lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildeMemberManager(section.getPermission()))).build());
		inv.setItem(23, ItemBuilder.create(Material.SKULL_ITEM).name("§7» §6Clan Gruppen").lore("§aKlicke hier für weitere Infos").listener((Click c) -> switchToGui(new GuiGildePermissionGroupOverview(section.getPermission()))).build());

//		inv.setItem(25, ItemBuilder.create(Material.GOLD_NUGGET).name("§7» §6Lade die Clan Bank auf").lore("§aKlicke hier um Geld aufzuladen").listener((c) -> {
//			if (!section.getPermission().hasPermission(lplayer, GildePermissions.BANK_DEPOSIT)) {
//				new GuiStatusPrint(5, "§cDu hast keine Berechtigung, Geld abzuheben.", ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cDu hast keine Berechtigung, Geld abzuheben.").build()) {
//					@Override
//					public void onContinue() {
//						switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
//					}
//				}.setPlayer(getPlayer()).openGui();
//				return;
//			}
//			final int money = lplayer.getCoinsSync();
//			new GuiIntegerSelect(getPlayer(), "§aWähle einen Betrag", Math.min(100, money / 2)) {
//
//				@Override
//				public void numberEntered(int number) {
//					AnvilGui gui = new AnvilGui(getPlayer());
//					gui.addListener(new AnvilGuiListener() {
//						@Override
//						public void onMessageChange(AnvilGui guy, String newMessage) {
//							//Update output item ;)
//							Item item = new Item(Material.ENCHANTED_BOOK);
//							item.getItemMeta().setDisplayName("§aAbhebegrund: §e" + (newMessage.isEmpty() ? "§cNo reason" : newMessage));
//							guy.setOutputItem(item);
//						}
//
//						@Override
//						public void onConfirmInput(AnvilGui guy, String message) {
//							GuiWaiting waiting = new GuiWaiting("§aÜberweise Geld", "§aBitte warte");
//							waiting.setPlayer(getPlayer()).openGui();
//							ThreadFactory.getFactory().createThread(() -> {
//								section.getMoney().addMoney(number);
//								section.getMoney().log(lplayer.getPlayerId(), number, ChatColor.stripColor(message.isEmpty() ? "No reason" : message));
//								lplayer.changeCoins(PacketInStatsEdit.Action.REMOVE, number);
//								waiting.waitForMinwait(1500);
//								new GuiStatusPrint(5, "§aGeld überwiesen.", ItemBuilder.create().material(Material.EMERALD).name("§aGeld überwiesen.").build()) {
//									@Override
//									public void onContinue() {
//										switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
//									}
//								}.setPlayer(getPlayer()).openGui();
//							}).start();
//						}
//
//						@Override
//						public void onClose(AnvilGui guy) {
//							switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
//							getPlayer().sendMessage("§cAktion abgebrochen.");
//						}
//					});
//					gui.setColorPrefix("§a");
//					gui.open();
//					gui.setBackgroundMessage("Gib den Grund ein");
//				}
//
//				@Override
//				public boolean isNumberAllowed(int number) {
//					return number > 0 && money >= number;
//				}
//			}.setMode(2).open();
//		}).build());
//		inv.setItem(37, ItemBuilder.create(Material.DIAMOND).name("§7» §6Entnehme was von der Clan Bank").listener(() -> {
//			if (!section.getPermission().hasPermission(lplayer, GildePermissions.BANK_WITHDRAW)) {
//				new GuiStatusPrint(5, "§cDu hast keine Berechtigung etwas abzuheben!", ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cDu hast keine Berechtigung etwas abzuheben!").build()) {
//					@Override
//					public void onContinue() {
//						switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
//					}
//				}.setPlayer(getPlayer()).openGui();
//				return;
//			}
//			final int money = section.getMoney().getCurrentMoney();
//			if (money < 10) {
//				new GuiStatusPrint(5, "§cDas Guthaben auf der Bank ist unter 10! Du kannst nichts abheben!", ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cDas Guthaben auf der Bank ist unter 10! Du kannst nichts abheben!").build()) {
//					@Override
//					public void onContinue() {
//						switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
//					}
//				}.setPlayer(getPlayer()).openGui();
//				return;
//			}
//			new GuiIntegerSelect(getPlayer(), "§aWähle einen Betrag", Math.min(100, money / 2)) {
//
//				@Override
//				public void numberEntered(int number) {
//					AnvilGui gui = new AnvilGui(getPlayer());
//					gui.addListener(new AnvilGuiListener() {
//						@Override
//						public void onMessageChange(AnvilGui guy, String newMessage) {
//							//Update output item ;)
//							Item item = new Item(Material.ENCHANTED_BOOK);
//							item.getItemMeta().setDisplayName("§aAbhebegrund: §e" + (newMessage.isEmpty() ? "§cNo reason" : newMessage));
//							guy.setOutputItem(item);
//						}
//
//						@Override
//						public void onConfirmInput(AnvilGui guy, String message) {
//							GuiWaiting waiting = new GuiWaiting("§aÜberweise Geld", "§aBitte warte");
//							waiting.setPlayer(getPlayer()).openGui();
//							ThreadFactory.getFactory().createThread(() -> {
//								if (section.getMoney().getCurrentMoney() < number) {
//									waiting.waitForMinwait(1500);
//									new GuiStatusPrint(5, "§cAuf der Clan-Bank ist nicht genug Geld!", ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cAuf der Clan-Bank ist nicht genug Geld!").build()) {
//										@Override
//										public void onContinue() {
//											switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
//										}
//									}.setPlayer(getPlayer()).openGui();
//									return;
//								}
//								section.getMoney().removeMoney(number);
//								section.getMoney().log(lplayer.getPlayerId(), -number, ChatColor.stripColor(message.isEmpty() ? "No reason" : message));
//								lplayer.changeCoins(PacketInStatsEdit.Action.ADD, number);
//								waiting.waitForMinwait(1500);
//								new GuiStatusPrint(5, "§aGeld überwiesen.", ItemBuilder.create().material(Material.EMERALD).name("§aGeld überwiesen.").build()) {
//									@Override
//									public void onContinue() {
//										switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
//									}
//								}.setPlayer(getPlayer()).openGui();
//							}).start();
//						}
//
//						@Override
//						public void onClose(AnvilGui guy) {
//							switchToGui(new GuiGildeSection(section)); //REBUILD THE GUI :)
//							getPlayer().sendMessage("§cAktion abgebrochen.");
//						}
//					});
//					gui.setColorPrefix("§a");
//					gui.open();
//					gui.setBackgroundMessage("Gebe den Grund ein");
//				}
//
//				@Override
//				public boolean isNumberAllowed(int number) {
//					return number > 0 && money >= number;
//				}
//			}.setMode(2).open();
//		}).build());

		inv.setItem(39, ItemBuilder.create(Material.SKULL_ITEM).durability(3).name("§aMitgliedsanfragen (" + section.getRequestedPlayer().size() + ")").listener(() -> {
			switchToGui(new GuiGildeMemberInvitations(section));
		}).build());

		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cSchließen").listener((Click c) -> c.getPlayer().closeInventory()).build());

		if (section.getHandle().getOwnerId() != lplayer.getPlayerId()){
			inv.setItem(8, ItemBuilder.create(Material.REDSTONE).name("§cClan verlassen").listener( new ItemClickListener(){

				@Override
				public void click(Click c) {
					section.kickPlayer(lplayer);
					getPlayer().closeInventory();
					getPlayer().sendMessage("§cDu hast den Clan verlassen!");
				}
			}).build());
		}
		fill(ItemBuilder.create(160).durability(7).name("§7").build());
	}

	private Item buildStats() {
		ItemBuilder builder = new ItemBuilder(339);
		builder.name("§a" + section.getType().getDisplayName() + " Stats");
		builder.glow();
		builder.lore("§cLade Statistik...");
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
