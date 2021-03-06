package eu.epicpvp.bungee.system.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.gilde.GildSection;
import eu.epicpvp.datenserver.definitions.gilde.GildeType;

public class GuiGildeKickPlayer extends Gui {

	private GildSection section;
	private LoadedPlayer player;

	private int modes[] = new int[4];

	public GuiGildeKickPlayer(GildSection section, LoadedPlayer player) {
		super(2, "§a" + section.getType().getDisplayName() + " §6» §aKick Player");
		this.section = section;
		this.player = player;
	}

	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiGildeMemberAdminPanel(section, player))).build());
		fill(ItemBuilder.create(160).durability(7).name("§7").build());
		for (int i = 0; i < GildeType.getPossibleValues().length; i++)
			buildSection(GildeType.getPossibleValues()[i], i);
		buildContinueItem();
	}

	private void buildContinueItem() {
		boolean possible = false;
		for (int i : modes)
			if (i == 1)
				possible = true;
		ItemBuilder builder = ItemBuilder.create();
		if (possible) {
			builder.id(399).name("§aKick player").listener((c) -> {
				c.getPlayer().closeInventory();
				int sectionCount = 0;
				for (int i = 0; i < modes.length; i++) {
					if (modes[i] == 1) {
						sectionCount++;
						section.getHandle().getSelection(GildeType.getPossibleValues()[i]).kickPlayer(player);
					}
				}

				String sectionText = "";
				if (sectionCount == 1) {
					sectionText += " dem Bereich ";
				} else {
					sectionText += " den Bereichen ";
				}
				for (int i = 0; i < modes.length; i++) {
					if (modes[i] == 1) {
						sectionText += ", " + GildeType.getPossibleValues()[i].getDisplayName();
					}
				}
				sectionText = sectionText.replaceFirst(", ", "");

				Main.getDatenServer().getClient().sendMessage(player.getPlayerId(), "§aDu wurdest aus " + sectionCount + " von dem Clan §e" + section.getHandle().getName() + "§a rausgeworfen.");
				getPlayer().sendMessage("§aDu hast den Spieler §e" + player.getName() + " §aaus " + sectionCount + " §arausgeworfen.");
			});
		} else {
			builder.id(351).durability(8).name("§cKickeden Spieler");
		}
		inv.setItem(17, builder.build());
	}

	private void buildSection(GildeType type, int index) {
		ItemBuilder builder = ItemBuilder.create(Material.ITEM_FRAME).name("§aKicke Spieler von Clanbereich " + type.getDisplayName());
		inv.setItem(index * 2, builder.build());

		builder = ItemBuilder.create(160);
		if (section.isActive() && section.getPlayers().contains(player.getPlayerId())) {
			builder.durability(modes[index] == 0 ? 14 : 13);
			builder.listener((c) -> {
				modes[index] = modes[index] == 0 ? 1 : 0;
				buildSection(type, index);
				buildContinueItem();
			});
		} else {
			builder.name("§cDer Spieler ist kein Mitglied des Clanbereiches " + type.getDisplayName());
			builder.durability(15);
			modes[index] = 0;
		}
		inv.setItem(index * 2 + 9, builder.build());
	}
}
