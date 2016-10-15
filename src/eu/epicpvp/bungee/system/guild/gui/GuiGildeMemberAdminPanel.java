package eu.epicpvp.bungee.system.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.gui.GuiStatusPrint;
import eu.epicpvp.bungee.system.gui.GuiWaiting;
import eu.epicpvp.bungee.system.gui.GuiYesNo;
import eu.epicpvp.bungee.system.guild.gui.section.SectionRegestry;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.gilde.GildSection;
import eu.epicpvp.datenserver.definitions.gilde.GildePermissions;
import eu.epicpvp.thread.ThreadFactory;

public class GuiGildeMemberAdminPanel extends Gui {

	private GildSection section;
	private LoadedPlayer targetPlayer;

	public GuiGildeMemberAdminPanel(GildSection section, LoadedPlayer targetPlayer) {
		super(4, "§a" + section.getType().getDisplayName() + " §6» §aMember » §a" + targetPlayer.getName());
		this.section = section;
		this.targetPlayer = targetPlayer;
	}

	@Override
	public void build() {
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).name("§6" + targetPlayer.getName()).lore("§c").lore("§aGruppe§7: §a" + section.getPermission().getGroup(targetPlayer).getName()).build(), targetPlayer.getName()));
		inv.setItem(19, ItemBuilder.create(Material.DIAMOND).listener((c) -> {
			if (targetPlayer.getPlayerId() == section.getHandle().getOwnerId()) {
				new GuiStatusPrint(6, "§cDu kannst die Gruppe des Clan-Owners nicht ändern!", ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu kannst die Gruppe des Clan-Owners nicht ändern!").build()) {
					@Override
					public void onContinue() {
						new GuiGildeMemberAdminPanel(section, targetPlayer).setPlayer(getPlayer()).openGui();
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			if (!section.getPermission().hasPermission(Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getUniqueId()), GildePermissions.MEMBER_GROUP_CHANGE)) {
				new GuiStatusPrint(6, "§cDu hast keine Berechtigung, Gruppen zu ändern!", ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu hast keine Berechtigung, Gruppen zu ändern!").build()) {
					@Override
					public void onContinue() {
						new GuiGildeMemberAdminPanel(section, targetPlayer).setPlayer(getPlayer()).openGui();
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			switchToGui(new GuiGildeSelectGroup(section, targetPlayer));
		}).name("§7» §6Gruppe setzen").lore("§aKlicke, um fortzufahren.").build());

		inv.setItem(22, ItemBuilder.create(Material.FEATHER).listener((c) -> {
			if (targetPlayer.getPlayerId() == section.getHandle().getOwnerId()) {
				new GuiStatusPrint(5, "§cDu kannst den Owner des Clans nicht kicken!", ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu kannst den Owner des Clans nicht kicken!").build()) {
					@Override
					public void onContinue() {
						new GuiGildeMemberAdminPanel(section, targetPlayer).setPlayer(getPlayer()).openGui();
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}
			if (!section.getPermission().hasPermission(Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getUniqueId()), GildePermissions.MEMBER_KICK)) {
				new GuiStatusPrint(6, "§cDu hast keine Berechtigung, einen Member zu kicken!", ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu hast keine Berechtigung, einen Member zu kicken!").build()) {
					@Override
					public void onContinue() {
						new GuiGildeMemberAdminPanel(section, targetPlayer).setPlayer(getPlayer()).openGui();
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}

			new GuiYesNo("§cBist du dir sicher?", null) {
				@Override
				public void onDicition(boolean flag) {
					ThreadFactory.getFactory().createThread(() -> {
						if (flag) {
							GuiWaiting w = new GuiWaiting(Main.getTranslationManager().translate("waiting.title", getPlayer()), Main.getTranslationManager().translate("waiting.message", getPlayer()));
							w.setPlayer(getPlayer()).openGui();

							w.waitForMinwait(1500);
							section.kickPlayer(targetPlayer);

							Main.getDatenServer().getClient().sendMessage(targetPlayer.getPlayerId(), "§cDu wurdest aus dem Clan " + section.getHandle().getName() + " gekickt!");

							new GuiStatusPrint(6, "§aMember erfolgreich gekickt!", ItemBuilder.create(Material.EMERALD).name("§aMember erfolgreich gekickt!").build()) {
								@Override
								public void onContinue() {
									SectionRegestry.getInstance().createGildeSection(section).setPlayer(getPlayer()).openGui();
								}
							}.setPlayer(getPlayer()).openGui();
							;
						} else {
							new GuiGildeMemberAdminPanel(section, targetPlayer).setPlayer(getPlayer()).openGui();
						}
					}).start();
				}
			}.setPlayer(getPlayer()).openGui();
		}).name("§7» §6Mitglied kicken.").lore("§aKlicke, um fortzufahren.").build());

//		inv.setItem(25, ItemBuilder.create(Material.GOLD_NUGGET).listener((c) -> {
//			new GuiGildeMoneyOverview(section.getMoney(), new GuiGildeMoneyOverview.LogFilter() {
//				@Override
//				public boolean accept(MoneyLogRecord record) {
//					return record.getPlayerId() == targetPlayer.getPlayerId();
//				}
//			}).setPlayer(getPlayer()).openGui();
//		}).name("§7» §6Geldstatistiken").lore("§aKlicke, um fortzufahren.").build());

		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiGildeMemberManager(section.getPermission()))).build());
		fill(ItemBuilder.create(160).durability(7).name("§7").build());
	}
}
