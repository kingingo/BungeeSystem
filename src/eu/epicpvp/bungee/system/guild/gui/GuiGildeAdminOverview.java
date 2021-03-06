package eu.epicpvp.bungee.system.guild.gui;

import java.util.HashMap;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.gui.GuiStatusPrint;
import eu.epicpvp.bungee.system.gui.GuiWaiting;
import eu.epicpvp.bungee.system.gui.GuiYesNo;
import eu.epicpvp.bungee.system.guild.gui.section.SectionRegestry;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildActionResponse;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildActionResponse.Action;
import eu.epicpvp.dataserver.protocoll.packets.PacketOutPacketStatus;
import eu.epicpvp.datenclient.client.Callback;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.gilde.Gilde;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.datenserver.definitions.gilde.GildeType;
import eu.epicpvp.thread.ThreadFactory;

public class GuiGildeAdminOverview extends Gui {

	private static HashMap<GildeType, Integer> itemMapping = new HashMap<>();

	static {
//		itemMapping.put(GildeType.ARCADE, 345);
//		itemMapping.put(GildeType.PVP, 279);
//		itemMapping.put(GildeType.SKY, 3);
//		itemMapping.put(GildeType.VERSUS, 261);
		itemMapping.put(GildeType.WARZ, 367);

		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "inventory.item.close", "§cClose");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "gilde.delete.item", "§cDelete clan");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "gilde.delete.title", "§cAre you sure?");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "gilde.delete.message", "§cYou cannot undo this action!");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "gilde.delete.error.message", "§cAn error occured while deleting the clan!");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "gilde.delete.success", "§aYour clan has been deleted successfully!");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "waiting.title", "§aPlease wait a moment");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "waiting.message", "§aprocessing...");

		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "inventory.item.close", "§cSchließen");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.item", "§cClan löschen");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.title", "§cBist du sicher?");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.message", "§cDu kannst diese Aktion nicht mehr rückgängig machen!");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.error.message", "§cBeim Löschen deines Clans gab es einen Fehler!");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.success", "§aDein Clan wurde erfolgreich gelöscht!");

		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "waiting.title", "§aBitte warte einen Moment");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "waiting.message", "§aBearbeite...");
	}

	private Player player;
	private LoadedPlayer lplayer;
	private Gilde gilde;

	public GuiGildeAdminOverview(Player player, Gilde gilde) {
		super(5, "§a" + gilde.getName() + " §7» §6Administration");
		this.player = player;
		this.lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(player.getName());
		this.gilde = gilde;
	}

	@Override
	public void build() {
		print();
	}

	private void print() {
		inv.disableUpdate();
		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 0, -1, true);
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name(Main.getTranslationManager().translate("inventory.item.close", getPlayer())).listener((Click c) -> c.getPlayer().closeInventory()).build());
		inv.setItem(8, ItemBuilder.create(Material.LAVA_BUCKET).name(Main.getTranslationManager().translate("gilde.delete.item", getPlayer())).listener(new Runnable() { //TODO check perms
			@Override
			public void run() {
				Gui question = new GuiYesNo(Main.getTranslationManager().translate("gilde.delete.title", getPlayer()), Main.getTranslationManager().translate("gilde.delete.message", getPlayer())) {
					@Override
					public void onDicition(boolean flag) {
						if (flag) {
							new GuiWaiting(Main.getTranslationManager().translate("waiting.title", getPlayer()), Main.getTranslationManager().translate("waiting.message", getPlayer())).setPlayer(player).openGui();
							Main.getGildeManager().deleteGilde(gilde, true).getAsync(new Callback<PacketGildActionResponse>() {
								@Override
								public void call(PacketGildActionResponse obj, Throwable exception) {
									if (obj == null) {
										player.sendMessage(Main.getTranslationManager().translate("gilde.delete.error.message", getPlayer()));
										if (exception != null)
											exception.printStackTrace();
									} else if (obj.getAction() == Action.ERROR) {
										player.sendMessage(Main.getTranslationManager().translate("gilde.delete.error.message", getPlayer()));
										System.out.println("An error happend while deleting clan " + gilde.getName() + " (" + obj.getMessage() + ")");
									} else {
										player.sendMessage(Main.getTranslationManager().translate("gilde.delete.error.message", getPlayer()));
									}
									player.closeInventory();
								}
							});
							;
						}
					}
				};
				question.setPlayer(player).openGui();
			}
		}).build());
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
		if (gilde.getSelection(type).isActive()) {
			item.lore("§aKlicke, um diesen Clanbereich zu verwalten");
			item.listener((c) -> {
				switchToGui(SectionRegestry.getInstance().createGildeSection(gilde.getSelection(type)));
			});
		} else {
			if (gilde.getOwnerId() == lplayer.getPlayerId()) {
				item.lore("§cDieser Clanbereich ist deaktiviert.");
				item.lore("§6Klicke, um den Bereich zu aktivieren.");
				item.listener((c) -> {
					GuiWaiting waiting = new GuiWaiting(Main.getTranslationManager().translate("waiting.title", getPlayer()), Main.getTranslationManager().translate("waiting.message", getPlayer()));
					waiting.setPlayer(player).openGui();
					ThreadFactory.getFactory().createThread(() -> {
						if (Main.getGildeManager().getGildeSync(lplayer, type) != null) {
							new GuiStatusPrint(5, "§cDu kannst kein Owner dieses Bereichs sein", ItemBuilder.create().material(Material.EMERALD).name("§cDu kannst kein Owner dieses Bereichs sein,").lore("da du hier bereits in einem anderen Clan bist.").build()) {
								@Override
								public void onContinue() {
									player.closeInventory();
								}
							}.setPlayer(player).openGui();
							return;
						}
						if (!gilde.getSelection(type).isActive()) {
							gilde.getSelection(type).setActive(true).getAsync(new Callback<PacketOutPacketStatus.Error[]>() {
								@Override
								public void call(PacketOutPacketStatus.Error[] obj, Throwable exception) {
									if (exception != null) {
										exception.printStackTrace();
										new GuiStatusPrint(5, "§cInterner Fehler", ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cEs ist ein Fehler aufgetreten (" + exception.getClass().getName() + " -> " + exception.getMessage() + ")").build()) {
											@Override
											public void onContinue() {
												SectionRegestry.getInstance().createGildeSection(gilde.getSelection(type)).setPlayer(player).openGui();
											}
										}.setPlayer(player).openGui();
									}
									gilde.getSelection(type).getMoney().init();
									gilde.getSelection(type).addMemeber(lplayer);
									gilde.getSelection(type).getPermission().setGroup(lplayer, gilde.getSelection(type).getPermission().getGroup("owner"));
									waiting.waitForMinwait(1500);
									new GuiStatusPrint(5, "§aDer Clanbereich wurde aktiviert", ItemBuilder.create().material(Material.EMERALD).name("§aDer Clanbereich wurde aktiviert.").build()) {
										@Override
										public void onContinue() {
											SectionRegestry.getInstance().createGildeSection(gilde.getSelection(type)).setPlayer(player).openGui();
										}
									}.setPlayer(player).openGui();
								}
							});
							;
						}
					}).start();
				});
			} else {
				item.id(289).name("§6Dieser Clanbereich ist leider disabled.").lore("§aNur der Clan-Owner kann Sectionen activieren.");
			}
		}

		return item.build();
	}
}
