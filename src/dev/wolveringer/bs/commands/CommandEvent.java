package dev.wolveringer.bs.commands;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandEvent extends Command implements Listener {

	private ServerInfo server = null;
	private boolean active = false;
	private int time = 2000;
	private CachedArrayList<UUID> connections = new CachedArrayList<>(2000, TimeUnit.MILLISECONDS);
	private int connectionsLimit = 10;

	public CommandEvent(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;

		if (args.length == 0) {
			if (active) {
				if (connections.size() >= connectionsLimit) {
					p.sendMessage(Language.getText(p, "PREFIX") + Language.getText(p, "BG_EVENT_SERVER_WAIT"));
					return;
				}
				connections.add(UUID.randomUUID());
				Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(3));
				p.sendMessage(Language.getText(p, "PREFIX") + Language.getText(p, "BG_EVENT_SERVER"));
				p.connect(server);
			} else {
				p.sendMessage(Language.getText(p, "PREFIX") + Language.getText(p, "BG_EVENT_NOT_NOW"));
			}
		} else if (args[0].equalsIgnoreCase("toggle") && PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true)) {
			if (active) {
				active = false;
				p.sendMessage(Language.getText(p, "PREFIX") + "§6Event:§c " + active);
			} else {
				active = true;
				p.sendMessage(Language.getText(p, "PREFIX") + "§6Event:§a " + active);
			}
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(1).writeBoolean(active));
		} else if (args[0].equalsIgnoreCase("set") && PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true)) {
			if (args.length == 2) {
				if (BungeeCord.getInstance().getServerInfo(args[1]) != null) {
					server = BungeeCord.getInstance().getServerInfo(args[1]);
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(0).writeString(BungeeCord.getInstance().getServerInfo(args[1]).getName()));
					p.sendMessage(Language.getText(p, "PREFIX") + "§6Der Event server wurde gesetzt!");
					return;
				}
			}
			server = p.getServer().getInfo();
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(0).writeString(server.getName()));
			p.sendMessage(Language.getText(p, "PREFIX") + "§6Der Event server wurde auf deinem Server gesetzt!");
		} else if (args[0].equalsIgnoreCase("setCLimit") && PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true)) {
			if (args.length != 3) {
				sender.sendMessage("§cUsage: /event setCLimit <connection> <time(millis)>");
				return;
			}
			connections = new CachedArrayList<>(time = Integer.parseInt(args[2]), TimeUnit.MILLISECONDS);
			connectionsLimit = Integer.parseInt(args[1]);
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(2).writeInt(connectionsLimit).writeInt(Integer.parseInt(args[2])));
			sender.sendMessage("§aConnectionlimit  gesetzt");
		} else if (args[0].equalsIgnoreCase("info") && PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true)) {
			sender.sendMessage("§bEvent-Status: "+(active ? "§aActive":"§cDisabled"));
			sender.sendMessage("§bTarget-Server: "+(server == null ? "§7undefined":"§c"+server.getName()));
			sender.sendMessage("§bConnection limit: §a"+connectionsLimit+"§7/§a"+time+"ms");
		}
		else
		{
			sender.sendMessage("§a/event §7| §bJoin event");
			sender.sendMessage("§a§m/event invite §7§m| §b§mLade alle Spieler ein.");
			sender.sendMessage("§a/event toggle §7| §bSchalte das event ein/aus");
			sender.sendMessage("§a/event set <server> §7| §bSetze den Event-Server");
			sender.sendMessage("§a/event setCLimit <connections> <time> §7| §bSetze die Server join limits");
			sender.sendMessage("§a/event info §7| §bEvent-Info");
		}
	}

	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("eventServer")) {
			byte action = e.getBuffer().readByte();
			if (action == 0) {
				server = BungeeCord.getInstance().getServerInfo(e.getBuffer().readString());
			} else if (action == 1) {
				active = e.getBuffer().readBoolean();
			} else if (action == 2) {
				connectionsLimit = e.getBuffer().readInt();
				connections = new CachedArrayList<>(time = e.getBuffer().readInt(), TimeUnit.MILLISECONDS);
			}
			else if(action == 3){
				connections.add(UUID.randomUUID());
			}
		}
	}
}
