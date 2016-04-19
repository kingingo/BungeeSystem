package dev.wolveringer.bs.listener;

import java.lang.reflect.Field;

import dev.wolveringer.bs.Main;
import dev.wolveringer.skin.Skin;
import dev.wolveringer.skin.SteveSkin;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;

public class SkinListener implements Listener{
	private static Field profileField = null;
	
	private static Field getProfileField() {
		if(profileField != null)
			return profileField;
		try {
			Field profileField = InitialHandler.class.getDeclaredField("loginProfile");
			profileField.setAccessible(true);
			return profileField;
		} catch (Throwable t) {
			System.err.println("Failed to get method handle for initial handel loginProfile field");
			t.printStackTrace();
		}
		return null;
	}
	
	@EventHandler
	public void a(PostLoginEvent e){
		try {
			LoginResult result = (LoginResult) getProfileField().get(e.getPlayer().getPendingConnection());
			Skin skin = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName()).getOwnSkin().getSync();
			if(result == null){
				result = new LoginResult(e.getPlayer().getUniqueId().toString(), new LoginResult.Property[0]);
				getProfileField().set(e.getPlayer().getPendingConnection(), result);
			}
			if(skin instanceof SteveSkin){
				if(!e.getPlayer().getPendingConnection().isOnlineMode()){
					result.setProperties(new LoginResult.Property[0]);
				}
			}else
				result.setProperties(new LoginResult.Property[]{new LoginResult.Property("textures", skin.getRawData(), skin.getSignature())});
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
	}
}
