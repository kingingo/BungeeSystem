package eu.epicpvp.bungee.system.guild.gui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.gui.GuiStatusPrint;
import eu.epicpvp.bungee.system.gui.GuiWaiting;
import eu.epicpvp.bungee.system.guild.gui.search.GildeSearchMenue;
import eu.epicpvp.bungee.system.guild.gui.section.SectionRegestry;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.report.search.PlayerTextEnterMenue;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildActionResponse;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildActionResponse.Action;
import eu.epicpvp.datenclient.client.Callback;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.gilde.Gilde;
import eu.epicpvp.datenserver.definitions.gilde.GildePermissions;
import eu.epicpvp.datenserver.definitions.gilde.GildeType;
import eu.epicpvp.thread.ThreadFactory;

public class GuiPlayerGildeOverview extends Gui {

	private static Map<GildeType, Integer> itemMapping = new EnumMap<>(GildeType.class);

	static {
//		itemMapping.put(GildeType.ARCADE, 345);
//		itemMapping.put(GildeType.PVP, 279);
//		itemMapping.put(GildeType.SKY, 3);
//		itemMapping.put(GildeType.VERSUS, 261);
		itemMapping.put(GildeType.WARZ, 367);
	}

	private Player player;
	private LoadedPlayer lplayer;
	private Map<GildeType, Gilde> gilden = new EnumMap<>(GildeType.class);
	private int ownerState = -1;
	private UUID ownGilde;

	public GuiPlayerGildeOverview(Player player) {
		super(5, "§aClanmanagement §7- §c§lBETA");
		this.player = player;
		this.lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(player.getName());
		loadData();
	}

	@Override
	public void build() {
		print();
	}

