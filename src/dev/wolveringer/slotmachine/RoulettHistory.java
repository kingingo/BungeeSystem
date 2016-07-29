package dev.wolveringer.slotmachine;

import java.util.ArrayList;
import java.util.LinkedList;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class RoulettHistory implements Listener {
	public static interface HistoryListener {
		public void update();
	}
	
	static public RoulettHistory history;
	public static boolean active = true;
	public void setHistory(RoulettHistory history) {
		this.history = history;
	}

	public static RoulettHistory getHistory() {
		return history;
	}

	@AllArgsConstructor
	@Getter
	public static class HistoryItem {
		private String player;
		private int betOn;
		private int put;
		private int balance;
	}

	private LinkedList<HistoryItem> list = new LinkedList<>();
	private ArrayList<HistoryListener> listener = new ArrayList<>();
	
	public void add(String player,int betOn,int put,int balance){
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "roulett", new DataBuffer().writeString(player).writeInt(betOn).writeInt(put).writeInt(balance));
		list.add(new HistoryItem(player, betOn, put, balance));
		if(list.size() > 10)
			list.pollFirst();
		for(HistoryListener l : listener)
			l.update();
	}
	
	public LinkedList<HistoryItem> getList() {
		return list;
	}

	public void addListener(HistoryListener listener){
		this.listener.add(listener);
	}
	
	public void removeListener(HistoryListener listener){
		this.listener.remove(listener);
	}
	
	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("roulett")) {
			list.add(new HistoryItem(e.getBuffer().readString(), e.getBuffer().readInt(), e.getBuffer().readInt(), e.getBuffer().readInt()));
			if(list.size() > 10)
				list.pollFirst();
			for(HistoryListener l : listener)
				l.update();
		}
		if (e.getChannel().equalsIgnoreCase("toogle_roulett")) {
			active = e.getBuffer().readBoolean();
		}
	}
}
