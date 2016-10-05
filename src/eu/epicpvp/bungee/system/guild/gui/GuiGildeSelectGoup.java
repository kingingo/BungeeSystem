package eu.epicpvp.bungee.system.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import eu.epicpvp.bungee.system.gui.GuiItemSelect;
import eu.epicpvp.bungee.system.gui.GuiStatusPrint;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.gilde.GildSection;

public class GuiGildeSelectGoup extends GuiItemSelect{
	private GildSection section;
	private LoadedPlayer player;

	private String[] groups;

	public GuiGildeSelectGoup(GildSection section, LoadedPlayer player) {
		super("§a"+section.getType().getDisplayName()+" §6» §aMember §6» §aGroup");
		this.section = section;
		this.player = player;
		groups = section.getPermission().getGroups().toArray(new String[0]);
		String own = section.getPermission().getGroup(player).getName();
		int index = 0;
		for(String s : groups){
			addSelectable(ItemBuilder.create().id(section.getPermission().getGroup(s).getItemId()).name("§a"+s).build());
			if(s.equalsIgnoreCase(own))
				setActive(index);
			index++;
		}
	}

	@Override
	public void select(int select) {
		if(groups[select].equalsIgnoreCase("owner")){
			new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE).name("§cDu kannst den Gildenbesitzer nicht ändern").build()) {
				@Override
				public void onContinue() {
					new GuiGildeSelectGoup(section, player).setPlayer(getPlayer()).openGui();
				}
			};
			return;
		}
		if(section.getPermission().getGroup(player).getName().equalsIgnoreCase("owner")){
			new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE).name("§cDu kannst den Gildenowner nicht heruntersetzen").build()) {
				@Override
				public void onContinue() {
					new GuiGildeSelectGoup(section, player).setPlayer(getPlayer()).openGui();
				}
			};
			return;
		}
		section.getPermission().setGroup(player, section.getPermission().getGroup(groups[select]));
		getPlayer().sendMessage("§aDu hast die Gruppe von §e"+player.getName()+"§a auf §e"+groups[select]+"§a in der Section §e"+section.getType().getDisplayName()+"§a gesetzt.");
		getPlayer().closeInventory();
	}

	@Override
	public void cancel() {
		switchToGui(new GuiGildeMemberAdminPannel(section, player));
	}
}
