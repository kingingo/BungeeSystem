package eu.epicpvp.bungee.system.bs.client;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.datenclient.client.connection.ServerInformations;
import dev.wolveringer.dataserver.gamestats.GameState;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus;
import net.md_5.bungee.BungeeCord;

public class ClientInfoManager implements ServerInformations{

	@Override
	public PacketInServerStatus getStatus() {
		PacketInServerStatus status = new PacketInServerStatus(0x00, BungeeCord.getInstance().getOnlineCount(), -1, "This is a Bungeecord", GameType.NONE, GameState.NONE,"NONE", true, Main.getInstance().getServerId());
		return status;
	}

}
