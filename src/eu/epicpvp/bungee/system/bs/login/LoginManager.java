package eu.epicpvp.bungee.system.bs.login;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.message.MessageManager;
import eu.epicpvp.bungee.system.bs.servermanager.ServerManager;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginManager implements Listener {
	private static LoginManager manager;

	public static void setManager(LoginManager manager) {
		LoginManager.manager = manager;
		BungeeCord.getInstance().registerChannel("login");
	}

	public static LoginManager getManager() {
		return manager;
	}

	private ArrayList<ProxiedPlayer> loggedIn = new ArrayList<>();

	public LoginManager() {
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@EventHandler
	public void a(PluginMessageEvent e) {
		if (e.getSender() instanceof ServerConnection) {
			if (e.getTag().equalsIgnoreCase("login")) {
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
				try {
					String sp;
					ProxiedPlayer player = BungeeCord.getInstance().getPlayer(sp = in.readUTF());
					if (player != null) {
						MessageManager.getManager(Main.getTranslationManager().getLanguage(player)).playTitles(player);
						loggedIn.add(player);
						if (PermissionManager.getManager().hasPermission(player, PermissionType.PREMIUM_LOBBY, false))
							player.connect(ServerManager.getManager().nextPremiumLobby());
						else
							player.connect(ServerManager.getManager().nextLobby());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public boolean isLoggedIn(ProxiedPlayer player) {
		return player.getPendingConnection().isOnlineMode() || loggedIn.contains(player);
	}

	@EventHandler
	public void a(PlayerDisconnectEvent e) {
		loggedIn.remove(e.getPlayer());
	}
}
