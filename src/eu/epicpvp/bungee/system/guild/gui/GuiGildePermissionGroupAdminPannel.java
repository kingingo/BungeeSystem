package eu.epicpvp.bungee.system.guild.gui;

import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.datenclient.gilde.GildPermissionGroup;
import eu.epicpvp.datenserver.definitions.gilde.GildePermissions;
import eu.epicpvp.datenserver.definitions.gilde.GildePermissions.DisplayItem;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.gui.GuiStatusPrint;
import eu.epicpvp.bungee.system.gui.GuiWaiting;
import eu.epicpvp.bungee.system.item.ItemBuilder;

public class GuiGildePermissionGroupAdminPannel extends Gui{
	private static class PermissionItem {
		private boolean locked;
		private Item item;
		private GildPermissionGroup handle;
		private GildePermissions perm;
		private GuiGildePermissionGroupAdminPannel inv;

		private static Item buildItem(DisplayItem item){
			return ItemBuilder.create(item.getId()).name(item.getName()).durbility(item.getDurbility()).lore(item.getLore()).build();
		}

		public PermissionItem(GuiGildePermissionGroupAdminPannel inv, GildPermissionGroup handle,GildePermissions perm) {
			item = buildItem(perm.getDisplayItem());
			this.handle = handle;
			this.perm = perm;
			this.inv = inv;
		}

		public Item buildItem(){
			return new ItemBuilder(item).glow(handle.getPermissions().contains(perm.getPermission())).listener((Click click)->{
				if(locked){
					click.getPlayer().sendMessage("§cDu kannst diese Permission nicht editieren.");
					return;
				}
				if(!handle.getHandle().hasPermission(Main.getDatenServer().getClient().getPlayerAndLoad(click.getPlayer().getName()), perm)){
					click.getPlayer().sendMessage("§cDu hast keine Berechtigungen um Permissions zu editieren.");
					return;
				}
				if(click.getItem().getItemMeta().hasGlow()){
					click.getItem().getItemMeta().setGlow(false);
					item.getItemMeta().setGlow(false);
					handle.removePermission(perm.getPermission());
				}
				else
				{
					click.getItem().getItemMeta().setGlow(true);
					item.getItemMeta().setGlow(true);
					handle.addPermission(perm.getPermission());
				}
				inv.inv.updateInventory();
			}).build();
		}
	}

	private static int[] slots = {11,29,  13,31,  15,33};
	private static int movePerClick = 2;

	private static final Item ARROW_LEFT = ItemBuilder.create(Material.ARROW).name("§7» §6Nach links").build();
	private static final Item ARROW_RIGHT = ItemBuilder.create(Material.ARROW).name("§7» §6Nach rechts").build();

	private GildPermissionGroup group;
	private PermissionItem[] items;
	private int pos = 0;

	public GuiGildePermissionGroupAdminPannel(GildPermissionGroup group) {
		super(5, "§a"+ group.getHandle().getHandle().getType().getDisplayName()+" §6» §aGroup §6» §a"+group.getName());
		this.group = group;
	}

	@Override
	public void build() {
		buildItem();

		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(new GuiGildePermissionGroupOverview(group.getHandle()))).build());
		if(!group.isDefaultGroup() || group.getName().equalsIgnoreCase("owner"))
			inv.setItem(8, ItemBuilder.create(Material.LAVA_BUCKET).name("§cGruppe löschen").listener(()->{
				GuiWaiting waiting = (GuiWaiting) new GuiWaiting("§a", "").setPlayer(getPlayer());
				waiting.openGui();

				group.delete();
				waiting.waitForMinwait(1500);
				new GuiStatusPrint(6,ItemBuilder.create(Material.EMERALD).name("§aGruppe erfolgreich gelöscht").build()) {
					@Override
					public void onContinue() {
						new GuiGildePermissionGroupOverview(group.getHandle()).setPlayer(getPlayer()).openGui();
					}
				};
			}).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
		print();
	}

	private void print(){
		inv.setItem(18, ItemBuilder.create(ARROW_LEFT).listener(()->{
			if(pos > 0){
				pos--;
				print();
			}
		}).glow(pos <= 0).build());
		inv.setItem(26, ItemBuilder.create(ARROW_RIGHT).listener(()->{
			if(items.length-slots.length-movePerClick*pos > 0){
				pos++;
				print();
			}
		}).glow(items.length-slots.length-movePerClick*pos <= 0).build());

		displayPermissions();
	}

	private void buildItem(){
		List<GildePermissions> permissions = GildePermissions.getAvariablePermission(group.getHandle().getHandle().getType());
		items = new PermissionItem[permissions.size()];
		for(int i = 0;i<items.length;i++){
			items[i] = new PermissionItem(this,group, permissions.get(i));
			if(group.getName().equalsIgnoreCase("owner")){
				items[i].locked = true;
				items[i].item.getItemMeta().setGlow(true);
			}
		}
	}

	public void displayPermissions(){
		for(int i = 0;i<slots.length;i++){
			if(i+pos*movePerClick < items.length)
				inv.setItem(slots[i], items[i+pos*movePerClick].buildItem());
			else
				inv.setItem(slots[i], null);
		}
	}
}