	private synchronized void print() {
		inv.disableUpdate();
		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 0, -1, true);
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cSchließen").listener((Click c) -> c.getPlayer().closeInventory()).build());
		if (ownerState != -1)
			if (ownerState == 1)
				inv.setItem(8, ItemBuilder.create(Material.DIAMOND_PICKAXE).name("§aManage deinen eigenen Clan").listener(new Runnable() {
					@Override
					public void run() {
						if (ownGilde == null)
							System.out.println("own null!");
						Gilde gilde = Main.getGildeManager().getGilde(ownGilde);
						if (gilde == null)
							System.out.println("Null clan");
						new GuiGildeAdminOverview(player, gilde).setPlayer(player).openGui();
					}
				}).build());
			else
				inv.setItem(8, ItemBuilder.create(Material.NETHER_STAR).name("§aErstelle deinen eigenden Clan").listener((Click c) -> createNewGilde(null)).build());
//		inv.setItem(19, buildSection(GildeType.ARCADE));
//		inv.setItem(30, buildSection(GildeType.PVP));
//		inv.setItem(32, buildSection(GildeType.SKY));
//		inv.setItem(25, buildSection(GildeType.VERSUS));
		inv.setItem(13, buildSection(GildeType.WARZ));
		inv.enableUpdate();
	}

	public Item buildSection(GildeType type) {
		ItemBuilder item = ItemBuilder.create(itemMapping.get(type));
		item.name("§7» §6" + type.getDisplayName());
		if (!gilden.containsKey(type)) {
			item.lore("§cLade Claninformationen...");
		} else {
			if (gilden.get(type) == null) {
				item.lore("§aKlicke, um einen Clan im Bereich " + type.getDisplayName() + " beizutreten.");
				item.listener((c) -> {
					GildeSearchMenue search = new GildeSearchMenue(c.getPlayer(), type) {
						@Override
						public void gildeEntered(UUID ugilde) {
							ThreadFactory.getFactory().createThread(() -> {
								GuiWaiting waiting = new GuiWaiting(Main.getTranslationManager().translate("waiting.title", getPlayer()), Main.getTranslationManager().translate("waiting.message", getPlayer()));
								waiting.setWaitTime(150);
								waiting.setPlayer(player);
								waiting.openGui();

								Gilde gilde = Main.getGildeManager().getGilde(ugilde);
								if (gilde == null || !gilde.getSelection(type).isActive()) {
									waiting.waitForMinwait(1500);
									new GuiStatusPrint(6, "§cFehler #1 während der Anfrage aufgetreten.", ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cFehler #1 während der Anfrage aufgetreten.").build()) {
										@Override
										public void onContinue() {
											new GuiPlayerGildeOverview(getPlayer()).setPlayer(getPlayer()).openGui();
										}
									}.setPlayer(player).openGui();
									return;
								}
								if (gilde.getSelection(type).getRequestedPlayer().contains(lplayer.getPlayerId())) {
									waiting.waitForMinwait(1500);
									new GuiStatusPrint(6, "§cDu hast diesem Clan bereits eine Mitgliedschaft angefordert!", ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu hast diesem Clan bereits eine Mitgliedschaft angefordert!").build()) {
										@Override
										public void onContinue() {
											new GuiPlayerGildeOverview(getPlayer()).setPlayer(getPlayer()).openGui();
										}
									}.setPlayer(player).openGui();
									return;
								}
								gilde.getSelection(type).addRequest(lplayer);
								ThreadFactory.getFactory().createThread(() -> {
									for (int players : gilde.getSelection(type).getPlayers()) {
										if (gilde.getSelection(type).getPermission().hasPermission(players, GildePermissions.MEMBER_ACCEPT))
											Main.getDatenServer().getClient().sendMessage(players, "§aDer Spieler §e" + player.getName() + "§a hat die Mitgliedschaft in dem Bereich §e" + type.getDisplayName() + " §afür den Clan §e" + gilde.getName() + "§a angefragt.");
									}
								}).start();

								waiting.waitForMinwait(1500);
								new GuiStatusPrint(6, "§cAn error happed while requesting.", ItemBuilder.create(Material.EMERALD).name("§aMitglidschaft beantragt.").build()) {
									@Override
									public void onContinue() {
										new GuiPlayerGildeOverview(getPlayer()).setPlayer(getPlayer()).openGui();
									}
								}.setPlayer(player).openGui();
							}).start();
						}
					};
					search.open();
				});
				item.glow();
			} else {
				Gilde gilde = gilden.get(type);
				item.lore("§aKlicke um in den Clanbereich");
				item.lore("§ades Clans " + gilde.getName() + " zu kommen.");
				item.listener((c) -> {
					switchToGui(SectionRegestry.getInstance().createGildeSection(gilde.getSelection(type)));
				});
			}
		}
		return item.build();
	}

	private void createNewGilde(String current) {
		PlayerTextEnterMenue name = new PlayerTextEnterMenue(player) {
			@Override
			public void textEntered(String name) {
				GuiWaiting waiting = new GuiWaiting(Main.getTranslationManager().translate("waiting.title", getPlayer()), Main.getTranslationManager().translate("waiting.message", getPlayer()));
				waiting.setWaitTime(150);
				waiting.setPlayer(player);
				waiting.openGui();

				Main.getDatenServer().getClient().getAvailableGilde(GildeType.ALL).getAsync(new Callback<HashMap<UUID, String>>() {
					@Override
					public void call(HashMap<UUID, String> obj, Throwable exception) {
						waiting.waitForMinwait((int) (1000 + System.currentTimeMillis() % 1200));
						ArrayList<String> names = new ArrayList<>();
						for (String name : obj.values())
							names.add(name.toLowerCase());
						if (names.contains(name)) {
							new GuiStatusPrint(5, "§cDieser Name wird bereits genutzt.", ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cDieser Name wird bereits genutzt.").lore("§6Klicke, um einen anderen Namen zu wählen").lore("§6Schließe das Inventar zum Abbrechen").build()) {
								@Override
								public void onContinue() {
									createNewGilde(name);
								}
							}.setPlayer(player).openGui();
						} else {
							Main.getDatenServer().getClient().createGilde(lplayer, name).getAsync(new Callback<PacketGildActionResponse>() {
								@Override
								public void call(PacketGildActionResponse obj, Throwable exception) {
									if (obj == null || obj.getAction() == Action.ERROR) {
										if (exception != null) {
											exception.printStackTrace();
										}
										new GuiStatusPrint(5, "§cEs ist ein Fehler beim Erstellen des Clans aufgetreten.", ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cEs ist ein Fehler beim Erstellen des Clans aufgetreten. (" + (obj == null ? 1 : obj.getAction() == Action.ERROR ? 2 : 3) + ")").lore(obj != null ? "§6Message: §7" + obj.getMessage() : "§c").build()) {
											@Override
											public void onContinue() {
												player.closeInventory();
											}
										}.setPlayer(player).openGui();
									} else {
										new GuiStatusPrint(5, "§aDein Clan wurde erfolgreich erstellt.", ItemBuilder.create().material(Material.EMERALD).name("§aDein Clan wurde erfolgreich erstellt.").build()) {
											@Override
											public void onContinue() {
												new GuiGildeAdminOverview(player, Main.getGildeManager().getGilde(obj.getUuid())).setPlayer(player).openGui();
											}
										}.setPlayer(player).openGui();
									}
								}
							});
							;
						}
					}
				});
			}

			@Override
			public void canceled() {}
		};
		name.setBackGround("§aGeb den Namen ein");
		name.open();
		if (current != null)
			name.getGui().setCurruntInput(current);
	}

	private void loadData() {
		loadData(GildeType.ARCADE);
		loadData(GildeType.PVP);
		loadData(GildeType.SKY);
		loadData(GildeType.VERSUS);
		loadData(GildeType.WARZ);
		Main.getDatenServer().getClient().getOwnGilde(lplayer).getAsync(new Callback<UUID>() {
			@Override
			public void call(UUID obj, Throwable exception) {
				if (exception != null)
					exception.printStackTrace();
				if (obj == null)
					ownerState = 0;
				else {
					ownerState = 1;
					ownGilde = obj;
				}
				if (isActive())
					print();
			}
		});
	}

	private void loadData(GildeType type) {
		Main.getDatenServer().getClient().getGildePlayer(lplayer, type).getAsync(new Callback<UUID>() {
			@Override
			public void call(UUID uuid, Throwable exception) {
				if (exception != null)
					exception.printStackTrace();
				if (uuid == null)
					gilden.put(type, null);
				else {
					Gilde gilde = Main.getGildeManager().getGilde(uuid);
					gilden.put(type, gilde);
				}
				//System.out.println("Having response: "+obj+" for "+type);
				while (isInAnimation()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (isActive())
					print();
			}
		});
	}
}
