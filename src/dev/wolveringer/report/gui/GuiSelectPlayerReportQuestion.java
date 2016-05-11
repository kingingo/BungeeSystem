package dev.wolveringer.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.report.sarch.PlayerTextEnterMenue;

public class GuiSelectPlayerReportQuestion extends Gui{
	private String target;
	private String categorisation;
	private String question;
	
	public GuiSelectPlayerReportQuestion(String target,String categorisation,String question) {
		super(1,"§aReporte §a" + target);
		this.target= target;
		this.categorisation = categorisation;
		this.question = question;
	}
	
	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiSelectPlayerReportReson(target))).build());
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).durbility(1).name("§6" + target).lore("§aGrund » §e"+categorisation).build(), target));
		inv.setItem(8, ItemBuilder.create(386).name(question).lore("§aKicke um fortzufahren.").listener((c) -> {
			PlayerTextEnterMenue gui = new PlayerTextEnterMenue(getPlayer()) {
				@Override
				public void textEntered(String name) {
					GuiReportConfirm gui = new GuiReportConfirm(target, categorisation, name);
					gui.setPlayer(getPlayer());
					gui.openGui();
				}
				
				@Override
				public void canceled() {}
			};
			gui.open();
		}).build());

		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, -1);
	}
}
