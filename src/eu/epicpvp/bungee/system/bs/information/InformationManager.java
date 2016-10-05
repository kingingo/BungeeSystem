package eu.epicpvp.bungee.system.bs.information;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.client.event.ServerMessageEvent;
import eu.epicpvp.bungee.system.mysql.MySQL;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
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
		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT `value`,`key` FROM `BG_INFROMATIONS`", -1);
		for(String[] s : query)
			inf_cash.put(s[0], s[1]);
	}

	public void reload() {
		inf_cash.clear();
	}

	public String getInfo(String info) {
		String currunt = inf_cash.get(info);
		if(currunt != null && !currunt.equalsIgnoreCase(""))
			return inf_cash.get(info);

		ArrayList<String[]> query = MySQL.getInstance().querySync("SELECT value FROM `BG_INFROMATIONS` WHERE `key`='"+info+"'", 1);
		if(query.size()>0){
			if(query.get(0)[0].startsWith(base64_start))
				inf_cash.put(info, currunt = new String(DatatypeConverter.parseBase64Binary(query.get(0)[0].substring(base64_start.length())),Charset.forName("UTF-8")));
			else
				inf_cash.put(info, currunt = new String(query.get(0)[0]));
		}
		else
			inf_cash.put(info, null);
		return currunt;
	}

	public boolean infoExist(String info) {
		return getInfo(info) != null;
	}

	public void setInfo(String info, String anser) {
		String sanser = base64_start + DatatypeConverter.printBase64Binary(anser.getBytes(Charset.forName("UTF-8")));
		if(infoExist(info)){
			System.out.println("Update");
			MySQL.getInstance().command("UPDATE `BG_INFROMATIONS` SET `value`='" + sanser + "' WHERE `key`='" + info + "'",new MySQL.Callback<Boolean>(){
				@Override
				public void done(Boolean obj, Throwable ex) {
					if(ex!= null)
						ex.printStackTrace();
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "informations", new DataBuffer().writeByte(0).writeString(info));
				}

			});
		}else{
			System.out.println("Inert");
			MySQL.getInstance().command("INSERT INTO `BG_INFROMATIONS`(`key`, `value`) VALUES ('" + info + "', '" + sanser + "')",new MySQL.Callback<Boolean>(){
				@Override
				public void done(Boolean obj, Throwable ex) {
					if(ex!= null)
						ex.printStackTrace();
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "informations", new DataBuffer().writeByte(0).writeString(info));
				}

			});
		}
		inf_cash.put(info, anser);
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
