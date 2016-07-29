package dev.wolveringer.actionbar;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.thread.ThreadFactory;
import dev.wolveringer.thread.ThreadRunner;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.Connection.Unsafe;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Chat;

public class ActionBar {
	private static ActionBar instance;

	public static ActionBar getInstance() {
		return instance;
	}

	public static void setInstance(ActionBar instance) {
		ActionBar.instance = instance;
	}

	public static class ActionBarMessage {
		private String key;
		private String message;

		@ConstructorProperties({ "key", "message", "importance", "permission" })
		public ActionBarMessage(String key, String message, int importance, String permission) {
			this.key = key;
			this.message = message;
			this.importance = importance;
			this.permission = permission;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public void setImportance(int importance) {
			this.importance = importance;
		}

		public void setPermission(String permission) {
			this.permission = permission;
		}

		public String getKey() {
			return this.key;
		}

		public String getMessage() {
			return this.message;
		}

		public int getImportance() {
			return this.importance;
		}

		private int importance = 1;
		private String permission;

		public String getPermission() {
			return this.permission;
		}

		public String toString() {
			return "ActionBarMessage [key=" + getKey() + ", message=" + getMessage() + ", importance=" + getImportance()
					+ ", permission=" + getPermission() + "]";
		}
	}

	private CopyOnWriteArrayList<ActionBarMessage> messages = new CopyOnWriteArrayList();
	private ThreadRunner runner;

	public void addMessage(ActionBarMessage message) {
		this.messages.add(message);
	}

	public ActionBarMessage getMessage(String key) {
		ActionBarMessage current = null;
		for (ActionBarMessage m : this.messages) {
			if (m.getKey().equalsIgnoreCase(key)) {
				return m;
			}
		}
		return null;
	}

	public void removeMessage(ActionBarMessage message) {
		this.messages.remove(message);
	}

	public Iterator<ActionBarMessage> getDisplayMessage(Player player) {
		ArrayList<ActionBarMessage> messages = new ArrayList(this.messages);
		Collections.sort(messages, new Comparator<ActionBarMessage>() {
			public int compare(ActionBar.ActionBarMessage o1, ActionBar.ActionBarMessage o2) {
				int compare = Boolean.compare(o1.getMessage() != null, o2.getMessage() != null);
				if (((o1.getMessage() == null) && (compare == 0)) || (compare != 0)) {
					return compare;
				}
				return Integer.compare(o1.importance, o2.importance);
			}
		});
		Collections.reverse(messages);
		return messages.iterator();
	}

	public ActionBar() {
		this.runner = ThreadFactory.getFactory().createThread(new Runnable() {
			public void run() {
				for (;;) {
					for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
						try {
							Iterator<ActionBar.ActionBarMessage> message = ActionBar.this.getDisplayMessage((Player) p);
							while (message.hasNext()) {
								ActionBar.ActionBarMessage c = (ActionBar.ActionBarMessage) message.next();
								if ((c.getMessage() != null) && ((c.getPermission() == null)
										|| (PermissionManager.getManager().hasPermission(p, c.getPermission())))) {
									ActionBar.this.sendActionBar((Player) p, c);
									break;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException localInterruptedException1) {
					}
				}
			}
		});
	}

	private void sendActionBar(Player p, ActionBarMessage message) {
		if ((message == null) || (message.getMessage() == null)) {
			return;
		}
		if ((message.getPermission() == null)
				|| (PermissionManager.getManager().hasPermission(p, message.getPermission()))) {
			p.unsafe().sendPacket(new Chat("{\"text\": \"" + message.getMessage() + "\"}", (byte) 2));
		}
	}

	public void start() {
		this.runner.start();
	}

	public void stop() {
		this.runner.stop();
	}
}
