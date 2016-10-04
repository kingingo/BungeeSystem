package dev.wolveringer.teamspeak;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.listener.PlayerJoinListener;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketTeamspeakAction;
import dev.wolveringer.events.teamspeak.TeamspeakLinkRequestEvent;
import eu.epicpvp.bungee.system.gui.GuiUpdating;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.permission.Group;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import dev.wolveringer.thread.ThreadFactory;

public abstract class GuiTeamspeakListRequest extends GuiUpdating {
	private final int TIME = 60 * 1000;
	private TeamspeakLinkRequestEvent e;
	private long start = System.currentTimeMillis();

	public GuiTeamspeakListRequest(TeamspeakLinkRequestEvent e) {
		super(1, "§aLink request from " + e.getTeamspeakName());
		this.e = e;
	}

	@Override
	public void updateInventory() {
		if (start + TIME < System.currentTimeMillis()) {
			getPlayer().closeInventory();
			input(false);
			return;
		}
		inv.setItem(4, createInfoItem());
	}

	@Override
	public void build() {
		fill(ItemBuilder.create(160).durbility(7).name("§7").build(), 0, -1, true);
		inv.setItem(4, createInfoItem());
		inv.setItem(0, ItemBuilder.create(159).durbility(14).name("§cAblehnen").listener(() -> {
			input(false);
			getPlayer().closeInventory();
		}).build());
		inv.setItem(8, ItemBuilder.create(159).durbility(5).name("§aAnnehmen").listener(() -> {
			LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
			input(true);
			getPlayer().closeInventory();
			ThreadFactory.getFactory().createThread(()->{
				try {
					Thread.sleep(1500);
				} catch (Exception e) {
				}
				List<Group> groups = PermissionManager.getManager().getPlayer(lplayer.getPlayerId()).getGroups();
				Collections.sort(groups,new Comparator<Group>() {
					@Override
					public int compare(Group o1, Group o2) {
						return Integer.compare(o2.getImportance(), o1.getImportance());
					}
				});
				Main.getDatenServer().getClient().writePacket(new PacketTeamspeakAction(lplayer.getPlayerId(), dev.wolveringer.dataserver.protocoll.packets.PacketTeamspeakAction.Action.UPDATE_GROUPS, groups.get(0).getName()));
			}).start();
		}).build());
	}

	private Item createInfoItem() {
		ItemBuilder builder = ItemBuilder.create(Material.PAPER);
		builder.name("§aWillst du diesen Minecraft-Account");
		builder.lore("§amit dem Teamspeak Account §e" + e.getTeamspeakName() + " §averbinden?");
		builder.lore("§c");
		builder.lore("§aDiese Anfrage wurde von der IP §b" + e.getTeamspeakIp());
		builder.lore("§amit dem Betriebs-System §b" + e.getTeamspeakPlatform() + " §agesendet.");
		builder.lore("§aDiese Anfrage wird in §c" + PlayerJoinListener.getDurationBreakdown(start + TIME - System.currentTimeMillis(), "now") + " geschlossen.");
		builder.glow();
		return builder.build();
	}

	public abstract void input(boolean accept);
}
