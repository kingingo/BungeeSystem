package dev.wolveringer.bs.message;

import dev.wolveringer.bs.client.event.ServerMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageListener implements Listener{
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("news")){
			byte action = e.getBuffer().readByte();
			if(action == 0)
				for(MessageManager m : MessageManager.messageManagers.values())
					m.updateMessages();
		}
	}
}
