package eu.epicpvp.bungee.system.report.search;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.api.gui.AnvilGui;
import dev.wolveringer.api.gui.AnvilGuiListener;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import lombok.Getter;
import lombok.Setter;

public abstract class PlayerTextEnterMenue implements AnvilGuiListener {
	@Getter
	private AnvilGui gui;

	private boolean active = false;
	@Getter
	@Setter
	private String backGround = "";

	public PlayerTextEnterMenue(Player player) {
		this.gui = new AnvilGui(player);
		this.gui.addListener(this);
	}

	public void open() {
		active = true;
		gui.open();
		gui.setBackgroundMaterial(Material.PAPER);
		gui.setBackgroundMessage(backGround);
	}


	@Override
	public void onClose(AnvilGui g) {
		if(!active)
			return;
		callDeconstructor();
		canceled();
	}

	@Override
	public void onConfirmInput(AnvilGui g, String name) {
		if(!active)
			return;
		callDeconstructor();
		textEntered(name);
		canceled();
	}

	@Override
	public void onMessageChange(AnvilGui g, String name) {
		if(!active)
			return;
		if(name == null)
			return;
		updateInv();
	}

	private void updateInv() {
		gui.setCenterItem(ItemBuilder.create(Material.GOLDEN_APPLE).durability(1).name("§eEnter your message").build());
		gui.setColorPrefix("§a");
		gui.setOutputItem(ItemBuilder.create(Material.SLIME_BALL).name("§aConfirm").lore("§aClick to confirm").glow().build());
	}

	private void callDeconstructor() {
		active = false;
		gui.close();
	}

	public abstract void textEntered(String name);
	public abstract void canceled();
}
