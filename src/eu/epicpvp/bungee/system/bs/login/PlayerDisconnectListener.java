package eu.epicpvp.bungee.system.bs.login;

import eu.epicpvp.bungee.system.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import dev.wolveringer.report.ReportState;
import dev.wolveringer.thread.ThreadFactory;
import dev.wolveringer.report.ReportEntity;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerDisconnectListener implements Listener{
	@EventHandler
	public void a(PlayerDisconnectEvent e){
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName());
		ThreadFactory.getFactory().createThread(()->{
			ReportEntity[] are = Main.getDatenServer().getClient().getReportEntity(RequestType.PLAYER_OPEN_REPORTS, player.getPlayerId()).getSync();
			for(ReportEntity re : are){
				Main.getDatenServer().getClient().closeReport(re,ReportState.DISCONNECTED_CLOSED.ordinal());
			}
		}).start();
		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2*60*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				for(ReportEntity re : Main.getDatenServer().getClient().getReportEntity(RequestType.OPEN_REPORTS, -1).getSync())
					if(re.getTarget() == player.getPlayerId())
						Main.getDatenServer().getClient().closeReport(re, ReportState.DISCONNECTED_CLOSED.ordinal());
			}
		}).start();
	}
}
