package dev.wolveringer.skin;

import java.util.concurrent.TimeUnit;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.hashmaps.CachedHashMap;

public class SkinCacheManager {
	private CachedHashMap<String, Skin> skinCache = new CachedHashMap<>(2, TimeUnit.MINUTES);
	
	public SkinCacheManager() {}
	
	public Skin getIfLoaded(String player){
		return skinCache.getOrDefault(player, null);
	}
	
	public Skin getOrLoad(String player){
		Skin s = getIfLoaded(player);
		if(s == null){
			if(player!=null && Main.getDatenServer().isActive()){
				LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(player);
				skinCache.put(player, s = lp.getOwnSkin().getSync());
			}else{
				if(player==null)new NullPointerException("The Player is null!").printStackTrace();
				s = new SteveSkin();
			}
		}
		return s;
	}
}
