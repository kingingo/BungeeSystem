package dev.wolveringer.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gilde.GildSection;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildeMemberAdminPannel extends Gui{
	private GildSection section;
	private LoadedPlayer player;
	
	public GuiGildeMemberAdminPannel(GildSection section, LoadedPlayer player) {
		super(4, "§a"+section.getType().getDisplayName()+" §6» §aMember"); //TODO Change!
		this.section = section;
		this.player = player;
	}



	@Override
	public void build() {
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).name("§6"+player.getName()).lore("§c").lore("§eGroup§7: §a"+section.getPermission().getGroup(player).getName()).build(), player.getName()));
		
		inv.setItem(28, ItemBuilder.create(Material.DIAMOND).listener((c)->{
			switchToGui(new GuiGildeSelectGoup(section, player));
		}).name("§7» §6Gruppe Setzen").lore("§aKlicke hier um fortzufahren.").build());
		
		inv.setItem(30, ItemBuilder.create(Material.FEATHER).listener((c)->{
			switchToGui(new GuiGildeKickPlayer(section, player));
		}).name("§7» §6Mitglied kicken.").lore("§aKlicke hier um fortzufahren.").build());
		
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiGildeMemberManager(section.getPermission()))).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
	}
}
