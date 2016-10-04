package eu.epicpvp.bungee.system.guild.gui;

import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.api.gui.AnvilGui;
import dev.wolveringer.api.gui.AnvilGuiListener;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.datenclient.gilde.GildPermissionGroup;
import eu.epicpvp.datenclient.gilde.GildSectionPermission;
import eu.epicpvp.datenserver.definitions.gilde.GildePermissions;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.gui.GuiStatusPrint;
import eu.epicpvp.bungee.system.gui.GuiWaiting;
import eu.epicpvp.bungee.system.guild.gui.section.SectionRegestry;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import dev.wolveringer.thread.ThreadFactory;

public class GuiGildePermissionGroupOverview extends Gui{
	private static final Item ARROW_LEFT = ItemBuilder.create(Material.ARROW).name("§7» §6Nach links").build();
	private static final Item ARROW_RIGHT = ItemBuilder.create(Material.ARROW).name("§7» §6Nach rechts").build();

	GildSectionPermission permission;
	public GuiGildePermissionGroupOverview(GildSectionPermission perms) {
		super(3, "§a"+ perms.getHandle().getType().getDisplayName()+" §6» §aGroups");
		this.permission =  perms;
	}

	private Item[] groups;
	private int pos = 0;

	@Override
	public void build() {
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(SectionRegestry.getInstance().createGildeSection(permission.getHandle().getType(), permission.getHandle()))).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());

		inv.setItem(8, ItemBuilder.create(Material.NETHER_STAR).name("§cErstelle eine Gruppe").listener((click)->{
			if(!permission.hasPermission(Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName()), GildePermissions.GROUP_EDIT_PERMISSIONS)){
				new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDu hast keine Bereichtigung neue gruppen zu erstellen").build()) {
					@Override
					public void onContinue() {
						GuiGildePermissionGroupOverview.this.setPlayer(getPlayer()).openGui(); //Its an try
					}
				}.setPlayer(getPlayer()).openGui();
				return;
			}

			AnvilGui gui = new AnvilGui(getPlayer());
			gui.addListener(new AnvilGuiListener() {
				@Override
				public void onMessageChange(AnvilGui guy, String newMessage) {
			    	//Update output item ;)
					Item item = new Item(Material.ENCHANTED_BOOK);
			    	item.getItemMeta().setDisplayName("§aGroupname: §e" + (newMessage.length() == 0 ? "§cNo name" : newMessage));
			    	guy.setOutputItem(item);
				}

				@Override
				public void onConfirmInput(AnvilGui guy, String message) {
					GuiWaiting waiting = new GuiWaiting("§aCreating group", "§aPlease wait");
					waiting.setPlayer(getPlayer()).openGui();
					ThreadFactory.getFactory().createThread(()->{
						waiting.waitForMinwait(1500);
						if(message.length() < 3){
							new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cDie Gruppe existiert bereits").build()) {
								@Override
								public void onContinue() {
									new GuiGildePermissionGroupOverview(permission).setPlayer(getPlayer()).openGui();
								}
							}.setPlayer(getPlayer()).openGui();
							return;
						}
						permission.createGroup(message);
						long start = System.currentTimeMillis();
						while (permission.getGroup(message) == null || System.currentTimeMillis()-start > 10000) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if(System.currentTimeMillis()-start > 10000){
							new GuiStatusPrint(6,ItemBuilder.create(Material.REDSTONE_BLOCK).name("§cCant create group (Group not created)").build()) {
								@Override
								public void onContinue() {
									new GuiGildePermissionGroupOverview(permission).setPlayer(getPlayer()).openGui();
								}
							}.setPlayer(getPlayer()).openGui();
							return;
						}
						new GuiStatusPrint(6,ItemBuilder.create(Material.EMERALD).name("§Gruppe erstellt.").build()) {
							@Override
							public void onContinue() {
								new GuiGildePermissionGroupOverview(permission).setPlayer(getPlayer()).openGui();
							}
						}.setPlayer(getPlayer()).openGui();
					}).start();
				}

				@Override
				public void onClose(AnvilGui guy) {
				}
			});
			gui.setColorPrefix("§a");
			gui.open();
			gui.setBackgroundMessage("Enter the groupname");
		}).build());

		buildGroups();
		drawGroup();
	}

	private void buildGroups(){
		List<String> groupsName = permission.getGroups();
		groups = new Item[groupsName.size()];
		int index = 0;
		for(String g : groupsName){
			GildPermissionGroup group = permission.getGroup(g);
			ItemBuilder builder = ItemBuilder.create(Math.max(group.getItemId(), 1));

			builder.name("§a"+group.getName()).lore("§aKlicke hier um die Gruppe zu editieren.");
			builder.listener((c)->{
				GuiGildePermissionGroupOverview.this.switchToGui(new GuiGildePermissionGroupAdminPannel(group));
			});
			groups[index++] = builder.build();
		}
	}

	private void drawGroup(){
		int baseSlot = 11;
		inv.setItem(10, ItemBuilder.create(ARROW_LEFT).listener((Click c)->{
			if(pos > 0){
				pos--;
				drawGroup();
			}
		}).glow(pos <= 0).build());
		inv.setItem(17, ItemBuilder.create(ARROW_RIGHT).listener((Click c)->{
			if(groups.length-pos-5 > 0){
				pos++;
				drawGroup();
			}
		}).glow(groups.length-pos-5 < 0).build());

		for(int i = 0;i<5;i++){
			if(i+pos >= groups.length)
				inv.setItem(baseSlot+i, null);
			else
				inv.setItem(baseSlot+i, groups[i+pos]);
		}
	}
}
