package dev.wolveringer.afk;

import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInBlockDig;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInBlockPlace;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInChat;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInFlying;
import dev.wolveringer.api.position.Location;
import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.arrays.CachedArrayList.UnloadListener;
import dev.wolveringer.ban.BannedServerManager;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.message.MessageManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.hashmaps.CachedHashMap;
import dev.wolveringer.server.CostumServer;
import dev.wolveringer.server.ServerConfiguration;
import dev.wolveringer.thread.ThreadFactory;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.ServerConnectEvent;

public class AfkListener implements PacketHandler<Packet> {
	private static final int MAX_MOVEAWAY = 100;
	private CachedArrayList<Player> timeoutObject = new CachedArrayList<>(9, TimeUnit.MINUTES);
	private CachedArrayList<Player> ignore = new CachedArrayList<>(20, TimeUnit.SECONDS);

	private ServerConfiguration config;

	private CachedHashMap<Player, Location> lastLocation = new CachedHashMap<>(50, TimeUnit.SECONDS);
	
	public AfkListener(ServerConfiguration config) {
		this.config = config;
		timeoutObject.addUnloadListener(new UnloadListener<Player>() {
			@Override
			public boolean canUnload(Player element) {
				return AfkListener.this.canUnload(element);
			}
		});
		ThreadFactory.getFactory().createThread(() -> {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				timeoutObject.update();
				ignore.update();
				lastLocation.get(null);
			}
		}).start();
	}

	@Override
	public void handle(PacketHandleEvent<Packet> e) {
		try{
			if (e.getPacket() instanceof PacketPlayInFlying) {
				if(BannedServerManager.getInstance().isBanned(e.getPlayer()))
					return;
				if (ignore.contains(e.getPlayer()))
					return;
				PacketPlayInFlying packet = (PacketPlayInFlying) e.getPacket();
				if (packet.hasPos()) {
					CostumServer server;
					if ((server = CostumServer.getServer(e.getPlayer())) != null) {
						Location center = server.getConfig().getWorld().getWorldSpawn();
						
						if (center.getBlockY() < 0 || center.distanceSquared(packet.getLocation()) > MAX_MOVEAWAY*MAX_MOVEAWAY) {
							moveBack(e.getPlayer());
							return;
						}
					} else {
						lastLocation.lock();
						if (!lastLocation.containsKey(e.getPlayer()))
							lastLocation.put(e.getPlayer(), new Location(0, 0, 0));
						if (lastLocation.get(e.getPlayer()).distanceSquared(packet.getLocation()) < 0.5)
							return;
						if (!timeoutObject.contains(e.getPlayer())){
							timeoutObject.add(e.getPlayer());
						}
						timeoutObject.resetTime(e.getPlayer());
						lastLocation.put(e.getPlayer(), packet.getLocation());
						lastLocation.unlock();
					}
				}
			} else if (e.getPacket() instanceof PacketPlayInChat) {
				if(BannedServerManager.getInstance().isBanned(e.getPlayer()))
					return;
				if (CostumServer.getServer(e.getPlayer()) != null) {
					if (ignore.contains(e.getPlayer()))
						return;
					moveBack(e.getPlayer());
					return;
				} else {
					if (!timeoutObject.contains(e.getPlayer())){
						timeoutObject.add(e.getPlayer());
					}
					timeoutObject.resetTime(e.getPlayer());
				}
			} else if (e.getPacket() instanceof PacketPlayInBlockDig || e.getPacket() instanceof PacketPlayInBlockPlace) {
				if(BannedServerManager.getInstance().isBanned(e.getPlayer()))
					return;
				if (ignore.contains(e.getPlayer()))
					return;
				if (CostumServer.getServer(e.getPlayer()) != null) {
					moveBack(e.getPlayer());
				} else {
					if (!timeoutObject.contains(e.getPlayer())){
						timeoutObject.add(e.getPlayer());
					}
					timeoutObject.resetTime(e.getPlayer());
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public synchronized boolean canUnload(Player player) {
		if(CostumServer.getServer(player) != null)
			return true;
		if(BannedServerManager.getInstance().isBanned(player))
			return true;
		if(!player.isConnected())
			return true;
		System.out.println("AFK: "+player);
		CostumServer.createServer(Main.getInstance(), player, config);
		return true;
	}
	
	private synchronized void moveBack(Player player){
		ignore.add(player);
		if (!timeoutObject.contains(player)){
			timeoutObject.add(player);
		}
		CostumServer.getServer(player).switchTo(BungeeCord.getInstance().getPluginManager().callEvent(new ServerConnectEvent(player, ServerManager.DEFAULT_HUB)).getTarget());
		MessageManager.getmanager(Main.getTranslationManager().getLanguage(player)).playTitles(player);
	}

}
