package eu.epicpvp.bungee.system.guild.gui;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.gui.Gui;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.gilde.Gilde;
import net.md_5.bungee.BungeeCord;

public class GuiGildeDeleteConfirm extends Gui {

	private LoadedPlayer lplayer;
	private Gilde gilde;

	public GuiGildeDeleteConfirm(LoadedPlayer lplayer, Gilde gilde) {
		super(1, "§cLösche eine Gilde");
		this.lplayer = lplayer;
		this.gilde = gilde;
	}

	@Override
	public void build() {
		inv.setItem(0, new ItemStack(ItemBuilder.create(Material.BARRIER).name("§cSchließen").build()) {
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
			}
		});
		inv.setItem(4, ItemBuilder.create(Material.ANVIL).name("§c§lAchtung!").lore("§cDas löschen deiner Gilde kann nicht wieder").lore("§crückgäning gemacht werden.").build());

		inv.setItem(7, new ItemStack(ItemBuilder.create(159).name("§cAbbrechen").durability(14).build()) {
			@Override
			public void click(Click c) {
				getPlayer().closeInventory();
				getPlayer().sendMessage("§cAction canceled.");
			}
		});
		inv.setItem(8, ItemBuilder.create(159).name("§a§mBestätigen").lore("§cBitte warte noch 5 sekunden befor du").lore("§cBestätigen kannst.").durability(11).build());

		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 0, 9);
	}

	@Override
	public void active() {
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			public void run() {
				int loop = 6;
				while (loop-- > 0) {
					inv.setItem(8, ItemBuilder.create(159).name("§a§mBestätigen").lore("§cBitte warte noch " + loop + " sekunden bevor du").lore("§cBestätigen kannst.").durability(11).build());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				inv.setItem(8, new ItemStack(ItemBuilder.create(159).name("§aBestätigen").durability(13).build()) {
					@Override
					public void click(Click c) {
						getPlayer().closeInventory();
					}
				});
			}
		});
	}
}
