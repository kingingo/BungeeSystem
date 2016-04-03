package dev.wolveringer.bs.information;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.mysql.MySQL;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class InformationManager implements Listener{
	private static InformationManager manager;
	private static final String base64_start = "base64;";
	public static void setManager(InformationManager manager) {
		InformationManager.manager = manager;
	}
	public static InformationManager getManager() {
		return manager;
	}
	
	private HashMap<String, String> inf_cash = new HashMap<>();

	public InformationManager() {
		MySQL.getInstance().commandSync("CREATE TABLE IF NOT EXISTS `BG_INFROMATIONS`(`key` VARCHAR (1000) NOT NULL default '', `value` VARCHAR (1000));");
		/*
		 * 
		 * 


		 */
	}

	public void reload() {
		inf_cash.clear();
	}

	public String getInfo(String info) {
		if(inf_cash.get(info) != null && !inf_cash.get(info).equalsIgnoreCase(""))
			return inf_cash.get(info);
		
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT value FROM `BG_INFROMATIONS` WHERE `key`='"+info+"'", 1);
		if(query.size()>0){
			if(query.get(0)[0].startsWith(base64_start))
			inf_cash.put(info, new String(DatatypeConverter.parseBase64Binary(query.get(0)[0]),Charset.forName("UTF-8")));
			else
			inf_cash.put(info, new String(query.get(0)[0]));
		}
		else
			inf_cash.put(info, null);
		return inf_cash.get(info);
	}

	public boolean infoExist(String info) {
		if(inf_cash.containsKey(info))
			return true;
		return getInfo(info) != null;
	}

	public void setInfo(String info, String anser) {
		anser = base64_start + DatatypeConverter.printBase64Binary(anser.getBytes(Charset.forName("UTF-8")));
		if(infoExist(info))
			MySQL.getInstance().command("UPDATE `BG_INFROMATIONS` SET value='" + anser + "' WHERE `key`='" + info + "'");
		else
			MySQL.getInstance().command("INSERT INTO `BG_INFROMATIONS`(key, value) VALUES ('" + info + "', '" + anser + "')");
		inf_cash.put(info, anser);
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "informations", new DataBuffer().writeByte(0).writeString(info));
	}
	
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("informations")){
			byte action = e.getBuffer().readByte();
			if(action == 0){
				String key = e.getBuffer().readString();
				inf_cash.remove(key);
				getInfo(key);
			}
		}
	}
}
