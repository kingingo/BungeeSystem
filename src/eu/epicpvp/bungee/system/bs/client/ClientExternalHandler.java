package eu.epicpvp.bungee.system.bs.client;

import java.util.UUID;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.UtilBungeeCord;
import eu.epicpvp.bungee.system.bs.client.event.ServerMessageEvent;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.client.connection.State;
import eu.epicpvp.datenclient.client.external.BungeeCordActionListener;
import eu.epicpvp.datenserver.definitions.dataserver.player.Setting;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import eu.epicpvp.thread.ThreadFactory;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ClientExternalHandler implements BungeeCordActionListener{

	@Override
	public void sendMessage(int player, String message) {
		LoadedPlayer lp = Main.getDatenServer().getClient().getPlayerAndLoad(player);
		ProxiedPlayer p = BungeeCord.getInstance().getPlayer(lp.getName());
		if(p != null)
			p.sendMessage(message);
	}

	@Override
	public void broadcast(String permission, String message) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers()){
			if(permission == null || PermissionManager.getManager().hasPermission(player, permission, false))
				player.sendMessage(message);
		}
		BungeeCord.getInstance().getConsole().sendMessage("§7[Broadcast] §r"+message);
	}

	@Override
	public void kickPlayer(int player, String message) {
		LoadedPlayer p = Main.getDatenServer().getClient().getPlayerAndLoad(player);
		if(p != null && BungeeCord.getInstance().getPlayer(p.getName()) != null){
			BungeeCord.getInstance().getPlayer(p.getName()).disconnect(message);
		}
	}

	@Override
	public void disconnected() {
		for(ProxiedPlayer player : BungeeCord.getInstance().getPlayers()){
			if(((Player)player).getInventoryView() != null){
				((Player)player).closeInventory();
				((Player)player).sendMessage("§cLost connection to server cheef. Closing all Inventories.");
			}
		}
		if(Main.isRestarting())
			return;
		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				BungeeCord.getInstance().getConsole().sendMessage("§5Clientlistener §7> §cDatenclient disconnected!");
				for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers()){
					try{
						if(p.hasPermission("epicpvp.bc.dataserver"))
							p.sendMessage(Main.getTranslationManager().translateOffline("prefix", p)+"§eClientListener §7>> §cDatenclient disconnected");
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
				try {
					Main.getDatenServer().start();
				} catch (Exception e) {
					BungeeCord.getInstance().getConsole().sendMessage("§5Clientlistener §7> §cReconnect failed!");
					e.printStackTrace();
					return;
				}
				BungeeCord.getInstance().getConsole().sendMessage("§5Clientlistener §7> §aReconnect sucessfull!");
			}
		}).start();
	}

	@Override
	public void connected() {
		BungeeCord.getInstance().getConsole().sendMessage("§5Clientlistener §7> §aDatenclient connected!");
		for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers()){
			try{
				if(PermissionManager.getManager().hasPermission(p, "epicpvp.bc.dataserver"))
					p.sendMessage(Main.getTranslationManager().translate("prefix", p)+"§eClientListener §7>> §aDatenclient connected");
			}catch(Exception ex){ }
		}
	}

	@Override
	public void serverMessage(String channel, DataBuffer buffer) {
		BungeeCord.getInstance().getPluginManager().callEvent(new ServerMessageEvent(channel, buffer));
	}

	@Override
	public void sendPlayer(int player, String server) {
		LoadedPlayer p = Main.getDatenServer().getClient().getPlayer(player);
		if(p != null && p.isLoaded()){
			if(BungeeCord.getInstance().getServerInfo(server) != null)
				BungeeCord.getInstance().getPlayer(p.getName()).connect(BungeeCord.getInstance().getServerInfo(server));
		}
	}

	@Override
	public void settingUpdate(UUID player, Setting setting, String value) {
		if(setting == Setting.LANGUAGE)
			Main.getTranslationManager().updateLanguage(Main.getDatenServer().getClient().getPlayerAndLoad(player));
	}

	@Override
	public void restart(String kickMessage) {
		for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers())
			p.disconnect(kickMessage);
		UtilBungeeCord.restart();
	}
	@Override
	public void stop(String kickMessage) {
		for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers())
			p.disconnect(kickMessage);
		BungeeCord.getInstance().stop();
	}

	@Override
	public void error(State state, Exception e) {
		System.out.println("§cError while "+state);
		e.printStackTrace();
	}

	@Override
	public boolean isOnline(String name) {
		return BungeeCord.getInstance().getPlayer(name) != null && BungeeCord.getInstance().getPlayer(name).isConnected();
	}
}
