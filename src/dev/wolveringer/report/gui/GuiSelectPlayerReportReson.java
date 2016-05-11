package dev.wolveringer.report.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.report.sarch.PlayerTextEnterMenue;

public class GuiSelectPlayerReportReson extends Gui{
	private String target;
	
	public GuiSelectPlayerReportReson(String target) {
		super(6,"§aReporte §a"+target);
		this.target = target;
	}
	
	@Override
	public void build() { //386
		inv.setItem(0, new ItemStack(ItemBuilder.create(Material.BARRIER).name("§cSchließen").build()){
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
			}
		});
		inv.setItem(4, loadSkin(ItemBuilder.create(Material.SKULL_ITEM).durbility(1).name("§6"+target).build(), target));
		inv.setItem(8, ItemBuilder.create(386).name("§aNicht dabei? » §eSelber schreiben").lore("§aKicke um fortzufahren.").listener((c)->{
			PlayerTextEnterMenue gui = new PlayerTextEnterMenue(getPlayer()) {
				@Override
				public void textEntered(String name) {
					GuiReportConfirm gui = new GuiReportConfirm(target, name, null);
					gui.setPlayer(getPlayer());
					gui.openGui();
				}
				
				@Override
				public void canceled() {}
			};
			gui.open();
		}).build());
		
		inv.setItem(11, ItemBuilder.create(Material.PAPER).name("§6Hacks").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiSelectPlayerReportHacks(target))).build());
		inv.setItem(13, ItemBuilder.create(Material.PAPER).name("§6Chatverhalten").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiSelectPlayerReportChat(target))).build());
		inv.setItem(15, ItemBuilder.create(Material.PAPER).name("§6Werbung").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiReportConfirm(target,"Werbung",null))).build());
		
		inv.setItem(29, ItemBuilder.create(Material.PAPER).name("§6Teaming").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiSelectPlayerReportQuestion(target,"Teaming","§6Wer ist der andereTeampartner?"))).build());
		inv.setItem(31, ItemBuilder.create(Material.PAPER).name("§6Coinfarming").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiReportConfirm(target,"Coinfarming",null))).build());
		inv.setItem(33, ItemBuilder.create(Material.PAPER).name("§6Trolling").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiSelectPlayerReportQuestion(target,"Trolling","§6Wie wurdest du getrolt?"))).build());
		
		inv.setItem(47, ItemBuilder.create(Material.PAPER).name("§6Random Killing").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiReportConfirm(target,"Random Killing",null))).build());
		inv.setItem(49, ItemBuilder.create(Material.PAPER).name("§6Bug-Using").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiSelectPlayerReportQuestion(target,"Bug-Using","§6Beschreibe diesen Bug näher."))).build());
		inv.setItem(51, ItemBuilder.create(Material.PAPER).name("§6Betrug").lore("§aKicke um fortzufahren.").listener((Click c)-> switchToGui(new GuiSelectPlayerReportQuestion(target,"Betrug","§6Um was wurdest du betrogen?"))).build());
		//11 13 15
		//29 31 33
		//47 49 51
		
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, 6*9);
	}
}
