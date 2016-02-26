package dev.wolveringer.bs.login;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;

import dev.wolveringer.bs.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginManager implements Listener{
	private static LoginManager manager;
	public static void setManager(LoginManager manager) {
		LoginManager.manager = manager;
	}
	public static LoginManager getManager() {
		return manager;
	}
	private ArrayList<ProxiedPlayer> loggedIn = new ArrayList<>();
	
	public LoginManager() {
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}
	
	@EventHandler
	public void a(PluginMessageEvent e){
		if(e.getSender() instanceof ServerConnection){
			  if (e.getTag().equalsIgnoreCase("BungeeCord")) {
				  DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
				  try{
					  if(in.readUTF().equalsIgnoreCase("login")){
						  ProxiedPlayer player = BungeeCord.getInstance().getPlayer(in.readUTF());
						  if(player != null)
							  loggedIn.add(player);
					  }
				  }catch(Exception ex){
					  ex.printStackTrace();
				  }
			  }
		}
	}
	
	public boolean isLoggedIn(ProxiedPlayer player){
		return player.getPendingConnection().isOnlineMode() || loggedIn.contains(player);
	}
	
	@EventHandler
	public void a(PlayerDisconnectEvent e){
		loggedIn.remove(e.getPlayer());
	}
}
