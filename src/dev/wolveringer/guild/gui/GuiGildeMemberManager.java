package dev.wolveringer.guild.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInWindowClick;
import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.gilde.GildPermissionGroup;
import dev.wolveringer.gilde.GildSectionPermission;
import dev.wolveringer.gilde.GildePermissions;
import dev.wolveringer.gui.Gui;
import dev.wolveringer.guild.gui.section.GuiGildeSection;
import dev.wolveringer.guild.gui.section.SectionRegestry;
import dev.wolveringer.item.ItemBuilder;

public class GuiGildeMemberManager extends Gui{
	private static final List<String> FINAL_GROUPS = Arrays.asList("owner");
	private static final Item EMPTY_GROUP = ItemBuilder.create(Material.IRON_FENCE).name("§7").build();
	private static final Item ARROW_UP_DOWN = ItemBuilder.create(Material.ARROW).name("§7» §6Hock/Runter").lore("§c").lore("§eLinksklick§7: §a︽").lore("§eRechtsklick§7: §a︾").build();
	private static final Item ARROW_LEFT_RIGHT = ItemBuilder.create(Material.ARROW).name("§7» §6Hock/Runter").lore("§c").lore("§eLinksklick§7: §a«").lore("§eRechtsklick§7: §a»").build();
	private static final int GROUPS_PER_COLUMN = 4;
	private static final int MEMBER_PER_ROW = 6;
	
	private GildSectionPermission permission;
	
	private Item[][] memberItems;
	private Item[] groupItems;
	private Item[] memberArrowItems;
	private Item groupArrowItem;
	
	private int[] memberIndex;
	private int groupIndex;
	
	public GuiGildeMemberManager(GildSectionPermission permission) {
		super(6, "§a"+permission.getHandle().getType().getDisplayName()+" §6» §aPermissions");
		this.permission = permission;
	}



	@Override
	public void build() {
		buildItems();
		buildArrows();
		buildGroupColumn();
		buildMemberRow();
		
		inv.setItem(46, groupArrowItem);
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cZurück").listener((Click c) -> switchToGui(SectionRegestry.getInstance().createGildeSection(permission.getHandle().getType(), permission.getHandle()))).build());
		fill(ItemBuilder.create(160).durbility(7).name("§7").build());
	}

	private void buildItems(){
		LoadedPlayer handlePlayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
		
		int index = 0;
		ArrayList<String> groups = permission.getGroups();
		memberItems = new Item[groups.size()][];
		groupItems = new Item[groups.size()];
		memberArrowItems = new Item[groups.size()];
		
		for(String s : groups){
			GildPermissionGroup group = permission.getGroup(s);
			ItemBuilder groupItemBuilder = ItemBuilder.create(group.getItemId());
			groupItemBuilder.name("§7» §6Clan Mitglieder");
			groupItemBuilder.lore("§c").lore("§eRang§7: §a"+group.getName());
			groupItemBuilder.lore("§c").lore("§eRechte:");
			for(String r : group.getPermissions())
				groupItemBuilder.lore("§7- §a"+GildPermissionMapping.getDisplayItem(r));
			groupItems[index] = groupItemBuilder.build();
			
			ArrayList<LoadedPlayer> players = group.getPlayers();
			Item[] playerItems = new Item[players.size()];
			int memberLoopIndex = 0;
			for(LoadedPlayer player : players){
				ItemBuilder memberItemBuilder = ItemBuilder.create(Material.SKULL_ITEM).name("§6"+player.getName());
				if(FINAL_GROUPS.contains(group.getName()))
					memberItemBuilder.glow();
				if(permission.hasPermission(handlePlayer,GildePermissions.MEMBER_EDIT)){
					memberItemBuilder.lore("§aKlicke um ins Member-Menue zu kommen.");
					memberItemBuilder.listener((c)->{
						switchToGui(new GuiGildeMemberAdminPannel(permission.getHandle(), player));
					});
				}
				playerItems[memberLoopIndex] = loadSkin(memberItemBuilder.build(), player.getName());
				memberLoopIndex++;
			}
			memberItems[index] = playerItems;
			
			final int finalGroupIndex = index;
			memberArrowItems[index] = ItemBuilder.create(ARROW_LEFT_RIGHT).listener((c)->{
				if(c.getMode() == PacketPlayInWindowClick.Mode.NORMAL_LEFT_CLICK){
					if(memberIndex[finalGroupIndex] > 0)
						memberIndex[finalGroupIndex]--;
					buildMemberRow();
				}
				else if(c.getMode() == PacketPlayInWindowClick.Mode.NORMAL_RIGHT_CLICK){
					if(memberIndex[finalGroupIndex]+MEMBER_PER_ROW+1 < memberItems[finalGroupIndex].length)
						memberIndex[finalGroupIndex]++;
					buildMemberRow();
				}
			}).build();
			
			index++;
		}
		
		groupArrowItem = ItemBuilder.create(ARROW_UP_DOWN).listener((c) -> {
			if(c.getMode() == PacketPlayInWindowClick.Mode.NORMAL_LEFT_CLICK){
				if(groupIndex > 0)
					groupIndex--;
				buildArrows();
				buildGroupColumn();
				buildMemberRow();
			}
			else if(c.getMode() == PacketPlayInWindowClick.Mode.NORMAL_RIGHT_CLICK){
				if(groupIndex+GROUPS_PER_COLUMN+1 < groupItems.length)
					groupIndex++;
				buildArrows();
				buildGroupColumn();
				buildMemberRow();
			}
		}).build();
		
		groupIndex = 0;
		memberIndex = new int[GROUPS_PER_COLUMN];
		Arrays.fill(memberIndex, 0);
	}
	
	private void buildArrows(){
		Item[] items = new Item[GROUPS_PER_COLUMN];
		Arrays.fill(items, EMPTY_GROUP);
		System.arraycopy(memberArrowItems, groupIndex, items, 0, Math.min(groupItems.length-groupIndex, GROUPS_PER_COLUMN));
		
		for(int i = 0;i<GROUPS_PER_COLUMN;i++){
			inv.setItem(i*9+17, items[i]);
		}
	}
	
	private void buildMemberRow(){
		Item[][] items = new Item[GROUPS_PER_COLUMN][];
		Arrays.fill(items, new Item[0]);
		System.arraycopy(memberItems, groupIndex, items, 0, Math.min(groupItems.length-groupIndex, groupIndex+GROUPS_PER_COLUMN));
		
		for(int i = 0;i<items.length;i++){
			Item[] members = items[i];
			Item[] drawItems = new Item[MEMBER_PER_ROW];
			if(memberItems.length > i && memberItems[i] != null && memberIndex.length > i)
				System.arraycopy(members, memberIndex[i], drawItems, 0, Math.min(memberItems[i].length-memberIndex[i], MEMBER_PER_ROW));
			for(int j = 0;j < drawItems.length; j++){
				inv.setItem(i*9+11+j, drawItems[j]);
			}
		}
	}
	
	private void buildGroupColumn(){
		Item[] items = new Item[GROUPS_PER_COLUMN];
		Arrays.fill(items, EMPTY_GROUP);
		System.arraycopy(groupItems, groupIndex, items, 0, Math.min(groupItems.length-groupIndex, GROUPS_PER_COLUMN));
		
		for(int i = 0;i<GROUPS_PER_COLUMN;i++){
			inv.setItem(i*9+10, items[i]);
		}
	}
	
}
