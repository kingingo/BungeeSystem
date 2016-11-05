package eu.epicpvp.bungee.system.slotmachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.api.inventory.Inventory;
import eu.epicpvp.bungee.system.booster.GuiIntegerSelect;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.slotmachine.RoulettHistory.HistoryItem;
import eu.epicpvp.bungee.system.slotmachine.RoulettHistory.HistoryListener;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import eu.epicpvp.dataserver.protocoll.packets.PacketOutPacketStatus;
import eu.epicpvp.dataserver.protocoll.packets.PacketOutPacketStatus.Error;
import eu.epicpvp.datenclient.client.Callback;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.StatsKey;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import eu.epicpvp.thread.ThreadFactory;

public class RoulettGui implements HistoryListener {
	private static final String BANK_NAME = "WolverinDEV";
	private static final int WHEEL_LENGTH = 12;

	private boolean active = false;

	private Player player;
	private Inventory inv;
	private boolean balanceRebuilding = false;
	private int balance = -1;
	private int bankBalance = -1;

	private Item[] wheelItems;
	private int wheelIndex = 0;

	private boolean wheelActive;

	private int[] puts = new int[3];

	public RoulettGui() {
		wheelItems = new Item[WHEEL_LENGTH + 1];
		for (int i = 0; i <= WHEEL_LENGTH; i++)
			wheelItems[i] = buildWheelItem(i).build();
		wheelIndex = Math.abs((int) System.currentTimeMillis());
		inv = new Inventory(54, "§6Roulette");
		for(int i = 0;i<inv.getSlots();i++)
			inv.setItem(i, ItemBuilder.create(160).durability(7).name("§7").build());
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	private void buildWheelItem() {
		inv.disableUpdate();
		wheelIndex = wheelIndex % wheelItems.length;
		int baseSlot = 19;
		for (int i = 0; i < 7; i++) {
			Item item = wheelItems[(wheelIndex + i) % wheelItems.length];
			item.getItemMeta().setGlow(i == 3);
			inv.setItem(baseSlot + i, item);
		}
		inv.enableUpdate();
	}

	private ItemBuilder buildWheelItem(int index) {
		if (index == 0)
			return ItemBuilder.create().id(160).durability(13).name("§a" + index).amouth(index);
		if (index % 2 == 0)
			return ItemBuilder.create().id(160).durability(14).name("§c" + index).amouth(index);
		return ItemBuilder.create().id(160).durability(15).name("§8" + index).amouth(index);
	}

	public void open() {
		active = true;
		build();
		RoulettHistory.getHistory().addListener(this);
		player.openInventory(inv);
		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				while (active) {
					if (!player.isInventoryOpened())
						close();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	private void build() {
		rebuildHistory();
		rebuildCurruntBalance();
		buildSelectionItem();
		buildWheelItem();
		for (int i = 0; i < 3; i++)
			buildPutment(i);
	}

	private void buildSelectionItem() {
		if (wheelActive)
			inv.setItem(13, ItemBuilder.create().id(101).name("§eCurrent Selection.").lore("§cIf you close the inventory or disconnect you will lost your bet!").glow().build());
		else
			inv.setItem(13, ItemBuilder.create().id(101).name("§eCurrent Selection.").lore("§cIf you close your inventory or disconnect you will lost your bet!").lore("§aClick to rool wheel.").listener((c) -> {
				int all = 0;
				for (int i : puts)
					all += i;
				if (all == 0) {
					player.sendMessage("§aPlease provide a bet!");
					return;
				}
				roolWheel();
			}).build());
	}

	private int[] multiply =
	{ 12, 2, 2 };
	private String[] NAMES =
	{ "§aGreen §7(§bMultiplier " + multiply[0] + "x§7)", "§8Black §7(§bMultiplier " + multiply[1] + "x§7) ", "§cRed §7(§bMultiplier " + multiply[2] + "x§7)" };
	private int[] LOCATION =
	{ 46, 49, 52 };

	private void buildPutment(int index) {
		if (!wheelActive) {
			int putment = puts[index];
			ItemBuilder item = buildWheelItem(index).amouth(1).name(NAMES[index]);
			item.lore("§aCurrent deposit: " + putment);
			item.lore("§bClick to deposit");
			item.listener((c) -> {
				if(balance <= 0){
					player.sendMessage("§cYou dont have enough gems for roulett!");
					return;
				}
				if(bankBalance <= 0){
					player.sendMessage("§aThe bank is out of money!");
					return;
				}
				GuiIntegerSelect select = new GuiIntegerSelect(player, "§aSelect your deposit.", Math.min(100, balance)) {
					@Override
					public void numberEntered(int number) {
						Main.getDatenServer().getClient().getPlayerAndLoad(BANK_NAME).changeGems(Action.ADD, number);
						Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()).changeGems(Action.REMOVE, number).getAsync(new Callback<PacketOutPacketStatus.Error[]>() {
							@Override
							public void call(PacketOutPacketStatus.Error[] obj, Throwable exception) {
								Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "money", new DataBuffer().writeInt(StatsKey.GEMS.ordinal()).writeInt(Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()).getPlayerId()));
								rebuildCurruntBalance();
							}
						});
						puts[index] = puts[index] + number;
						buildPutment(index);
						player.sendMessage("§aGems put.");
						player.openInventory(inv);
					}

					@Override
					public boolean isNumberAllowed(int number) {
						return balance >= number && number >= puts[index] && number*multiply[index] < bankBalance;
					}
				};
				select.setMode(2);
				select.open();
			});
			inv.setItem(LOCATION[index], item.build());
		} else {
			int putment = puts[index];
			ItemBuilder item = buildWheelItem(index).amouth(1).name(NAMES[index]);
			item.glow().lore("§aCurrent deposit: " + putment);
			inv.setItem(LOCATION[index], item.build());
		}
	}

