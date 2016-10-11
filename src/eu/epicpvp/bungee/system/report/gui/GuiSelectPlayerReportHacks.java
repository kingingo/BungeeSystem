package eu.epicpvp.bungee.system.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.report.search.PlayerTextEnterMenue;

public class GuiSelectPlayerReportHacks extends Gui {
	private String target;
	private String targetNickname;

	public GuiSelectPlayerReportHacks(String target,String targetNickname) {
		super(6, "§aReporte §a" + targetNickname);
		this.target = target;
		this.targetNickname = targetNickname;
	}

	@Override
	public void build() { //386
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiSelectPlayerReportReson(target,targetNickname))).build());
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).name("§6" + targetNickname).durability(1).lore("§aGrund » §eHacks").build(), targetNickname));
		inv.setItem(8, ItemBuilder.create(386).name("§aNicht dabei? » §eSelber schreiben").lore("§aKicke um fortzufahren.").listener((c) -> {
			PlayerTextEnterMenue gui = new PlayerTextEnterMenue(getPlayer()) {
				@Override
				public void textEntered(String name) {
					GuiReportConfirm gui = new GuiReportConfirm(target, targetNickname, "Hacks", name);
					gui.setPlayer(getPlayer());
					gui.openGui();
				}

				@Override
				public void canceled() {}
			};
			gui.open();
		}).build());

		inv.setItem(11, ItemBuilder.create(Material.PAPER).name("§6Killaura").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","Killaura"))).build());
		inv.setItem(13, ItemBuilder.create(Material.PAPER).name("§6Fly").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","Fly"))).build());
		inv.setItem(15, ItemBuilder.create(Material.PAPER).name("§6NoKnockback").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","NoKnockback"))).build());

		inv.setItem(29, ItemBuilder.create(Material.PAPER).name("§6ForceField").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","ForceField"))).build());
		inv.setItem(31, ItemBuilder.create(Material.PAPER).name("§6Glide").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","Glide"))).build());
		inv.setItem(33, ItemBuilder.create(Material.PAPER).name("§6NoSlowdown").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","NoSlowdown"))).build());

		inv.setItem(47, ItemBuilder.create(Material.PAPER).name("§6Criticals").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","Criticals"))).build());
		inv.setItem(49, ItemBuilder.create(Material.PAPER).name("§6Speed").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","Speed"))).build());
		inv.setItem(51, ItemBuilder.create(Material.PAPER).name("§6Fastbuild").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Hacks","Fastbuild"))).build());
		//11 13 15
		//29 31 33
		//47 49 51

		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 0, 6 * 9);
	}
}
