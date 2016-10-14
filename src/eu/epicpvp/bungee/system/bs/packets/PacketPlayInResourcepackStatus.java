package eu.epicpvp.bungee.system.bs.packets;

import dev.wolveringer.BungeeUtil.ClientVersion;
import dev.wolveringer.BungeeUtil.packets.Abstract.PacketPlayIn;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.packet.PacketDataSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class PacketPlayInResourcepackStatus extends Packet implements PacketPlayIn{
	public static enum Action {
		SUCCESSFULLY_LOADED,
		DECLINED,
		DOWNLOAD_FAILED,
		ACCEPTED;
	}
	private String hash;
	private Action action;

	@Override
	public void read(PacketDataSerializer s) {
		if (getVersion().getBigVersion() == ClientVersion.BigClientVersion.v1_8) {
			hash = s.readString(-1);
		}
		action = Action.values()[s.readVarInt()];
	}

	@Override
	public void write(PacketDataSerializer s) {
		if (getVersion().getBigVersion() == ClientVersion.BigClientVersion.v1_8) {
			s.writeString(hash);
		}
		s.writeVarInt(action.ordinal());
	}
}
