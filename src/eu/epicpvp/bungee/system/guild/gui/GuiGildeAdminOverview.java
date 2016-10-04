package dev.wolveringer.guild.gui;

import java.util.HashMap;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import dev.wolveringer.client.Callback;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.player.LanguageType;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildActionResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildActionResponse.Action;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus;
import dev.wolveringer.gilde.Gilde;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.gui.GuiStatusPrint;
import dev.wolveringer.gui.GuiWaiting;
import dev.wolveringer.gui.GuiYesNo;
import dev.wolveringer.guild.gui.section.SectionRegestry;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.thread.ThreadFactory;

public class GuiGildeAdminOverview extends Gui{
	private static HashMap<GildeType, Integer> itemMapping = new HashMap<>();
	static {
		itemMapping.put(GildeType.ARCADE, 345);
		itemMapping.put(GildeType.PVP, 279);
		itemMapping.put(GildeType.SKY, 3);
		itemMapping.put(GildeType.VERSUS, 261);
		itemMapping.put(GildeType.WARZ, 367);

		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "inventory.item.close", "§cSchließen"); //
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.item", "§cGilde löschen");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.title", "§cBist du sicher?");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.message", "§cDu kannst diese Aktion nicht mehr rückgänig machen!");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.error.message", "§cBei dem Löschen deiner Gilde gab es einen Fehler!");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "gilde.delete.success", "§aDeine Gilde wurde erfolgreich gelöscht!");

		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "waiting.title", "§aBitte warte einen moment");
		Main.getTranslationManager().registerFallback(LanguageType.GERMAN, "waiting.message", "§aAuftrag wird ausgeführt.");
	}

	private Player player;
	private LoadedPlayer lplayer;
	private Gilde gilde;

	public GuiGildeAdminOverview(Player player, Gilde gilde) {
		super(5, "§a"+gilde.getName()+" §7» §6Admin Pannel");
		this.player = player;
		this.lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(player.getName());
		this.gilde = gilde;
	}

	@Override
	public void build() {
		print();
	}

	private void print(){
		inv.disableUpdate();
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0 , -1,true);
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name(Main.getTranslationManager().translate("inventory.item.close", getPlayer())).listener((Click c) -> c.getPlayer().closeInventory()).build());
		inv.setItem(8, ItemBuilder.create(Material.LAVA_BUCKET).name(Main.getTranslationManager().translate("gilde.delete.item", getPlayer())).listener(new Runnable() { //TODO check perms
			@Override
			public void run() {
				Gui question = new GuiYesNo(Main.getTranslationManager().translate("gilde.delete.title", getPlayer()),Main.getTranslationManager().translate("gilde.delete.message", getPlayer())) {
					@Override
					public void onDicition(boolean flag) {
						if(flag){
							new GuiWaiting(Main.getTranslationManager().translate("waiting.title", getPlayer()), Main.getTranslationManager().translate("waiting.message", getPlayer())).setPlayer(player).openGui();
							Main.getGildeManager().deleteGilde(gilde, true).getAsync(new Callback<PacketGildActionResponse>() {
								@Override
								public void call(PacketGildActionResponse obj, Throwable exception) {
									if(obj == null){
										player.sendMessage(Main.getTranslationManager().translate("gilde.delete.error.message", getPlayer()));
										if(exception != null)
											exception.printStackTrace();
									}
									else if(obj.getAction() == Action.ERROR){
										player.sendMessage(Main.getTranslationManager().translate("gilde.delete.error.message", getPlayer()));
										System.out.println("An error happend while deleting gilde "+gilde.getName()+" ("+obj.getMessage()+")");
									}
									else
									{
										player.sendMessage(Main.getTranslationManager().translate("gilde.delete.error.message", getPlayer()));
									}
									player.closeInventory();
								}
							});;
						}
					}
				};
				question.setPlayer(player).openGui();
			}
		}).build());
		inv.setItem(19, buildSection(GildeType.ARCADE));
		inv.setItem(30, buildSection(GildeType.PVP));
		inv.setItem(32, buildSection(GildeType.SKY));
		inv.setItem(25, buildSection(GildeType.VERSUS));
		inv.setItem(13, buildSection(GildeType.WARZ));
		inv.enableUpdate();
	}

	public Item buildSection(GildeType type){
		ItemBuilder item = ItemBuilder.create(itemMapping.get(type));
		item.name("§7» §6"+type.getDisplayName());
		if(gilde.getSelection(type).isActive()){
			item.lore("§aKlicke um in den Gildenbereich bereich");
			item.lore("§a"+type.getDisplayName()+" zu kommen.");
			item.listener((c)->{
				switchToGui(SectionRegestry.getInstance().createGildeSection(gilde.getSelection(type)));
			});
		}
		else
		{
			if(gilde.getOwnerId() == lplayer.getPlayerId()){
				item.lore("§aDieser Gildenbereich ist deaktiviert.");
				item.lore("§6Klicke hier um den bereich zu aktivieren.");
				item.listener((c)->{
					GuiWaiting waiting = new GuiWaiting(Main.getTranslationManager().translate("waiting.title", getPlayer()), Main.getTranslationManager().translate("waiting.message", getPlayer()));
					waiting.setPlayer(player).openGui();
					ThreadFactory.getFactory().createThread(()->{
						if(Main.getGildeManager().getGildeSync(lplayer, type) != null){
							new GuiStatusPrint(5, ItemBuilder.create().material(Material.EMERALD).name("§cDu kannst kein Owner dieses Bereichs sein,").lore("da du hier bereits in einer anderen Gilde bist.").build()) {
								@Override
								public void onContinue() {
									player.closeInventory();
								}
							}.setPlayer(player).openGui();
							return;
						}
						if(!gilde.getSelection(type).isActive()){
							gilde.getSelection(type).setActive(true).getAsync(new Callback<PacketOutPacketStatus.Error[]>() {
								@Override
								public void call(PacketOutPacketStatus.Error[] obj, Throwable exception) {
									if(exception != null){
										new GuiStatusPrint(5, ItemBuilder.create().material(Material.REDSTONE_BLOCK).name("§cEs ist ein Fehler aufgetreten ("+exception.getClass().getName()+" -> "+exception.getMessage()+")").build()) {
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
									new GuiStatusPrint(5, ItemBuilder.create().material(Material.EMERALD).name("§aDie Gildsection wurde activiert.").build()) {
										@Override
										public void onContinue() {
											SectionRegestry.getInstance().createGildeSection(gilde.getSelection(type)).setPlayer(player).openGui();
										}
									}.setPlayer(player).openGui();
								}
							});;
						}
					}).start();
				});
			}
			else
			{
				item.id(289).name("§6Dieser Gildenbereich ist leider disabled.").lore("§aNur der Gilde-Owner kann Sectionen activieren.");
			}
		}

		return item.build();
	}
}
