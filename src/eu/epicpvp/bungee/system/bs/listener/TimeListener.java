package eu.epicpvp.bungee.system.bs.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit.EditStats;
import eu.epicpvp.datenclient.client.Callback;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.client.ProgressFuture;
import eu.epicpvp.datenclient.client.futures.BaseProgressFuture;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.StatsKey;
import eu.epicpvp.datenserver.definitions.gamestats.Statistic;
import eu.epicpvp.datenserver.definitions.hashmaps.CachedHashMap;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TimeListener implements Listener {
	private static class ValueProgressFuture<T> extends BaseProgressFuture<T> {
		public void applayValue(T val){
			done(val);
		}
	}
	@Getter
	@Setter
	private static TimeListener instance;
	private HashMap<Integer, Counter> players = new HashMap<>();
	private final long MINUTE = 60000L;
	private static boolean active = false;
	private CachedHashMap<Player, Long> alltimings = new CachedHashMap<>(2, TimeUnit.MINUTES);

	public TimeListener() {
		init();
	}

	@EventHandler
	public void dis(PlayerDisconnectEvent ev) {
		LoadedPlayer loadedplayer = Main.getDatenServer().getClient().getPlayerAndLoad(ev.getPlayer().getName());
		save(loadedplayer.getPlayerId());
		players.remove(loadedplayer.getPlayerId());
	}

	@EventHandler(priority=0)
	public void a(PostLoginEvent ev) {
		LoadedPlayer loadedplayer = Main.getDatenServer().getClient().getPlayerAndLoad(ev.getPlayer().getName());
		players.put(loadedplayer.getPlayerId(), new Counter());
	}

	@EventHandler
	public void switchServer(ServerConnectEvent ev) {
		int playerId = Main.getDatenServer().getClient().getPlayerAndLoad(ev.getPlayer().getName()).getPlayerId();

		if (this.players.containsKey(playerId)) {
			StatsKey toKey = getType(ev.getTarget().getName());
			Counter c = this.players.get(playerId);

			if (c.key != toKey) {
				save(playerId);
				c.timestamp = System.currentTimeMillis();
				c.key = toKey;
			}
		}
	}

	public ProgressFuture<Long> getTime(Player player){
		LoadedPlayer loadedplayer = Main.getDatenServer().getClient().getPlayerAndLoad(player.getName());
		synchronized (alltimings) {
			alltimings.lock();
			if(alltimings.containsKey(player)){
				ValueProgressFuture<Long> out = new ValueProgressFuture<>();
				long playedTime = alltimings.get(player);
				if(players.containsKey(loadedplayer.getPlayerId()))
					playedTime += System.currentTimeMillis()-players.get(loadedplayer.getPlayerId()).timestamp;
				out.applayValue(playedTime);
				return out;
			}
			alltimings.unlock();
		}
		ValueProgressFuture<Long> future = new ValueProgressFuture<Long>();
		loadedplayer.getStats(GameType.TIME).getAsync(new Callback<Statistic[]>() {
			@Override
			public void call(Statistic[] obj, Throwable exception) {
				long playedTime = 0;
				for(Statistic c : obj)
					if(c.getStatsKey().name().endsWith("_TIME"))
						playedTime += c.asInt();
				if(players.containsKey(loadedplayer.getPlayerId()))
					playedTime+=System.currentTimeMillis()-players.get(loadedplayer.getPlayerId()).timestamp;
				future.applayValue(playedTime);

			}
		});
		return future;
	}

	public void init() {
		active = true;
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {

			@Override
			public void run() {
				while (active) {
					if (!players.isEmpty()) {
						for (int playerId : new ArrayList<>(players.keySet())) {
							if (Main.getDatenServer().getClient().getPlayer(playerId) != null) {
								save(playerId);
							} else {
								players.remove(playerId);
							}
						}
					}

					try {
						Thread.sleep(30 * 60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public StatsKey getType(String server) {
		if (server.equalsIgnoreCase("pvp")) {
			return StatsKey.PVP_TIME;
		} else if (server.equalsIgnoreCase("sky")) {
			return StatsKey.SKY_TIME;
		} else if (server.toLowerCase().contains("gungame")) {
			return StatsKey.GUNGAME_TIME;
		} else if (server.toLowerCase().contains("warz")) {
			return StatsKey.WARZ_TIME;
		} else if (server.toLowerCase().contains("creative")) {
			return StatsKey.CREATIVE_TIME;
		} else {
			return StatsKey.GAME_TIME;
		}
	}

	public void save(int playerId) {
		if (this.players.containsKey(playerId)) {
			Counter c = this.players.get(playerId);

			if ((System.currentTimeMillis() - c.timestamp) > (MINUTE * 3)) {
				System.out.println("[TimeListener] Save for the player " + playerId + " " + (System.currentTimeMillis() - c.timestamp) + " " + c.key.name());
				LoadedPlayer loadedplayer = Main.getDatenServer().getClient().getPlayerAndLoad(playerId);
				EditStats edit = new EditStats(GameType.TIME, Action.ADD, c.key, (int) (System.currentTimeMillis() - c.timestamp));
				loadedplayer.setStats(edit);
				c.timestamp = System.currentTimeMillis();
			}
		}
	}

	private class Counter {
		private long timestamp;
		private StatsKey key;

		public Counter() {
			this.timestamp = System.currentTimeMillis();
			this.key = StatsKey.GAME_TIME;
		}
	}
}
