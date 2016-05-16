package dev.wolveringer.report.search;

import java.util.ArrayList;
import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.PacketLib;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.gameprofile.SkinFactory;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import dev.wolveringer.BungeeUtil.item.itemmeta.SkullMeta;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInWindowClick;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutSetSlot;
import dev.wolveringer.api.guy.AnvilGui;
import dev.wolveringer.api.guy.AnvilGuiListener;
import dev.wolveringer.api.inventory.Inventory;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.item.ItemBuilder;
import dev.wolveringer.bs.Main;
import dev.wolveringer.skin.Skin;
import dev.wolveringer.skin.SteveSkin;
import net.md_5.bungee.BungeeCord;

public abstract class PlayerTextEnterMenue implements AnvilGuiListener {
	private AnvilGui gui;

	private boolean active = false;
	
	public PlayerTextEnterMenue(Player player) {
		this.gui = new AnvilGui(player);
		this.gui.addListener(this);
	}

	public void open() {
		active = true;
		gui.open();
		gui.setBackgroundMaterial(Material.PAPER);
		gui.setBackgroundMessage("");
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
		gui.setCenterItem(ItemBuilder.create(Material.GOLDEN_APPLE).durbility(1).name("§eEnter your message").build());
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
