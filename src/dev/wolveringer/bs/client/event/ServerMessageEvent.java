package dev.wolveringer.bs.client.event;

import dev.wolveringer.dataclient.protocoll.DataBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Event;

@AllArgsConstructor
@Getter
public class ServerMessageEvent extends Event{
	private String channel;
	private DataBuffer buffer;
}