	private void rebuildCurruntBalance() {
		if (balanceRebuilding)
			return;
		balanceRebuilding = true;
		balance = -1;
		inv.setItem(8, ItemBuilder.create().id(339).name("§aYour currunt balance:").lore("§6Loading").build());
		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (Main.getDatenServer().isActive()) {
						balance = Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()).getGemsSync();
						bankBalance = Main.getDatenServer().getClient().getPlayerAndLoad(BANK_NAME).getGemsSync();
						inv.setItem(8, ItemBuilder.create().id(339).name("§aYour currunt balance:").lore("§b" + balance + " Gems").build());
						break;
					}
				}
				balanceRebuilding = false;
			}
		}).start();
	}

	private void rebuildHistory() {
		ItemBuilder builder = ItemBuilder.create();
		builder.id(321);
		builder.name("§c");
		ArrayList<HistoryItem> out = new ArrayList<>(RoulettHistory.getHistory().getList());
		Collections.reverse(out);
		for (HistoryItem item : out) {
			builder.lore("§e" + item.getPlayer() + " §7put §b" + item.getPut() + " Gems §7on " + getBetType(item.getBetOn()) + " §7and " + (item.getBalance() > 0 ? "§awon " + item.getBalance() + " Gems" : "§cloose " + (item.getBalance() * -1) + " Gems") + "§7.");
		}
		inv.setItem(0, builder.build());
	}

	public void roolWheel() {
		wheelActive = true;
		buildSelectionItem();
		for (int i = 0; i < 3; i++)
			buildPutment(i);

		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				Random rnd = new Random();
				run(30, 20 + rnd.nextInt(20));
				run(100, 10 + rnd.nextInt(10));
				run(200, 5 + rnd.nextInt(5));
				run(400, 1 + rnd.nextInt(3));

				if (!active)
					return;

				int looseIndex = 0;
				int currentCount = 100000;
				for(int i = 0;i<puts.length;i++)
					if(puts[i] < currentCount && i != 0){
						looseIndex = i;
						currentCount = puts[i];
					}

				int winIndex = calculateWinIndex();

				/*
				if(puts[winIndex] != 0 && rnd.nextDouble() <= 0.5D){
					System.out.println("Skipping whin for player "+player.getName());
					while (winIndex != looseIndex && puts[winIndex] != 0) {
						moveWheel();
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
						}
						winIndex = calculateWinIndex();
					}
				}
				*/
				int all = 0;
				int index = 0;
				for (int i : puts) {
					if (i != 0)
						RoulettHistory.getHistory().add(Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()).getFinalName(), index, i, i * (index == winIndex ? multiply[winIndex] : -1));
					all += i;
					index++;
				}
				if (puts[winIndex] == 0) {
					player.sendMessage("§cYou lost " + all + " Gems!");
				} else {
					player.sendMessage("§aYou won " + (puts[winIndex] * multiply[winIndex]) + " Gems!");
					Main.getDatenServer().getClient().getPlayerAndLoad(BANK_NAME).changeGems(Action.REMOVE, puts[winIndex] * multiply[winIndex]);
					Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()).changeGems(Action.ADD, puts[winIndex] * multiply[winIndex]).getAsync(new Callback<PacketOutPacketStatus.Error[]>() {
						@Override
						public void call(Error[] obj, Throwable exception) {
							Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "money", new DataBuffer().writeInt(StatsKey.GEMS.ordinal()).writeInt(Main.getDatenServer().getClient().getPlayerAndLoad(player.getName()).getPlayerId()));
							rebuildCurruntBalance();
						}
					});
				}

				puts = new int[3];
				wheelActive = false;
				buildSelectionItem();
				for (int i = 0; i < 3; i++)
					buildPutment(i);
			}

			private int calculateWinIndex(){
				int winIndex = -1;
				if (((wheelIndex+3)%wheelItems.length) == 0)
					winIndex = 0;
				else if (((wheelIndex+3)%wheelItems.length) % 2 == 0)
					winIndex = 2;
				else
					winIndex = 1;
				return winIndex;
			}

			private void moveWheel(){
				wheelIndex++;
				buildWheelItem();
			}

			private void run(int speed, int loops) {
				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < loops; i++) {
					moveWheel();
					if (!player.isInventoryOpened()) {
						close();
					}
					try {
						Thread.sleep(speed);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private String getBetType(int type) {
		switch (type) {
		case 0:
			return "§aGreen";
		case 1:
			return "§7Black";
		case 2:
			return "§cRed";
		default:
			break;
		}
		return "§mUndefined";
	}

	public void close() {
		active = false;
		RoulettHistory.getHistory().removeListener(this);
		player.closeInventory();
	}

	@Override
	public void update() {
		rebuildHistory();
	}

}
