package dev.wolveringer.chat;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.BungeeUtil.packets.Abstract.PacketPlayOut;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutChat;
import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.chat.ChatComponentText;
import dev.wolveringer.chat.IChatBaseComponent;
import dev.wolveringer.client.threadfactory.ThreadFactory;
import dev.wolveringer.client.threadfactory.ThreadRunner;
import dev.wolveringer.hashmaps.InitHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	private static final int LINE_HISTORY_SIZE = 40;
	private HashMap<Player, ChatHistory> histories;
	private HashMap<Player, List<ChatBoxModifier>> chatBoxes;
	private ThreadRunner chatResender;
	private CachedArrayList<PacketPlayOutChat> ignorePackets;

	public ChatManager() {
		this.histories = new InitHashMap<Player, ChatHistory>() {

			@Override
			public ChatHistory defaultValue(Player key) {
				return new ChatHistory();
			}
		};
		this.chatBoxes = new InitHashMap<Player, List<ChatBoxModifier>>() {

			@Override
			public List<ChatBoxModifier> defaultValue(Player key) {
				ArrayList<ChatBoxModifier> out = new ArrayList<>();
				out.add(new DefaultChatBoxModifier(key, ChatManager.this));
				return out;
			}
		};
		this.chatResender = ThreadFactory.getFactory().createThread(() -> {
			do {
				for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
					ChatBoxModifier mod = getChatBoxModifier((Player) p);
					if (!mod.isKeepChatVisiable())
						continue;
					mod.redraw();
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

	public void addChatBoxModifier(Player player, ChatBoxModifier modifier) {
		this.chatBoxes.get(player).add(modifier);
		modifier.redraw();
	}
	
	public void removeChatBoxModifier(Player player, String modifier) {
		List<ChatBoxModifier> mods = this.chatBoxes.get(player);
		for(ChatBoxModifier m : new ArrayList<>(mods))
			if(m.getName() != null)
				if(m.getName().equalsIgnoreCase(modifier))
					mods.remove(m);
	}

	public ChatBoxModifier getChatBoxModifier(Player player) {
		Collections.sort(this.chatBoxes.get((Object) player), new Comparator<ChatBoxModifier>() {
			@Override
			public int compare(ChatBoxModifier o1, ChatBoxModifier o2) {
				return Integer.compare(o2.getImportance(), o1.getImportance());
			}
		});
		Iterator<ChatBoxModifier> modifiers = this.chatBoxes.get((Object) player).iterator();
		while (modifiers.hasNext()) {
			ChatBoxModifier modifier = modifiers.next();
			if(modifier.getPermission() == null || player.hasPermission(modifier.getPermission()))
				return modifier;
		}
		return null;
	}
	
	public ChatBoxModifier getChatBoxModifier(Player player,String name) {
		Iterator<ChatBoxModifier> modifiers = this.chatBoxes.get((Object) player).iterator();
		while (modifiers.hasNext()) {
			ChatBoxModifier modifier = modifiers.next();
			if(modifier.getName().equalsIgnoreCase(name))
				return modifier;
		}
		return null;
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
		ChatBoxModifier mod = getChatBoxModifier(e.getPlayer());
		
		ThreadFactory.getFactory().createThread(()->{
			try {
				Thread.sleep(5);
			} catch (Exception e1) {}
			synchronized (histories) {
				this.histories.get((Object) e.getPlayer()).addMessage(((PacketPlayOutChat) e.getPacket()).getMessage());
			}
			if (mod instanceof DefaultChatBoxModifier) {
				return;
			}
			mod.redraw();
		}).start(); //Invoke after event fiered!
		if (mod instanceof DefaultChatBoxModifier) {
			return;
		}
		e.setCancelled(true);
	}

	public static ChatManager getInstance() {
		return instance;
	}

	public static void setInstance(ChatManager instance) {
		ChatManager.instance = instance;
	}

	private static final class DefaultChatBoxModifier extends ChatBoxModifier {
		public DefaultChatBoxModifier(Player player, ChatManager handle) {
			super("default", 0 ,player, handle, new ArrayList<IChatBaseComponent>());
		}
	}

	@RequiredArgsConstructor
	public static class ChatBoxModifier {
		@Getter
		private final String name;
		@Getter
		private final int importance;
		protected final Player player;
		private final ChatManager handle;
		private final List<IChatBaseComponent> footer;
		@Getter
		private String permission;
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

		public ChatBoxModifier(String name, int importance, Player player, ChatManager handle, List<String> footer, boolean unused) {
			super();
			this.name = name;
			this.importance = importance;
			this.player = player;
			this.handle = handle;
			this.footer = new ArrayList<>();
			for(String m : footer)
				this.footer.add(ChatSerializer.fromMessage(m));
		}
	}

	private static class ChatHistory {
		private LinkedList<IChatBaseComponent> stack = new LinkedList<IChatBaseComponent>();

		public ChatHistory() {
			for (int i = 0; i < LINE_HISTORY_SIZE; ++i) {
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
