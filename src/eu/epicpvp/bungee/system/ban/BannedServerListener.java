package eu.epicpvp.bungee.system.ban;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInChat;

public class BannedServerListener implements PacketHandler<Packet>{

	@Override
	public void handle(PacketHandleEvent<Packet> event) {
		if(event.getPacket() instanceof PacketPlayInChat){
			if(BannedServerManager.getInstance().isBanned(event.getPlayer())){
				event.setCancelled(true);
				event.getPlayer().sendMessage("§cYou dont have permission to chat.");
			}
		}
	}

}
