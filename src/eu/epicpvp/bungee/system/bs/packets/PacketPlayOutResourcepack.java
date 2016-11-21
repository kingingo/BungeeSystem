package eu.epicpvp.bungee.system.bs.packets;

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
	public void read(PacketDataSerializer serializer) {
		url = serializer.readString(-1);
		hash = serializer.readString(-1);
	}

	@Override
	public void write(PacketDataSerializer serializer) {
		serializer.writeString(url);
		serializer.writeString(hash);
	}

}
