package eu.epicpvp.bungee.system.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gilde.GildSection;
import dev.wolveringer.gilde.GildePermissions;
import dev.wolveringer.gilde.MoneyLogRecord;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.gui.GuiStatusPrint;
import eu.epicpvp.bungee.system.gui.GuiWaiting;
import eu.epicpvp.bungee.system.gui.GuiYesNo;
import eu.epicpvp.bungee.system.guild.gui.section.SectionRegestry;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import dev.wolveringer.thread.ThreadFactory;

public class GuiGildeMemberAdminPannel extends Gui{
	private GildSection section;
	private LoadedPlayer player;

	public GuiGildeMemberAdminPannel(GildSection section, LoadedPlayer player) {
		super(4, "§a"+section.getType().getDisplayName()+" §6» §aMember » §a"+player.getName());
		this.section = section;
		this.player = player;
	}



	@Override
	public void build() {
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).name("§6"+player.getName()).lore("§c").lore("§aGruppe§7: §a"+section.getPermission().getGroup(player).getName()).build(), player.getName()));
		inv.setItem(19, ItemBuilder.create(Material.DIAMOND).listener((c)->{
			if(player.getPlayerId() == section.getHandle().getOwnerId()){
				new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu kannst die Gruppe des Guilden-Owners nicht ändern!").build()) {
					@Override
					public void onContinue() {
						new GuiGildeMemberAdminPannel(section, player).setPlayer(getPlayer()).openGui();
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			if(!section.getPermission().hasPermission(player, GildePermissions.MEMBER_GROUP_CHANGE)){
				new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu hast keine Berechtigung Gruppen zu ändern!").build()) {
					@Override
					public void onContinue() {
						new GuiGildeMemberAdminPannel(section, player).setPlayer(getPlayer()).openGui();
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			switchToGui(new GuiGildeSelectGoup(section, player));
		}).name("§7» §6Gruppe Setzen").lore("§aKlicke hier um fortzufahren.").build());

		inv.setItem(22, ItemBuilder.create(Material.FEATHER).listener((c)->{
			if(player.getPlayerId() == section.getHandle().getOwnerId()){
				new GuiStatusPrint(5,ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu kannst den Owner der Gilde nicht kicken!").build()) {
					@Override
					public void onContinue() {
						new GuiGildeMemberAdminPannel(section, player).setPlayer(getPlayer()).openGui();
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			if(!section.getPermission().hasPermission(player, GildePermissions.MEMBER_KICK)){
				new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu hast keine Berechtigung einen Member zu kicken!").build()) {
					@Override
					public void onContinue() {
						new GuiGildeMemberAdminPannel(section, player).setPlayer(getPlayer()).openGui();
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}

			new GuiYesNo("§cBist du dir sicher?", null) {
				@Override
				public void onDicition(boolean flag) {
					ThreadFactory.getFactory().createThread(()->{
						if(flag){
							GuiWaiting w = new GuiWaiting(Main.getTranslationManager().translate("waiting.title", getPlayer()), Main.getTranslationManager().translate("waiting.message", getPlayer()));
							w.setPlayer(getPlayer()).openGui();

							w.waitForMinwait(1500);
							section.kickPlayer(player);

							Main.getDatenServer().getClient().sendMessage(player.getPlayerId(), "§cDu wurdest aus der Gilde "+section.getHandle().getName()+" gekickt!");

							new GuiStatusPrint(6,ItemBuilder.create(Material.EMERALD).name("§Member erfolgreich gekickt!").build()) {
								@Override
								public void onContinue() {
									SectionRegestry.getInstance().createGildeSection(section).setPlayer(getPlayer()).openGui();
								}
							}.setPlayer(getPlayer()).openGui();;
						}else {
							new GuiGildeMemberAdminPannel(section, player).setPlayer(getPlayer()).openGui();
						}
					}).start();
				}
			}.setPlayer(getPlayer()).openGui();
		}).name("§7» §6Mitglied kicken.").lore("§aKlicke hier um fortzufahren.").build());

		inv.setItem(25, ItemBuilder.create(Material.GOLD_NUGGET).listener((c)->{
			new GuiGildeMoneyOverview(section.getMoney(), new GuiGildeMoneyOverview.LogFilter() {
				@Override
				public boolean accept(MoneyLogRecord record) {
					return record.getPlayerId() == player.getPlayerId();
				}
			}).setPlayer(getPlayer()).openGui();
		}).name("§7» §6Player Money Statistiken").lore("§aKlicke hier um fortzufahren.").build());

		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiGildeMemberManager(section.getPermission()))).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
	}
}
