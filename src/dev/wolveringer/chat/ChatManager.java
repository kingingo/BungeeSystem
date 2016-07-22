package dev.wolveringer.chat;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.packets.Abstract.PacketPlayOut;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutChat;
import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.chat.ChatComponentText;
import dev.wolveringer.chat.IChatBaseComponent;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.client.threadfactory.ThreadRunner;
import dev.wolveringer.hashmaps.InitHashMap;
import lombok.RequiredArgsConstructor;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.event.EventHandler;

public class ChatManager implements PacketHandler<PacketPlayOutChat> {
	private static ChatManager instance;
	private static final int LINE_HISTORY_SIZE = 20;
	private HashMap<Player, ChatHistory> histories;
	private HashMap<Player, ChatBoxModifier> chatBoxes;
	private ThreadRunner chatResender;
	private CachedArrayList<PacketPlayOutChat> ignorePackets;

	public ChatManager() {
		this.histories = new InitHashMap<Player, ChatHistory>() {

			@Override
			public ChatHistory defaultValue(Player key) {
				return new ChatHistory();
			}
		};
		this.chatBoxes = new InitHashMap<Player, ChatBoxModifier>() {

			@Override
			public ChatBoxModifier defaultValue(Player key) {
				return new DefaultChatBoxModifier(key, ChatManager.this);
			}
		};
		this.chatResender = ThreadFactory.getFactory().createThread(() -> {
			do {
				for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
					if (!this.chatBoxes.get((Object) ((Player) p)).isKeepChatVisiable())
						continue;
					this.chatBoxes.get((Object) ((Player) p)).redraw();
				}
				try {
					Thread.sleep(1500);
				} catch (Exception var1_2) {
				}
			} while (true);
		});
		this.ignorePackets = new CachedArrayList(1, TimeUnit.SECONDS);
		this.chatResender.start();
	}

	public void setChatBoxModifier(Player player, ChatBoxModifier modifier) {
		if (modifier == null) {
			this.chatBoxes.remove((Object) player);
			return;
		}
		this.chatBoxes.put(player, modifier);
		modifier.redraw();
	}

	public ChatBoxModifier getChatBoxModifier(Player player) {
		return this.chatBoxes.get((Object) player);
	}

	@EventHandler
	public void a(PlayerDisconnectEvent e) {
		this.histories.remove((Object) e.getPlayer());
		this.chatBoxes.remove((Object) e.getPlayer());
	}

	private void resendHistory(Player player) {
		Iterator<IChatBaseComponent> comps = this.histories.get((Object) player).getLines().iterator();
		while (comps.hasNext()) {
			this.sendMessage(player, comps.next());
		}
	}

	private void sendMessage(Player player, IChatBaseComponent comp) {
		PacketPlayOutChat packet = new PacketPlayOutChat(comp);
		this.ignorePackets.add(packet);
		player.sendPacket((PacketPlayOut) packet);
	}

	public void handle(PacketHandleEvent<PacketPlayOutChat> e) {
		if (((PacketPlayOutChat) e.getPacket()).getModus() == 2 || this.ignorePackets.contains((Object) e.getPacket())) {
			return;
		}
		this.histories.get((Object) e.getPlayer()).addMessage(((PacketPlayOutChat) e.getPacket()).getMessage());
		if (this.chatBoxes.get((Object) e.getPlayer()) instanceof DefaultChatBoxModifier) {
			return;
		}
		e.setCancelled(true);
		this.chatBoxes.get((Object) e.getPlayer()).redraw();
	}

	public static ChatManager getInstance() {
		return instance;
	}

	public static void setInstance(ChatManager instance) {
		ChatManager.instance = instance;
	}

	private static final class DefaultChatBoxModifier extends ChatBoxModifier {
		public DefaultChatBoxModifier(Player player, ChatManager handle) {
			super(player, handle, new ArrayList<IChatBaseComponent>());
		}
	}

	@RequiredArgsConstructor
	public static class ChatBoxModifier {
		protected final Player player;
		private final ChatManager handle;
		private final List<IChatBaseComponent> footer;
		private boolean keepChatVisiable;

		public void redraw() {
			this.handle.resendHistory(this.player);
			for (IChatBaseComponent s : this.footer) {
				this.handle.sendMessage(this.player, s);
			}
		}

		public Player getPlayer() {
			return this.player;
		}

		public ChatManager getHandle() {
			return this.handle;
		}

		public List<IChatBaseComponent> getFooter() {
			return this.footer;
		}

		public boolean isKeepChatVisiable() {
			return this.keepChatVisiable;
		}

		public void setKeepChatVisiable(boolean keepChatVisiable) {
			this.keepChatVisiable = keepChatVisiable;
		}
	}

	private static class ChatHistory {
		private LinkedList<IChatBaseComponent> stack = new LinkedList();

		public ChatHistory() {
			for (int i = 0; i < 20; ++i) {
				this.stack.add((IChatBaseComponent) new ChatComponentText(""));
			}
		}

		public void addMessage(IChatBaseComponent message) {
			this.stack.add(message);
			if (this.stack.size() > 20) {
				this.stack.pollFirst();
			}
		}

		public List<IChatBaseComponent> getLines() {
			return Collections.unmodifiableList(this.stack);
		}
	}

}
