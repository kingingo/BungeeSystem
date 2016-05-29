package dev.wolveringer.bs.login;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportRequest.RequestType;
import dev.wolveringer.report.CloseReason;
import dev.wolveringer.report.ReportEntity;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerDisconnectListener implements Listener{
	@EventHandler
	public void a(PlayerDisconnectEvent e){
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(e.getPlayer().getName());
		ReportEntity[] are = Main.getDatenServer().getClient().getReportEntity(RequestType.PLAYER_OPEN_REPORTS, player.getPlayerId()).getSync();
		for(ReportEntity re : are){
			Main.getDatenServer().getClient().closeReport(re,CloseReason.DISCONNECTED.ordinal());
		}
	}
}
