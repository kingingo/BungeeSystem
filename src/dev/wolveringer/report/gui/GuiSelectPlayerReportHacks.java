package dev.wolveringer.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.report.sarch.PlayerTextEnterMenue;

public class GuiSelectPlayerReportHacks extends Gui {
	private String target;

	public GuiSelectPlayerReportHacks(String target) {
		super(6, "§aReporte §a" + target);
		this.target = target;
	}

	@Override
	public void build() { //386
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiSelectPlayerReportReson(target))).build());
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).name("§6" + target).durbility(1).lore("§aGrund » §eHacks").build(), target));
		inv.setItem(8, ItemBuilder.create(386).name("§aNicht dabei? » §eSelber schreiben").lore("§aKicke um fortzufahren.").listener((c) -> {
			PlayerTextEnterMenue gui = new PlayerTextEnterMenue(getPlayer()) {
				@Override
				public void textEntered(String name) {
					GuiReportConfirm gui = new GuiReportConfirm(target, "Hacks", name);
					gui.setPlayer(getPlayer());
					gui.openGui();
				}
				
				@Override
				public void canceled() {}
			};
			gui.open();
		}).build());

		inv.setItem(11, ItemBuilder.create(Material.PAPER).name("§6Killaura").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","Killaura"))).build());
		inv.setItem(13, ItemBuilder.create(Material.PAPER).name("§6Fly").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","Fly"))).build());
		inv.setItem(15, ItemBuilder.create(Material.PAPER).name("§6NoKnockback").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","NoKnockback"))).build());

		inv.setItem(29, ItemBuilder.create(Material.PAPER).name("§6ForceField").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","ForceField"))).build());
		inv.setItem(31, ItemBuilder.create(Material.PAPER).name("§6Glide").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","Glide"))).build());
		inv.setItem(33, ItemBuilder.create(Material.PAPER).name("§6NoSlowdown").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","NoSlowdown"))).build());

		inv.setItem(47, ItemBuilder.create(Material.PAPER).name("§6Criticals").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","Criticals"))).build());
		inv.setItem(49, ItemBuilder.create(Material.PAPER).name("§6Speed").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","Speed"))).build());
		inv.setItem(51, ItemBuilder.create(Material.PAPER).name("§6Fastbuild").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,"Hacks","Fastbuild"))).build());
		//11 13 15
		//29 31 33
		//47 49 51

		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, 6 * 9);
	}
}
