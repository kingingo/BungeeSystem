package dev.wolveringer.report.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.actionbar.ActionBar;
import dev.wolveringer.actionbar.ActionBar.ActionBarMessage;
import dev.wolveringer.bs.Main;
import dev.wolveringer.chat.ChatManager;
import dev.wolveringer.chat.IChatBaseComponent;
import dev.wolveringer.chat.ChatManager.ChatBoxModifier;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.client.threadfactory.ThreadRunner;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import dev.wolveringer.report.ReportEntity;
import lombok.Getter;
import lombok.Setter;

public class ActionBarInformation {
	@Getter
	@Setter
	private static ActionBarInformation instance;
	private ThreadRunner updater;
	private boolean active = false;
	
	private int openReports = 0;
	
	private static class ActionBarInstance extends ActionBarMessage {
		private ActionBarInformation handle;
		public ActionBarInstance(ActionBarInformation handle) {
			super("report.info", null, 100, "bungee.report.info");
			this.handle = handle;
		}
		@Override
		public String getMessage() {
			if(handle.openReports == 0)
				return null;
			return "§c§lEs gibt momentan "+buildReportColor()+"§l"+handle.openReports+" §c§loffene Reports!";
		}
		private String buildReportColor(){
			if(handle.openReports < 5)
				return "§a";
			else if(handle.openReports < 15)
				return "§e";
			else if(handle.openReports < 25)
				return "§6";
			else 
				return "§4";
		}
	}
	
	public static class ChatBoxMessage extends ChatBoxModifier{
		private ActionBarInformation handle;
		public ChatBoxMessage(Player player, ChatManager cm, ActionBarInformation handle) {
			super("report", 100, player, cm, Arrays.asList("§e-------------------------------------------","§cEs gibt zu viele offene Reports! Bitte bearbeiten!","§e-------------------------------------------"), false);
			this.handle = handle;
		}
		
		@Override
		public int getImportance() {
			return handle.openReports > 10 ? 100 : -1;
		}
		
		@Override
		public boolean isKeepChatVisiable() {
			return true;
		}
	}
	
	public ActionBarInformation(int intervall, int updateIntervall) {
		updater = ThreadFactory.getFactory().createThread(()->{
			while (active) {
				try {
					Thread.sleep(updateIntervall);
				} catch (Exception e) {
				}
				try {
					if(Main.getDatenServer().isActive()){
						long start = System.currentTimeMillis();
						ReportEntity[] e = Main.getDatenServer().getClient().getReportEntity(RequestType.OPEN_REPORTS, -1).getSync();
						for(ReportEntity e1 : e){
							Main.getDatenServer().getClient().getPlayerAndLoad(e1.getReporter());
							Main.getDatenServer().getClient().getPlayerAndLoad(e1.getTarget());
						}
						long end = System.currentTimeMillis();
						if(end-start > 1000)
							System.out.println("Time needed for reports: "+(end-start));
						openReports = e.length;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	
	public void start(){
		active = true;
		updater.start();
		ActionBar.getInstance().addMessage(new ActionBarInstance(this));
	}
}
//http://hastebin.com/qusoqofepu.avrasm
