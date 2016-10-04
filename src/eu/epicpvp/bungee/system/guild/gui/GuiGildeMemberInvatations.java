package dev.wolveringer.guild.gui;

import java.util.ArrayList;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gilde.GildSection;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.gui.GuiStatusPrint;
import dev.wolveringer.gui.GuiYesNo;
import dev.wolveringer.guild.gui.section.SectionRegestry;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildeMemberInvatations extends Gui{
	private GildSection section;
	private int side;

	public GuiGildeMemberInvatations(GildSection section) {
		super(6, "§a"+section.getType().getDisplayName()+" §6» §aRequests"); //TODO
		this.section = section;
	}

	@Override
	public void build() {
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(),36,44);
		updateInventory();
	}

	private void updateInventory(){
		updateBar();
		drawSection();
	}

	private void drawSection(){
		ArrayList<Integer> records = section.getRequestedPlayer();
		for(int i = 0;i<4*9;i++)
			if(side*4*9+i < records.size())
				inv.setItem(i, buildItem(records.get(side*4*9+i)));
	}

	private Item buildItem(int playerId){
		ItemBuilder builder = ItemBuilder.create(Material.SKULL_ITEM).durbility(3);
		LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(playerId);
		builder.name("§a"+lp.getName());
		builder.listener(()->{
			new GuiYesNo("§aAnfrage von §e"+lp.getName()+" §aannehmen?",null) {
				@Override
				public void onDicition(boolean flag) {
					if(Main.getGildeManager().getGildeSync(lp, section.getType()) != null){
						new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDieser Spieler ist bereits in einer Gilde!").build()) {
							@Override
							public void onContinue() {
								new GuiGildeMemberInvatations(section).setPlayer(getPlayer()).openGui();
							}
						}.setPlayer(getPlayer()).openGui();;
						return;
					}
					if(flag){
						getPlayer().sendMessage("§aDu hast die Anfrage von "+lp.getName()+" angenommen.");
						section.acceptRequest(lp);
					}
					else
					{
						getPlayer().sendMessage("§aDu hast die Anfrage von "+lp.getName()+" abgelehnt.");
						section.removeRequest(lp);
					}
					new GuiGildeMemberInvatations(section).setPlayer(getPlayer()).openGui();
				}
			}.setPlayer(getPlayer()).openGui();
		});
		return loadSkin(builder.build(), lp.getName());
	}

	private void updateBar(){
		inv.setItem(45, ItemBuilder.create(Material.ARROW).name("§7Vorherige Seite "+(side != 0 ? "("+(side-1)+")":"")).listener(()-> {
			if(side > 0){
				side--;
				updateInventory();
			}
		}).glow(side == 0).build());

		inv.setItem(49, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(SectionRegestry.getInstance().createGildeSection(section))).build());

		inv.setItem(53, ItemBuilder.create(Material.ARROW).name("§7Nächste Seite "+(side*4*9 > section.getRequestedPlayer().size() ? "("+(side+1)+")":"")).listener(()-> {
			if(side > 0){
				side++;
				updateInventory();
			}
		}).glow((side+1)*4*9 > section.getRequestedPlayer().size()).build());
	}


}
