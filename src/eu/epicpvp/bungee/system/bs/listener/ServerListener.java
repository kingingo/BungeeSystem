package dev.wolveringer.bs.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.ban.BannedServerManager;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.login.LoginManager;
import dev.wolveringer.bs.message.MessageManager;
import dev.wolveringer.bs.servermanager.ServerManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import dev.wolveringer.chat.ChatManager;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.ban.BanEntity;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.report.info.ActionBarInformation;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener{
	@EventHandler
	public void a(PlayerDisconnectEvent e){
		System.out.println("Player disconnect. UUID: "+e.getPlayer().getUniqueId());
		disconnectPlayer(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()));
		BungeeCord.getInstance().getScheduler().schedule(Main.getInstance(), ()->{
			if(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()) != null)
				if(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()).getServer().getSync() == null) //Player still disconnected
					Main.getDatenServer().getClient().clearCacheForPlayer(Main.getDatenServer().getClient().getPlayer(e.getPlayer().getName()));
		}, 500, TimeUnit.MILLISECONDS);
		if(BannedServerManager.getInstance() != null)
			BannedServerManager.getInstance().playerQuit((Player) e.getPlayer());
	}
	
	public void disconnectPlayer(LoadedPlayer player){
		while (player != null) {
			try{
				if(Main.getDatenServer().isActive())
					player.setServerSync(null); //disconnect
				player = null;
			}catch(Exception e){
				e.getMessage();
			}
		}
	}
	
	@EventHandler
	public void a(ServerConnectEvent e) {
		try{
			if(e.getPlayer().getServer() == null && ((UserConnection)e.getPlayer()).getPendingConnects().size() == 0){
				e.setTarget(ServerManager.DEFAULT_HUB);
				if(PermissionManager.getManager().hasPermission(e.getPlayer(), "report.info") && !PermissionManager.getManager().hasPermission(e.getPlayer(), "report.info.ignore")){
					if(ChatManager.getInstance().getChatBoxModifier((Player) e.getPlayer(), "report") == null)
						ChatManager.getInstance().addChatBoxModifier((Player) e.getPlayer(), new ActionBarInformation.ChatBoxMessage((Player) e.getPlayer(), ChatManager.getInstance(), ActionBarInformation.getInstance()));
				}
			}
			if (e.getTarget().getName().equalsIgnoreCase("hub")) {
				if(e.getPlayer().getServer() == null){
					List<BanEntity> entries = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName()).getBanStats(e.getPlayer().getPendingConnection().getAddress().getHostString(), 1).getSync();
					if (entries.size() > 0 && entries.get(0).isActive()) {
						BannedServerManager.getInstance().joinServer((Player) e.getPlayer(), entries.get(0));
						e.setCancelled(true);
						return;
					}
				}
				Queue<String> joinQueue;
				if (e.getPlayer().getPendingConnection().isOnlineMode() || LoginManager.getManager().isLoggedIn(e.getPlayer())) {
					LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getUniqueId());
					MessageManager.getmanager(player.getLanguageSync());
					if (PermissionManager.getManager().hasPermission(e.getPlayer(), PermissionType.PREMIUM_LOBBY, false))
						joinQueue = ServerManager.getManager().buildPremiumQueue();
					else
						joinQueue = ServerManager.getManager().buildLobbyQueue();
				} else {
					joinQueue = ServerManager.getManager().buildLoginQueue();
				}
				e.setTarget(BungeeCord.getInstance().getServerInfo(((LinkedList<String>) joinQueue).removeFirst()));
				((UserConnection) e.getPlayer()).setServerJoinQueue(joinQueue);
			}
		}catch(Exception ex){
			e.setCancelled(true);
			ex.printStackTrace();
			if(ex != null)
				((Player)e.getPlayer()).disconnect(ex);
			else
				System.out.println("Empty ex?!");
		}
	}
	
	@EventHandler
	public void a(ServerConnectedEvent e){
		BungeeCord.getInstance().createTitle().title(new ComponentBuilder("").create()).subTitle(new ComponentBuilder("").create()).fadeIn(0).stay(0).fadeOut(0).send(e.getPlayer()); //Reset title
	}
	
	@EventHandler
	public void a(ServerSwitchEvent e){
		Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getUniqueId()).setServerSync(e.getPlayer().getServer().getInfo().getName());
	}
}
