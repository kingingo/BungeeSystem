package dev.wolveringer.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.bs.Main;
import dev.wolveringer.gilde.Gilde;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildeOverview extends Gui{

	private Gilde gilde;
	
	
	public GuiGildeOverview(Gilde gilde) {
		super(5, "§a"+gilde.getName());
		this.gilde = gilde;
	}



	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cSchließen").listener((Click c) -> c.getPlayer().closeInventory()).build());
		inv.setItem(19, ItemBuilder.create(345).name("§7» §6Games").listener((c)->  {
			
		}).glow(!gilde.getSelection(GildeType.ARCADE).isActive()).lore(gilde.getSelection(GildeType.ARCADE).isActive() ? "§aKlicke um in den Gildenbereich bereich zu kommen." : gilde.getOwnerId() == Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName()).getPlayerId() ? "§aKlicke um den Gildenbereich zu aktivieren" : "§cDieser Gildenbereich ist deaktiviert.").build());
		
		inv.setItem(21, ItemBuilder.create(279).name("§7» §6PvP").listener((c)->  {
			
		}).glow(!gilde.getSelection(GildeType.PVP).isActive()).lore(gilde.getSelection(GildeType.PVP).isActive() ? "§aKlicke um in den Gildenbereich bereich zu kommen." : gilde.getOwnerId() == Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName()).getPlayerId() ? "§aKlicke um den Gildenbereich zu aktivieren" : "§cDieser Gildenbereich ist deaktiviert.").build());
		
		inv.setItem(23, ItemBuilder.create(3).name("§7» §6SkyBlock").listener((c)->  {
			
		}).glow(!gilde.getSelection(GildeType.SKY).isActive()).lore(gilde.getSelection(GildeType.SKY).isActive() ? "§aKlicke um in den Gildenbereich bereich zu kommen." : gilde.getOwnerId() == Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName()).getPlayerId() ? "§aKlicke um den Gildenbereich zu aktivieren" : "§cDieser Gildenbereich ist deaktiviert.").build());
		
		inv.setItem(25, ItemBuilder.create(261).name("§7» §6Versus").listener((c)->  {
			
		}).glow(!gilde.getSelection(GildeType.VERSUS).isActive()).lore(gilde.getSelection(GildeType.VERSUS).isActive() ? "§aKlicke um in den Gildenbereich bereich zu kommen." : gilde.getOwnerId() == Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName()).getPlayerId() ? "§aKlicke um den Gildenbereich zu aktivieren" : "§cDieser Gildenbereich ist deaktiviert.").build());

		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
	}

}
