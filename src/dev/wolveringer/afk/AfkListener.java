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
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.hashmaps.CachedHashMap;
import dev.wolveringer.server.CostumServer;
import dev.wolveringer.server.ServerConfiguration;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.ServerConnectEvent;

public class AfkListener implements PacketHandler<Packet> {
	private CachedArrayList<Player> timeoutObject = new CachedArrayList<>(3, TimeUnit.SECONDS);
	private CachedArrayList<Player> moveBack = new CachedArrayList<>(10, TimeUnit.SECONDS);

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
				moveBack.update();
				lastLocation.get(null);
			}
		}).start();
	}

	@Override
	public void handle(PacketHandleEvent<Packet> e) {
		if (e.getPacket() instanceof PacketPlayInFlying) {
			PacketPlayInFlying packet = (PacketPlayInFlying) e.getPacket();
			if (packet.hasPos()) {
				CostumServer server;
				if ((server = CostumServer.getServer(e.getPlayer())) != null) {
					if (moveBack.contains(e.getPlayer()))
						return;
					Location center = server.getConfig().getWorld().getWorldSpawn();
					if (center.distanceSquared(packet.getLocation()) > 3 * 3) {
						server.switchTo(BungeeCord.getInstance().getPluginManager().callEvent(new ServerConnectEvent(e.getPlayer(), ServerManager.DEFAULT_HUB)).getTarget());
						moveBack.add(e.getPlayer());
						return;
					}
				} else {
					lastLocation.lock();
					if (!lastLocation.containsKey(e.getPlayer()))
						lastLocation.put(e.getPlayer(), new Location(0, 0, 0));
					if (lastLocation.get(e.getPlayer()).distanceSquared(packet.getLocation()) < 2)
						return;
					if (!timeoutObject.contains(e.getPlayer()))
						timeoutObject.add(e.getPlayer());
					timeoutObject.resetTime(e.getPlayer());
					lastLocation.put(e.getPlayer(), packet.getLocation());
					lastLocation.unlock();
				}
			}
		} else if (e.getPacket() instanceof PacketPlayInChat) {
			if (CostumServer.getServer(e.getPlayer()) != null) {
				if (moveBack.contains(e.getPlayer()))
					return;
				CostumServer.getServer(e.getPlayer()).switchTo(BungeeCord.getInstance().getPluginManager().callEvent(new ServerConnectEvent(e.getPlayer(), ServerManager.DEFAULT_HUB)).getTarget());
				moveBack.add(e.getPlayer());
				return;
			} else {
				if (!timeoutObject.contains(e.getPlayer()))
					timeoutObject.add(e.getPlayer());
				timeoutObject.resetTime(e.getPlayer());
			}
		} else if (e.getPacket() instanceof PacketPlayInBlockDig || e.getPacket() instanceof PacketPlayInBlockPlace) {
			if (CostumServer.getServer(e.getPlayer()) != null) {
				if (moveBack.contains(e.getPlayer()))
					return;
				CostumServer.getServer(e.getPlayer()).switchTo(BungeeCord.getInstance().getPluginManager().callEvent(new ServerConnectEvent(e.getPlayer(), ServerManager.DEFAULT_HUB)).getTarget());
				moveBack.add(e.getPlayer());
				return;
			} else {
				if (!timeoutObject.contains(e.getPlayer()))
					timeoutObject.add(e.getPlayer());
				timeoutObject.resetTime(e.getPlayer());
			}
		}
	}

	public boolean canUnload(Player player) {
		CostumServer.createServer(Main.getInstance(), player, config);
		return true;
	}

}
