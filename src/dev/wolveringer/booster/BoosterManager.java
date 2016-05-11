package dev.wolveringer.booster;

import java.util.HashMap;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.event.EventListener;
import dev.wolveringer.events.Event;
import dev.wolveringer.events.booster.BoosterStatusChangeEvent;
import dev.wolveringer.hashmaps.InitHashMap;

public class BoosterManager implements EventListener{
	@SuppressWarnings("serial")
	private HashMap<BoosterType, NetworkBooster> booster = new InitHashMap<BoosterType, NetworkBooster>() {
		public NetworkBooster defaultValue(BoosterType key) {
			return new NetworkBooster.NotActiveBooster(key);
		};
	};
	
	public void init(){
		for(BoosterType t : BoosterType.values())
			if(t != BoosterType.NONE)
				loadBooster(t);
	}
	
	private void loadBooster(BoosterType type){
		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try{
						NetworkBooster b = Main.getDatenServer().getClient().getNetworkBooster(type).getSync();
						if(b == null)
							throw new NullPointerException("Booster is null!");
						booster.put(type, b);
						return;
					}catch(Exception e){
						e.printStackTrace();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}).start();
	}
	
	@Override
	public void fireEvent(Event e) {
		if(e instanceof BoosterStatusChangeEvent){
			if(((BoosterStatusChangeEvent) e).isActive())
				loadBooster(((BoosterStatusChangeEvent) e).getBoosterType());
			else
				booster.put(((BoosterStatusChangeEvent) e).getBoosterType(), new NetworkBooster.NotActiveBooster(((BoosterStatusChangeEvent) e).getBoosterType()));
		}
	}
	
	public NetworkBooster getBooster(BoosterType type){
		return booster.get(type);
	}
}
