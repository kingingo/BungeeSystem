package dev.wolveringer.bs.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import dev.wolveringer.bs.Main;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.util.apache.StringUtils;
import dev.wolveringer.bukkit.permissions.PermissionType;
import dev.wolveringer.client.Callback;
import dev.wolveringer.client.PacketHandleErrorException;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandServer extends Command implements TabExecutor {
	public CommandServer(String name) {
		super(name);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender arg0, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			for (String s : BungeeCord.getInstance().getServers().keySet()) {
				if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
					list.add(s);
				}
			}

		} else {
			for (String s : BungeeCord.getInstance().getServers().keySet()) {
				list.add(s.toLowerCase());
			}
		}
		return list;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (PermissionManager.getManager().hasPermission((ProxiedPlayer) sender, PermissionType.SERVER, true)) {
			if (args.length == 1) {
				if(args[0].equalsIgnoreCase("list")){
					Iterator<String> serverss = BungeeCord.getInstance().getServers().keySet().iterator();
					List<BaseComponent[]> servers = Lists.newArrayList();
					ComponentBuilder currunt = new ComponentBuilder("");
					int count = 0;
					for (;serverss.hasNext();) {
						String s = serverss.next();
						if (!serverss.hasNext()) {
							currunt.append("§9" + s).color(ChatColor.BLUE).underlined(false).event(new ClickEvent(Action.RUN_COMMAND, "/server " + s)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§9Klicke hier um auf §e" + s + " §9Zu Connecten!").create()));
						} else {
							currunt.append("§9" + s + "§7,").color(ChatColor.BLUE).underlined(false).event(new ClickEvent(Action.RUN_COMMAND, "/server " + s)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§9Klicke hier um auf §e" + s + " §9Zu Connecten!").create()));
						}
						count++;
						if(count>5){
							servers.add(currunt.create());
							currunt = new ComponentBuilder("");
							count = 0;
						}
					}
					sender.sendMessage(new ComponentBuilder("§7----------------------------------------------------").create());
					sender.sendMessage(new ComponentBuilder("§6Servers: ").create());
					for (BaseComponent[] comp : servers)
						sender.sendMessage(comp);
					sender.sendMessage(new ComponentBuilder("§7----------------------------------------------------").create());
					return;
				}
				
				if (!(BungeeCord.getInstance().getServerInfo(args[0]) == null)) {
					if (sender instanceof ProxiedPlayer) {
						ProxiedPlayer p = (ProxiedPlayer) sender;
						p.connect(BungeeCord.getInstance().getServerInfo(args[0]));
					}
				} else {
					sender.sendMessage(new ComponentBuilder(Main.getTranslationManager().translate("prefix", sender)+ "§cServer not found!").create());
				}
				return;
			}
			else if(args.length == 2){
				if(args[0].equalsIgnoreCase("info")){
					sender.sendMessage("§aFetching server information.");
					Main.getDatenServer().getClient().getServerStatus(dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus.Action.SERVER, args[1], true).getAsync(new Callback<PacketOutServerStatus>() {
						@Override
						public void call(PacketOutServerStatus obj, Throwable exception) {
							if(exception != null){
								if(exception instanceof PacketHandleErrorException){
									sender.sendMessage("§cAn error happend: §4"+((PacketHandleErrorException)exception).getErrors()[0].getMessage());
								}
								else
									sender.sendMessage("§cAn error happend: §4"+exception.getMessage());
								return;
							}
							if(obj == null){
								sender.sendMessage("§cThe response is null");
								return;
							}
							sender.sendMessage("§aServerID: §e"+obj.getServerId());
							sender.sendMessage("§aSlots: §e"+obj.getPlayer()+"/"+obj.getMaxPlayers());
							sender.sendMessage("§aGame: §e"+(obj.getGames() != null && obj.getGames().length > 0 ? obj.getGames()[0] : "§7undefined"));
							sender.sendMessage("§aGamestate: §e"+obj.getState());
							sender.sendMessage("§aPlayers: §7(§b"+obj.getPlayers().size()+"§7)");
							sender.sendMessage("§e"+StringUtils.join(obj.getPlayers(),"§7, §e"));
						}
					});
					return;
				}
			}
			sender.sendMessage("§a/server list §7| §bListe alle Server auf.");
			sender.sendMessage("§a/server <name> §7| §bJoine einem Servers.");
			sender.sendMessage("§a/server info <name> §7| §bListe die Serverinformationen auf.");
		}
	}

}
