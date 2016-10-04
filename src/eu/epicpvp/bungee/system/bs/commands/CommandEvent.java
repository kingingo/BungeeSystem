package eu.epicpvp.bungee.system.bs.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.actionbar.ActionBar;
import dev.wolveringer.arrays.CachedArrayList;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.client.event.ServerMessageEvent;
import dev.wolveringer.bukkit.permissions.PermissionType;
import dev.wolveringer.chat.ChatClickable;
import dev.wolveringer.chat.ChatComponentText;
import dev.wolveringer.chat.ChatHoverable;
import eu.epicpvp.bungee.system.chat.ChatManager;
import dev.wolveringer.chat.ChatModifier;
import dev.wolveringer.chat.ChatSerializer;
import dev.wolveringer.chat.EnumClickAction;
import dev.wolveringer.chat.EnumHoverAction;
import dev.wolveringer.chat.IChatBaseComponent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandEvent extends Command implements Listener, CachedArrayList.UnloadListener<Player> {
	private static class EventActionBar extends ActionBar.ActionBarMessage {
		private CommandEvent command;

		//public ActionBarMessage(String key, String message, int importance, String permission) {
		public EventActionBar(CommandEvent command) {
			super("event", null, 150, null);
			this.command = command;
		}

		public String getMessage() {
			if (this.command.active) {
				return "§e§lEvent §a§lactive! §e§lJoin now §c§l/event§e§l!";
			}
			return null;
		}
	}

	private static class ChatBoxInvite extends ChatManager.ChatBoxModifier {
		public ChatBoxInvite(Player player, ChatManager handle, String inviter) {
			super("event", 150, player, handle, getInviteMessage(player, inviter));
		}

		private static List<IChatBaseComponent> getInviteMessage(Player player, String inviter) {
			ArrayList<IChatBaseComponent> out = new ArrayList<>();
			out.add(ChatSerializer.fromMessage("§7-----------------------------------------------"));
			out.add(ChatSerializer.fromMessage("§aDu wurdest von §e" + inviter + " §ain das Event eingeladen!"));
			IChatBaseComponent comp = new ChatComponentText("§aMöchtest du der Einladung folgen: ");
			comp.addSibling(new ChatComponentText("Ja").setChatModifier(new ChatModifier().setColor(ChatColor.GREEN).setBold(true).setChatClickable(new ChatClickable(EnumClickAction.RUN_COMMAND, "/event invite accept")).setHover(new ChatHoverable(EnumHoverAction.SHOW_TEXT, ChatSerializer.fromMessage("§aKlick mich für ja")))));
			comp.addSibling(new ChatComponentText(" §7| "));
			comp.addSibling(new ChatComponentText("Nein").setChatModifier(new ChatModifier().setColor(ChatColor.RED).setBold(true).setChatClickable(new ChatClickable(EnumClickAction.RUN_COMMAND, "/event invite deny")).setHover(new ChatHoverable(EnumHoverAction.SHOW_TEXT, ChatSerializer.fromMessage("§cKlick mich für nein")))));
			out.add(comp);
			out.add(ChatSerializer.fromMessage("§7-----------------------------------------------"));
			return out;
		}

		public boolean isKeepChatVisiable() {
			return true;
		}
	}

	private ServerInfo server = null;
	private boolean active = false;
	private int time = 2000;
	private CachedArrayList<UUID> connections = new CachedArrayList<>(2000, TimeUnit.MILLISECONDS);
	private int connectionsLimit = 10;
	private CachedArrayList<Player> invites = new CachedArrayList<>(1, TimeUnit.MINUTES);

	public CommandEvent(String name) {
		super(name);
		ActionBar.getInstance().addMessage(new EventActionBar(this));
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
		this.invites.addUnloadListener(this);
	}

	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if (args.length == 0) {
			if ((this.active) && (this.server != null)) {
				if (p.getServer().getInfo().equals(this.server)) {
					p.sendMessage("§cDu bist bereits auf dem Event-Server!");
					return;
				}
				if (this.connections.size() >= this.connectionsLimit) {
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.wait", sender, new Object[0]));
					return;
				}
				if (this.invites.contains(sender)) {
					ChatManager.getInstance().removeChatBoxModifier((Player) p, "event");
					this.invites.remove(sender);
				}
				this.connections.add(UUID.randomUUID());
				Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(3));
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.joined", sender, new Object[0]));
				p.connect(this.server);
			} else {
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.novent", sender, new Object[0]));
			}
		} else if ((args[0].equalsIgnoreCase("toggle")) && (PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true))) {
			if (this.active) {
				this.active = false;
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.active.false", sender, new Object[0]));
			} else {
				this.active = true;
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.active.true", sender, new Object[0]));
			}
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(1).writeBoolean(this.active));
		} else if ((args[0].equalsIgnoreCase("set")) && (PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true))) {
			if ((args.length == 2) && (BungeeCord.getInstance().getServerInfo(args[1]) != null)) {
				this.server = BungeeCord.getInstance().getServerInfo(args[1]);
				Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(0).writeString(BungeeCord.getInstance().getServerInfo(args[1]).getName()));
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.set", sender, new Object[] { this.server.getName() }));
				return;
			}
			this.server = p.getServer().getInfo();
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(0).writeString(this.server.getName()));
			p.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.set", sender, new Object[] { this.server.getName() }));
		} else if ((args[0].equalsIgnoreCase("setCLimit")) && (PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true))) {
			if (args.length != 3) {
				sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.help.climit", sender, new Object[0]));
				return;
			}
			this.connections = new CachedArrayList<>(this.time = Integer.parseInt(args[2]), TimeUnit.MILLISECONDS);
			this.connectionsLimit = Integer.parseInt(args[1]);
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(2).writeInt(this.connectionsLimit).writeInt(Integer.parseInt(args[2])));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.climit.set", sender, new Object[] { Integer.valueOf(this.connectionsLimit), args[2] }));
		} else if ((args[0].equalsIgnoreCase("info")) && (PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true))) {
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + (this.active ? Main.getTranslationManager().translate("command.event.status.active", sender, new Object[0]) : Main.getTranslationManager().translate("command.event.status.disabled", sender, new Object[0])));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.status.server", sender, new Object[] { "" + this.server.getName() }));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.status.connectionLimit", sender, new Object[] { Integer.valueOf(this.connectionsLimit), Integer.valueOf(this.time) }));
		} else if ((args.length == 2) && (args[0].equalsIgnoreCase("invite"))) {
			if (args[1].equalsIgnoreCase("accept")) {
				BungeeCord.getInstance().getPluginManager().dispatchCommand(sender, "event");
			} else {
				ChatManager.getInstance().removeChatBoxModifier((Player) p, "event");
				this.invites.remove((Player) p);
			}
		} else if ((args[0].equalsIgnoreCase("invite")) && (PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true))) {
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(4).writeString(sender.getName()));
			for (ProxiedPlayer pl : BungeeCord.getInstance().getPlayers()) {
				invite((Player) pl, p.getName());
			}
			p.sendMessage("§aDu hast alle Leute eingeladen!");
		} else {
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.help.join", sender, new Object[0]));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.help.invite", sender, new Object[0]).replaceAll("§m", ""));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.help.toggle", sender, new Object[0]));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.help.set", sender, new Object[0]));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.help.climit", sender, new Object[0]));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender, new Object[0]) + Main.getTranslationManager().translate("command.event.help.info", sender, new Object[0]));
		}
	}

	@EventHandler
	public void a(ServerMessageEvent e) {
		String inviter;
		if (e.getChannel().equalsIgnoreCase("eventServer")) {
			byte action = e.getBuffer().readByte();
			if (action == 0) {
				this.server = BungeeCord.getInstance().getServerInfo(e.getBuffer().readString());
			} else if (action == 1) {
				this.active = e.getBuffer().readBoolean();
			} else if (action == 2) {
				this.connectionsLimit = e.getBuffer().readInt();
				this.connections = new CachedArrayList(this.time = e.getBuffer().readInt(), TimeUnit.MILLISECONDS);
			} else if (action == 3) {
				this.connections.add(UUID.randomUUID());
			} else if (action == 4) {
				inviter = e.getBuffer().readString();
				for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
					invite((Player) p, inviter);
				}
			}
		}
	}

	private void invite(Player player, String inviter) {
		if (player.getServer().getInfo().equals(this.server)) {
			return;
		}
		this.invites.add(player);
		ChatManager.getInstance().addChatBoxModifier(player, new ChatBoxInvite(player, ChatManager.getInstance(), inviter));
	}

	public boolean canUnload(Player element) {
		ChatManager.getInstance().removeChatBoxModifier(element, "event");
		return true;
	}
}

