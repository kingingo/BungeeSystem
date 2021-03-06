package eu.epicpvp.bungee.system.teamspeak;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.dataserver.protocoll.packets.PacketTeamspeakRequestAction;
import eu.epicpvp.datenclient.event.EventListener;
import eu.epicpvp.datenserver.definitions.events.Event;
import eu.epicpvp.datenserver.definitions.events.teamspeak.TeamspeakLinkRequestEvent;
import net.md_5.bungee.BungeeCord;

public class TeamspeakListener implements EventListener{

	@Override
	public void fireEvent(Event e) {
		if(e instanceof TeamspeakLinkRequestEvent){
			TeamspeakLinkRequestEvent request = (TeamspeakLinkRequestEvent) e;
			Player player;
			if((player = (Player) BungeeCord.getInstance().getPlayer(request.getTargetName())) == null)
				return;
			GuiTeamspeakListRequest gui = new GuiTeamspeakListRequest(request) {
				@Override
				public void input(boolean accept) {
					if(accept){
						player.sendMessage("§aDu bist nun mit Teamspeak verbunden.");
					}
					else
					{
						player.sendMessage("§cDu hast die Anfrage abgelehnt.");
					}
					Main.getDatenServer().getClient().writePacket(new PacketTeamspeakRequestAction(((TeamspeakLinkRequestEvent) e).getToken(), accept));
				}
			};
			gui.setPlayer(player).openGui();
		}
	}

}
