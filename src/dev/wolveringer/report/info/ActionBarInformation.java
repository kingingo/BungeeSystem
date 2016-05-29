package dev.wolveringer.report.info;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.client.threadfactory.ThreadRunner;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Chat;

public class ActionBarInformation {
	private ThreadRunner thread;
	private ThreadRunner updater;
	private int intervall;
	private int updateIntervall;
	private boolean active = false;
	
	private int openReports = 0;
	
	public ActionBarInformation(int intervall, int updateIntervall) {
		this.intervall = intervall;
		this.updateIntervall = updateIntervall;
		thread = ThreadFactory.getFactory().createThread(()->{
			while (active) {
				try {
					Thread.sleep(intervall);
				} catch (Exception e) {
				}
				try {
					sendBar();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		updater = ThreadFactory.getFactory().createThread(()->{
			while (active) {
				try {
					Thread.sleep(updateIntervall);
				} catch (Exception e) {
				}
				try {
					if(Main.getDatenServer().isActive())openReports = Main.getDatenServer().getClient().getReportEntity(RequestType.OPEN_REPORTS, -1).getSync().length;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	
	public void start(){
		active = true;
		thread.start();
		updater.start();
	}
	
	private void sendBar(){
		if(openReports == 0)
			return;
		String message = "§c§lEs gibt momentan "+buildReportColor()+"§l"+openReports+" §c§loffene Reports!";
		for(ProxiedPlayer p : BungeeCord.getInstance().getPlayers())
			if(p != null)
				if(PermissionManager.getManager().hasPermission(p, "bungee.report.info"))
					p.unsafe().sendPacket(new Chat("{\"text\": \"" + message + "\"}", (byte) 2));
	}
	private String buildReportColor(){
		if(openReports < 2)
			return "§a";
		else if(openReports < 10)
			return "§e";
		else 
			return "§4";
	}
}
