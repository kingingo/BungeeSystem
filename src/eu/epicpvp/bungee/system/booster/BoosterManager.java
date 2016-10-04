package eu.epicpvp.bungee.system.booster;

import java.util.HashMap;

import dev.wolveringer.booster.BoosterType;
import dev.wolveringer.booster.NetworkBooster;
import dev.wolveringer.event.EventListener;
import dev.wolveringer.events.Event;
import dev.wolveringer.events.booster.BoosterStatusChangeEvent;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.thread.ThreadFactory;
import eu.epicpvp.bungee.system.actionbar.ActionBar;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.listener.PlayerJoinListener;
import net.md_5.bungee.BungeeCord;

public class BoosterManager implements EventListener {
	@SuppressWarnings("serial")
	private HashMap<BoosterType, NetworkBooster> booster = new InitHashMap<BoosterType, NetworkBooster>() {
		public NetworkBooster defaultValue(BoosterType key) {
			return new NetworkBooster.NotActiveBooster(key);
		};
	};

	public void init() {
		for (BoosterType t : BoosterType.values())
			if (t != BoosterType.NONE)
				loadBooster(t, false);
		ActionBar.getInstance().addMessage(new BoosterActionBarMessage(this, BoosterType.ARCADE));
		ActionBar.getInstance().addMessage(new BoosterActionBarMessage(this, BoosterType.SKY));
	}

	private void loadBooster(BoosterType type, boolean message) {
		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (Main.getDatenServer().isActive()) {
						try {
							NetworkBooster b = Main.getDatenServer().getClient().getNetworkBooster(type).getSync();
							if (b == null)
								throw new NullPointerException("Booster is null!");
							booster.put(type, b);
							if (message && b.isActive())
								BungeeCord.getInstance().broadcast(Main.getTranslationManager().translate("prefix") + "§aDer " + b.getType().getDisplayName() + "-Booster wurde von §e" + Main.getDatenServer().getClient().getPlayerAndLoad(b.getPlayer()).getName() + " §a für §b" + PlayerJoinListener.getDurationBreakdown((b.getStart() + b.getTime()) - System.currentTimeMillis()) + " §aaktiviert!");
							break;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}).start();
	}

	@Override
	public void fireEvent(Event e) {
		if (e instanceof BoosterStatusChangeEvent) {
			System.out.println("Booster changed (" + ((BoosterStatusChangeEvent) e).getBoosterType() + ")");
			if (((BoosterStatusChangeEvent) e).isActive()) {
				loadBooster(((BoosterStatusChangeEvent) e).getBoosterType(), true);
			} else {
				booster.put(((BoosterStatusChangeEvent) e).getBoosterType(), new NetworkBooster.NotActiveBooster(((BoosterStatusChangeEvent) e).getBoosterType()));
				BungeeCord.getInstance().broadcast(Main.getTranslationManager().translate("prefix") + "§aDer " + ((BoosterStatusChangeEvent) e).getBoosterType().getDisplayName() + "-Booster ist ausgelaufen. §e/booster §aum deinen Booster zu aktivieren.");

			}
		}
	}

	public void reloadBooster(BoosterType type) {
		booster.remove(type);
		loadBooster(type, false);
	}

	public NetworkBooster getBooster(BoosterType type) {
		return booster.get(type);
	}

	private static class BoosterActionBarMessage extends ActionBar.ActionBarMessage {
		private BoosterType type;
		private BoosterManager manager;

		public BoosterActionBarMessage(BoosterManager manager, BoosterType type) {
			super("booster", "§cLoading booster", 80, null);
			this.type = type;
			this.manager = manager;
		}

		public String getMessage() {
			if (this.manager.getBooster(this.type).isActive()) {
				return "§a§l" + this.type.getDisplayName() + "-Booster wurde aktiviert von §e§l" + Main.getDatenServer().getClient().getPlayerAndLoad(Main.getBoosterManager().getBooster(this.type).getPlayer()).getName();
			}
			return null;
		}
	}
}
// booster.actionbar.arcade - §aDouble-Coin Booster wurde aktiviert von §e%s0
// booster.actionbar.sky - §aFarm Booster wurde aktiviert von §e%s0
// http://hastebin.com/onezemopoq.avrasm
