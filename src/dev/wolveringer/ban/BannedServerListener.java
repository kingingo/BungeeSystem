package dev.wolveringer.ban;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInChat;

public class BannedServerListener implements PacketHandler<Packet>{

	@Override
	public void handle(PacketHandleEvent<Packet> paramPacketHandleEvent) {
		if(paramPacketHandleEvent.getPacket() instanceof PacketPlayInChat){
			if(BannedServerManager.getInstance().isBanned(paramPacketHandleEvent.getPlayer())){
				paramPacketHandleEvent.setCancelled(true);
				paramPacketHandleEvent.getPlayer().sendMessage("§cYou dont have permission to chat.");
			}
		}
	}

}
