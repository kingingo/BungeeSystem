package eu.epicpvp.bungee.system.guild.listener;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.gameprofile.PlayerInfoData;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutPlayerInfo;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutScoreboardTeam;
import dev.wolveringer.chat.ChatMessage;
import dev.wolveringer.chat.IChatBaseComponent;

public class TabListener implements PacketHandler<Packet>{

	@Override
	public void handle(PacketHandleEvent<Packet> e) {
		if(e.getPacket() instanceof PacketPlayOutPlayerInfo)
			handle0((PacketHandleEvent<PacketPlayOutPlayerInfo>)((PacketHandleEvent) e));
		if(e.getPacket() instanceof PacketPlayOutScoreboardTeam)
			handle1((PacketHandleEvent<PacketPlayOutScoreboardTeam>)((PacketHandleEvent) e));
	}

	public void handle0(PacketHandleEvent<PacketPlayOutPlayerInfo> e) {
			for(PlayerInfoData data : e.getPacket().getData()){
				IChatBaseComponent base = data.getName();
				if(base != null){
					base.addSibling(new ChatMessage(" §7[§aX§7]"));
				}
			}
			System.out.println(e.getPacket().getAction()+":"+e.getPacket().getData());
	}
	public void handle1(PacketHandleEvent<PacketPlayOutScoreboardTeam> e) {
		//e.setCancelled(true);
	}

}
