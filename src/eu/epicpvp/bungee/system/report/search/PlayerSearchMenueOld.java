package eu.epicpvp.bungee.system.report.search;

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
import dev.wolveringer.api.gui.AnvilGui;
import dev.wolveringer.api.gui.AnvilGuiListener;
import dev.wolveringer.api.inventory.Inventory;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.datenserver.definitions.skin.Skin;
import eu.epicpvp.datenserver.definitions.skin.SteveSkin;
import eu.epicpvp.thread.ThreadFactory;

public abstract class PlayerSearchMenueOld implements AnvilGuiListener, PacketHandler<PacketPlayInWindowClick> {
	private AnvilGui gui;
	private Player player;
	private int maxPlayerEachSide = 27;
	private Item[] playerInventorySave;

	private List<String> selection = new ArrayList<>();
	private int side;

	private boolean active = false;

	public PlayerSearchMenueOld(Player player) {
		this.player = player;
		this.gui = new AnvilGui(player);
		this.gui.addListener(this);
	}

	public void open() {
		active = true;
		PacketLib.addHandler(this);
		this.playerInventorySave = player.getPlayerInventory().getContains();
		player.getPlayerInventory().reset();
		gui.open();
		gui.setBackgroundMaterial(Material.PAPER);
		gui.setBackgroundMessage("Enter name:");
		updateInv();
		redrawInventory();
	}

	private void setHotbarItemstack(Item item, int slot) {
		slot += 36; //Slot 0 starts at 36
		player.getPlayerInventory().setItem(slot, item);
	}

	private void setHotbarItemstack(ItemStack item, int slot) {
		setHotbarItemstack((Item) item, slot);
	}

	private Item getHotbarItem(int slot){
		return player.getPlayerInventory().getItem(slot+36);
	}

	private void setInventoryItemstack(Item item, int slot) {
		slot += 9;
		player.getPlayerInventory().setItem(slot, item);
	}

	private void setInventoryItemstack(ItemStack item, int slot) {
		setInventoryItemstack((Item) item, slot);
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
		updateSelection(name);
		callDeconstructor();
		canceled();
	}

	private void updateSelection(String name){
		selection.clear();
		if(Main.getDatenServer().getPlayers() != null)
			for(String s : Main.getDatenServer().getPlayers()){
				if(s != null)
					if(s.toLowerCase().startsWith(name.toLowerCase()))
						selection.add(s);
			}
	}

	@Override
	public void onMessageChange(AnvilGui g, String name) {
		if(!active)
			return;
		if(name == null)
			return;
		updateSelection(name);
		redrawInventory();
		updateInv();
	}

	@Override
	public void handle(PacketHandleEvent<PacketPlayInWindowClick> e) {
		if(player==null)throw new NullPointerException("player == NULL");

		if (!(e.getPacket() instanceof PacketPlayInWindowClick) || !e.getPlayer().equals(player))
			return;
		if (e.getPacket().getWindow() == Inventory.ID && e.getPacket().getSlot()>=0) { //Player inventory
			int slot = e.getPacket().getSlot() - 3 + 9; //-3 -> Anvil | +9 -> Player inv + armor
			if (player.getPlayerInventory().getItem(slot) instanceof ItemStack) {
				((ItemStack) player.getPlayerInventory().getItem(slot)).click(new ItemStack.Click(e.getPlayer(), slot, null, e.getPacket().getItem(), e.getPacket().getMode(),true));
			}
			player.sendPacket(new PacketPlayOutSetSlot(null, -1, 0));
			e.setCancelled(true);
			e.getPlayer().updateInventory();
		}
	}

	private void updateInv() {
		gui.setCenterItem(ItemBuilder.create(Material.GOLDEN_APPLE).durability(1).name("§eEnter a playername").build());
		gui.setColorPrefix("§a");
		gui.setOutputItem(ItemBuilder.create(Material.LAVA_BUCKET).name("§cCancel").lore("§aClick to cancel").glow().build());
	}

	private void redrawInventory() {
		player.getPlayerInventory().clear();
		redrawPlayers();
		if(selection.size()>maxPlayerEachSide*(side+1)){
			setHotbarItemstack(new ItemStack(ItemBuilder.create(Material.ARROW).name("§aView page "+(side+1)).glow().build()){
				@Override
				public void click(Click c) {
					side++;
					redrawInventory();
				}
			}, 8);
		}
		if(side>0){
			setHotbarItemstack(new ItemStack(ItemBuilder.create(Material.ARROW).name("§aView page "+(side-1)).glow().build()){
				@Override
				public void click(Click c) {
					side--;
					redrawInventory();
				}
			}, 0);
		}
		setHotbarItemstack(ItemBuilder.create(Material.WATCH).name("§bPlayer matches: "+selection.size()).lore("§aPage: "+side).glow().build(), 4);

		for (int i = 0; i < 9; i++)
			if(getHotbarItem(i) == null)
				setHotbarItemstack(ItemBuilder.create(Material.BARRIER).name("§a").build(), i);

		player.updateInventory();
	}

	private void redrawPlayers() {
		if (selection.size() == 0) {
			setInventoryItemstack(ItemBuilder.create(Material.NETHER_STAR).name("§c").build(), 13);
		} else {
			drawPlayers(new ArrayList<>(selection.subList(maxPlayerEachSide*side, Math.min(selection.size(), maxPlayerEachSide*(side+1)))));
		}
	}

	private void drawPlayers(List<String> players){
		for(int i = 0;i<Math.min(players.size(), 27);i++){
			final String name = players.get(i);
			ItemStack is = new ItemStack(ItemBuilder.create(Material.SKULL_ITEM).durability(1).name("§e"+name).build()){
				@Override
				public void click(Click c) {
					playerEntered(name);
					callDeconstructor();
				}
			};
			setInventoryItemstack(loadSkin(is, name, i), i);
		}
	}

	private Item loadSkin(ItemStack is,String name,int slot){
		if(player == null)
			return is;
		Skin skin = Main.getSkinManager().getIfLoaded(name);
		if(skin != null){
			is.setDurability((short) 3);
			((SkullMeta)is.getItemMeta()).setSkin(toBungeeUtilSkin(skin));
			return is;
		}
		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				Skin skin = Main.getSkinManager().getOrLoad(name);
				is.setDurability((short) 3);
				((SkullMeta)is.getItemMeta()).setSkin(toBungeeUtilSkin(skin));
				Item old = PlayerSearchMenueOld.this.player.getPlayerInventory().getItem(slot+9);
				if(old != null){
					setInventoryItemstack(is, slot);
				}
			}
		}).start();
		return is;
	}

	private dev.wolveringer.BungeeUtil.gameprofile.Skin toBungeeUtilSkin(Skin skin){
		dev.wolveringer.BungeeUtil.gameprofile.Skin _new = SkinFactory.createEmptySkin();
		if(!(skin instanceof SteveSkin)){
			_new = SkinFactory.createSkin(skin.getRawData(), skin.getSignature());
		}
		return _new;
	}

	private void callDeconstructor() {
		active = false;
		gui.close();
		player.getPlayerInventory().reset();
		for (int i = 0; i < playerInventorySave.length; i++)
			player.getPlayerInventory().setItem(i, playerInventorySave[i]);
		player.updateInventory();
		PacketLib.removeHandler(this);
	}

	public abstract void playerEntered(String name);
	public abstract void canceled();
}
