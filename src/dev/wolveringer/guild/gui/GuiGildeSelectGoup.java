package dev.wolveringer.guild.gui;

import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gilde.GildSection;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildeSelectGoup extends GuiItemSelect{
	private GildSection section;
	private LoadedPlayer player;
	
	private String[] groups;
	
	public GuiGildeSelectGoup(GildSection section, LoadedPlayer player) {
		super("§a"+section.getType().getDisplayName()+" §6» §aMember §6» §aGroup");
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
		section.getPermission().setGroup(player, section.getPermission().getGroup(groups[select]));
		getPlayer().sendMessage("§aDu hast die Gruppe von §e"+player.getName()+" auf "+groups[select]+" in der Section "+section.getType().getDisplayName()+" gesetzt.");
		getPlayer().closeInventory();
	}

	@Override
	public void cancel() {}
}
