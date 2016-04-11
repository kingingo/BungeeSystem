package dev.wolveringer.bs.commands;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
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
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ Main.getTranslationManager().translate("command.event.wait", sender));
					return;
				}
				connections.add(UUID.randomUUID());
				Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(3));
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ Main.getTranslationManager().translate("command.event.joined", sender));
				p.connect(server);
			} else {
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ Main.getTranslationManager().translate("command.event.novent", sender));
			}
		} else if (args[0].equalsIgnoreCase("toggle") && PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true)) {
			if (active) {
				active = false;
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.active.false", sender));
			} else {
				active = true;
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.active.true", sender));
			}
			
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(1).writeBoolean(active));
		} else if (args[0].equalsIgnoreCase("set") && PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true)) {
			if (args.length == 2) {
				if (BungeeCord.getInstance().getServerInfo(args[1]) != null) {
					server = BungeeCord.getInstance().getServerInfo(args[1]);
					Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(0).writeString(BungeeCord.getInstance().getServerInfo(args[1]).getName()));
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.set", sender,server.getName())); 
					return;
				}
			}
			server = p.getServer().getInfo();
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(0).writeString(server.getName()));
			p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.set", sender,server.getName())); 
		} else if (args[0].equalsIgnoreCase("setCLimit") && PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true)) {
			if (args.length != 3) {
				sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.help.climit", sender)); //
				return;
			}
			connections = new CachedArrayList<>(time = Integer.parseInt(args[2]), TimeUnit.MILLISECONDS);
			connectionsLimit = Integer.parseInt(args[1]);
			Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "eventServer", new DataBuffer().writeByte(2).writeInt(connectionsLimit).writeInt(Integer.parseInt(args[2])));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.climit.set", sender,connectionsLimit,args[2]));
		} else if (args[0].equalsIgnoreCase("info") && PermissionManager.getManager().hasPermission(p, PermissionType.ALL_PERMISSION, true)) {
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+(active ? Main.getTranslationManager().translate("command.event.status.active", sender):Main.getTranslationManager().translate("command.event.status.disabled", sender)));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.status.server", sender,(server == null ? "undefined":""+server.getName())));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.status.connectionLimit", sender,connectionsLimit,time));
		}
		else
		{
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.help.join", sender));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.help.invite", sender));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.help.toggle", sender));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.help.set", sender));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.help.climit", sender));
			sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.event.help.info", sender));
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
//command.event.wait - §cToo many people try to connect. Please wait.
//command.event.joined - §aCongratulations you joined the §eEvent-Server§a!
//command.event.noevent -  §cThere is no §eevent-round §cscheduled!
//command.event.active.false - §6The event is now §cdisabled
//command.event.active.true - §6The event is now §aenabled
//command.event.set - §6You set the event-server to §e%s0 [servername]
//command.event.climit.help - §cUsage: /event setCLimit <connection> <time(millis)>
//command.event.climit.set - §aYou set the connection limit to §e%s0 §aconnection per §e%s1 §ams
//command.event.status.active - §bEvent-Status: §aactive
//command.event.status.disabled - §bEvent-Status: §cdisabled
//command.event.status.server - §bTarget-Server: %s0 [servername]
//command.event.status.connectionLimit - §bConnection limit: §a%s0§7/§a%s1ms [connectionsLimit,time]
//command.event.help.join -§a/event §7| §bJoin to the event
//command.event.help.invite -§a§m/event invite §7§m| §b§mInvite all players
//command.event.help.toggle - §a/event toggle §7| §bEnable/disbale the event
//command.event.help.set - §a/event set <server> §7| §bSet the event server
//command.event.help.climit - §a/event setCLimit <connections> <time> §7| §bSet the connection limit
//command.event.help.info - §a/event info §7| §bView the event informations