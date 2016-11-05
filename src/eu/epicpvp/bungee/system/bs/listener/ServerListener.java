package eu.epicpvp.bungee.system.bs.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.ban.BannedServerManager;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.login.LoginManager;
import eu.epicpvp.bungee.system.bs.message.MessageManager;
import eu.epicpvp.bungee.system.bs.servermanager.ServerManager;
import eu.epicpvp.bungee.system.chat.ChatManager;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.bungee.system.report.info.ActionBarInformation;
import eu.epicpvp.datenclient.client.ClientWrapper;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.dataserver.ban.BanEntity;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener {

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		String name = player.getName();
		System.out.println("Player disconnect. Name: " + name + " UUID: " + player.getUniqueId());
		ClientWrapper client = Main.getDatenServer().getClient();
		LoadedPlayer loadedPlayer = client.getPlayer(name);
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), () -> disconnectPlayer(loadedPlayer));
		if (loadedPlayer != null) {
			BungeeCord.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
				if (loadedPlayer.getServer().getSync() == null) {
					client.clearCacheForPlayer(loadedPlayer);
				}
			}, 550, TimeUnit.MILLISECONDS);
		}
		BannedServerManager banServer = BannedServerManager.getInstance();
		if (banServer != null)
			banServer.playerQuit((Player) player);
	}

	public void disconnectPlayer(LoadedPlayer player) {
		while (player != null) {
			try {
				if (Main.getDatenServer().isActive())
					player.setServerSync(null); //disconnect
				player = null;
			} catch (Exception e) {
				e.getMessage();
			}
		}
	}

	@EventHandler
	public void onServerConnect(ServerConnectEvent event) {
		try {
			if (event.getPlayer().getServer() == null && ((UserConnection) event.getPlayer()).getPendingConnects().isEmpty()) {
				event.setTarget(ServerManager.DEFAULT_HUB);
				if (PermissionManager.getManager().hasPermission(event.getPlayer(), "report.info") && !PermissionManager.getManager().hasPermission(event.getPlayer(), "report.info.ignore")) {
					if (ChatManager.getInstance().getChatBoxModifier((Player) event.getPlayer(), "report") == null)
						ChatManager.getInstance().addChatBoxModifier((Player) event.getPlayer(), new ActionBarInformation.ChatBoxMessage((Player) event.getPlayer(), ChatManager.getInstance(), ActionBarInformation.getInstance()));
				}
			}
			if (event.getTarget().getName().equalsIgnoreCase("hub")) {
				if (event.getPlayer().getServer() == null) {
					String name = event.getPlayer().getName();
					String ip = event.getPlayer().getPendingConnection().getAddress().getAddress().getHostAddress();
					List<BanEntity> entries = Main.getDatenServer().getClient().getPlayerAndLoad(name).getBanStats(ip, 1).getSync();
					if (!entries.isEmpty()) {
						BanEntity banEntity = entries.get(0);
						if (banEntity.isActive()) {
							long ipBanEnd = banEntity.getDate() + TimeUnit.DAYS.toMillis(7);//7 * 24 * 60 * 60 * 1000;
							if (banEntity.getUsernames().stream().anyMatch(name::equalsIgnoreCase) || (banEntity.getIp().equalsIgnoreCase(ip) && System.currentTimeMillis() <= ipBanEnd)) {
								BannedServerManager.getInstance().joinServer((Player) event.getPlayer(), banEntity);
								event.setCancelled(true);
								return;
							}
						}
					}
				}
				Queue<String> joinQueue;
				if (event.getPlayer().getPendingConnection().isOnlineMode() || LoginManager.getManager().isLoggedIn(event.getPlayer())) {
					LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(event.getPlayer().getUniqueId());
					MessageManager.getManager(player.getLanguageSync());
					if (PermissionManager.getManager().hasPermission(event.getPlayer(), PermissionType.PREMIUM_LOBBY, false))
						joinQueue = ServerManager.getManager().buildPremiumQueue();
					else
						joinQueue = ServerManager.getManager().buildLobbyQueue();
				} else {
					joinQueue = ServerManager.getManager().buildLoginQueue();
				}
				event.setTarget(BungeeCord.getInstance().getServerInfo(((LinkedList<String>) joinQueue).removeFirst()));
				((UserConnection) event.getPlayer()).setServerJoinQueue(joinQueue);
			}
		} catch (Exception ex) {
			event.setCancelled(true);
			ex.printStackTrace();
			((Player) event.getPlayer()).disconnect(ex);
		}
	}

	@EventHandler
	public void onServerConnected(ServerConnectedEvent e) {
		//Reset title
		BungeeCord.getInstance().createTitle().title(new ComponentBuilder("").create()).subTitle(new ComponentBuilder("").create()).fadeIn(0).stay(0).fadeOut(0).send(e.getPlayer());
	}

	@EventHandler
	public void onServerSwitch(ServerSwitchEvent e) {
		Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getUniqueId()).setServerSync(e.getPlayer().getServer().getInfo().getName());
	}
}
