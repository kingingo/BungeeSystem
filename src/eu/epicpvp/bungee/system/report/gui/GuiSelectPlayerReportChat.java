package eu.epicpvp.bungee.system.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.report.search.PlayerTextEnterMenue;

public class GuiSelectPlayerReportChat extends Gui{
	private String target;
	private String targetNickname;

	public GuiSelectPlayerReportChat(String target, String tarnetNickname) {
		super(6,"§aReporte §a" + tarnetNickname);
		this.target = target;
		this.targetNickname = tarnetNickname;
	}

	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiSelectPlayerReportReson(target,targetNickname))).build());
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).name("§6" + targetNickname).durbility(1).lore("§aGrund » §eChatverhalten").build(), targetNickname));
		inv.setItem(8, ItemBuilder.create(386).name("§aNicht dabei? » §eSelber schreiben").lore("§aKicke um fortzufahren.").listener((c) -> {
			PlayerTextEnterMenue gui = new PlayerTextEnterMenue(getPlayer()) {
				@Override
				public void textEntered(String name) {
					GuiReportConfirm gui = new GuiReportConfirm(target,targetNickname, "Chatverhalten", name);
					gui.setPlayer(getPlayer());
					gui.openGui();
				}

				@Override
				public void canceled() {}
			};
			gui.open();
		}).build());

		inv.setItem(11, ItemBuilder.create(Material.PAPER).name("§6Insult").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Insult"))).build());
		inv.setItem(13, ItemBuilder.create(Material.PAPER).name("§6Spam").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Spam"))).build());
		inv.setItem(15, ItemBuilder.create(Material.PAPER).name("§6Provokation").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Provokation"))).build());

		inv.setItem(29, ItemBuilder.create(Material.PAPER).name("§6Server-Insult").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Server-Insult"))).build());
		inv.setItem(31, ItemBuilder.create(Material.PAPER).name("§6Team-Insult").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Team-Insult"))).build());
		inv.setItem(33, ItemBuilder.create(Material.PAPER).name("§6Echtgeld-Handle").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Echtgeld-Handle"))).build());

		inv.setItem(47, ItemBuilder.create(Material.PAPER).name("§6Nationalsozialismus").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Nationalsozialismus"))).build());
		inv.setItem(49, ItemBuilder.create(Material.PAPER).name("§6Extremismus").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Extremismus"))).build());
		inv.setItem(51, ItemBuilder.create(Material.PAPER).name("§6Rassismus").lore("§aKicke um fortzufahren.").listener((c) -> switchToGui(new GuiReportConfirm(target,targetNickname,"Chatverhalten","Rassismus"))).build());
		//11 13 15
		//29 31 33
		//47 49 51

		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, 6 * 9);
	}
}
