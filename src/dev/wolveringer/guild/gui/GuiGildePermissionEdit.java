package dev.wolveringer.guild.gui;

import dev.wolveringer.gilde.GildPermissionGroup;
import dev.wolveringer.gilde.Gilde;
import dev.wolveringer.gui.Gui;

public class GuiGildePermissionEdit extends Gui{

	public GuiGildePermissionEdit(GildPermissionGroup group) {
		super(4, "§a"+group.getHandle().getHandle().getType().getDisplayName()+" §6» §aGroups");
	}

	@Override
	public void build() {
		// TODO Auto-generated method stub
		
	}

}
