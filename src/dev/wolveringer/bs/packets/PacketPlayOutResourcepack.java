package dev.wolveringer.bs.packets;

import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.Abstract.PacketPlayOut;
import dev.wolveringer.packet.PacketDataSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketPlayOutResourcepack extends Packet implements PacketPlayOut{
	private String url;
	private String hash;
	
	@Override
	public void read(PacketDataSerializer s) {
		url = s.readString(-1);
		hash = s.readString(-1);
	}

	@Override
	public void write(PacketDataSerializer s) {
		s.writeString(url);
		s.writeString(hash);
	}

}
