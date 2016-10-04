package dev.wolveringer.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import dev.wolveringer.report.search.PlayerTextEnterMenue;

public class GuiSelectPlayerReportQuestion extends Gui{
	private String target;
	private String targetNickname;
	private String categorisation;
	private String question;

	public GuiSelectPlayerReportQuestion(String target,String targetNickname,String categorisation,String question) {
		super(1,"§aReporte §a" + targetNickname);
		this.target= target;
		this.categorisation = categorisation;
		this.question = question;
		this.targetNickname = targetNickname;
	}

	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiSelectPlayerReportReson(target,targetNickname))).build());
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).durbility(1).name("§6" + targetNickname).lore("§aGrund » §e"+categorisation).build(), targetNickname));
		inv.setItem(8, ItemBuilder.create(386).name(question).lore("§aKicke um fortzufahren.").listener((c) -> {
			PlayerTextEnterMenue gui = new PlayerTextEnterMenue(getPlayer()) {
				@Override
				public void textEntered(String name) {
					GuiReportConfirm gui = new GuiReportConfirm(target, targetNickname, categorisation, name);
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
