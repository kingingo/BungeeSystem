package dev.wolveringer.skin;

import java.util.concurrent.TimeUnit;

import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.hashmaps.CachedHashMap;
import dev.wolveringer.bs.Main;
import dev.wolveringer.skin.Skin;

public class SkinCacheManager {
	private CachedHashMap<String, Skin> skinCache = new CachedHashMap<>(2, TimeUnit.MINUTES);
	
	public SkinCacheManager() {}
	
	public Skin getIfLoaded(String player){
		return skinCache.getOrDefault(player, null);
	}
	public Skin getOrLoad(String player){
		Skin s = getIfLoaded(player);
		if(s == null){
			if(Main.getDatenServer().isActive()){
				LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(player);
				skinCache.put(player, s = lp.getOwnSkin().getSync());
			}else{
				s = new SteveSkin();
			}
		}
		return s;
	}
}
