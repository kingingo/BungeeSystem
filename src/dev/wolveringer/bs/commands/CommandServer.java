package dev.wolveringer.bs.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.command.PlayerCommand;

import com.google.common.collect.Lists;

import dev.wolveringer.bs.Main;

public class CommandServer extends PlayerCommand implements TabExecutor {
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
				if (!(BungeeCord.getInstance().getServerInfo(args[0]) == null)) {
					if (sender instanceof ProxiedPlayer) {
						ProxiedPlayer p = (ProxiedPlayer) sender;
						if (args[0].equalsIgnoreCase("build") && !PermissionManager.getManager().hasPermission(p, PermissionType.BUILD_SERVER, false))
							return;
						p.connect(BungeeCord.getInstance().getServerInfo(args[0]));
					}
				} else {
					sender.sendMessage(new ComponentBuilder(Main.getTranslationManager().translate("prefix", sender)+ "§cServer not found!").create());
				}
			} else {
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
			}
		}
	}

}