// command.event.wait - §cToo many people try to connect. Please wait.
// command.event.joined - §aCongratulations you joined the §eEvent-Server§a!
// command.event.noevent - §cThere is no §eevent-round §cscheduled!
// command.event.active.false - §6The event is now §cdisabled
// command.event.active.true - §6The event is now §aenabled
// command.event.set - §6You set the event-server to §e%s0 [servername]
// command.event.climit.help - §cUsage: /event setCLimit <connection>
// <time(millis)>
// command.event.climit.set - §aYou set the connection limit to §e%s0
// §aconnection per §e%s1 §ams
// command.event.status.active - §bEvent-Status: §aactive
// command.event.status.disabled - §bEvent-Status: §cdisabled
// command.event.status.server - §bTarget-Server: %s0 [servername]
// command.event.status.connectionLimit - §bConnection limit: §a%s0§7/§a%s1ms
// [connectionsLimit,time]
// command.event.help.join -§a/event §7| §bJoin to the event
// command.event.help.invite -§a§m/event invite §7§m| §b§mInvite all players
// command.event.help.toggle - §a/event toggle §7| §bEnable/disbale the event
// command.event.help.set - §a/event set <server> §7| §bSet the event server
// command.event.help.climit - §a/event setCLimit <connections> <time> §7| §bSet
// the connection limit
// command.event.help.info - §a/event info §7| §bView the event informations
