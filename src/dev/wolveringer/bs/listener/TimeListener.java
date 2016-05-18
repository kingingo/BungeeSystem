package dev.wolveringer.bs.listener;

import java.util.ArrayList;
import java.util.HashMap;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.EditStats;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TimeListener implements Listener{

	private HashMap<Integer,Counter> players = new HashMap<>();
	private final long MINUTE = 60000L;
	private static boolean active = false;
	
	public TimeListener(){
		init();
	}
	
	@EventHandler
	public void dis(PlayerDisconnectEvent ev){
		LoadedPlayer loadedplayer = Main.getDatenServer().getClient().getPlayerAndLoad(ev.getPlayer().getName());
		save(loadedplayer.getPlayerId());
		players.remove(loadedplayer.getPlayerId());
	}
	
	@EventHandler
	public void a(PostLoginEvent ev){
		LoadedPlayer loadedplayer = Main.getDatenServer().getClient().getPlayerAndLoad(ev.getPlayer().getName());
		players.put(loadedplayer.getPlayerId(), new Counter());
	}
	
	@EventHandler
	public void switchServer(ServerConnectEvent ev){
		int playerId = Main.getDatenServer().getClient().getPlayerAndLoad(ev.getPlayer().getName()).getPlayerId();
		
		if(this.players.containsKey(playerId)){
			StatsKey toKey = getType(ev.getTarget().getName());
			Counter c = this.players.get(playerId);
			
			if(c.key != toKey){
				save(playerId);
				c.timestamp=System.currentTimeMillis();
				c.key=toKey;
			}
		}
	}
	
	public void init(){
		active = true;
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				while(active){
					if(!players.isEmpty()){
						for(int playerId : new ArrayList<>(players.keySet())){
							if(Main.getDatenServer().getClient().getPlayer(playerId)!=null){
								save(playerId);
							}else{
								players.remove(playerId);
							}
						}
					}
					
					try {
						Thread.sleep(1000 * 60 * 30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public StatsKey getType(String server){
		if(server.equalsIgnoreCase("pvp")){
			return StatsKey.PVP_TIME;
		}else if(server.equalsIgnoreCase("sky")){
			return StatsKey.SKY_TIME;
		}else if(server.toLowerCase().contains("gungame")){
			return StatsKey.GUNGAME_TIME;
		}else{
			return StatsKey.GAME_TIME;
		}
	}
	
	public void save(int playerId){
		if(this.players.containsKey(playerId)){
			Counter c = this.players.get(playerId);

			if( (System.currentTimeMillis()-c.timestamp) > (MINUTE * 5) ){
				LoadedPlayer loadedplayer = Main.getDatenServer().getClient().getPlayerAndLoad(playerId);
				EditStats edit = new EditStats(GameType.TIME,Action.ADD,c.key, (System.currentTimeMillis()-c.timestamp));
				loadedplayer.setStats(edit);
			}
		}
	}
	
	private class Counter{
		private long timestamp;
		private StatsKey key;
		
		public Counter(){
			this.timestamp=System.currentTimeMillis();
			this.key=StatsKey.GAME_TIME;
		}
	}
}
