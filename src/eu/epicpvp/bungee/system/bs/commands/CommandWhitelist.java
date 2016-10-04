package eu.epicpvp.bungee.system.bs.commands;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.information.InformationManager;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandWhitelist extends Command implements Listener{
	public CommandWhitelist() {
		super("bwhitelist");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!PermissionManager.getManager().hasPermission(cs, PermissionType.COMMAND_WHITELIST,true)) return;
		if(args.length == 1){
			if(args[0].equalsIgnoreCase("on")){
				Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "whitelist", new DataBuffer().writeByte(1));
				InformationManager.getManager().setInfo("whitelistActive", "true");
				cs.sendMessage("§cYou §aenabled§c the whitelist.");
				return;
			}else if(args[0].equalsIgnoreCase("off")){
				Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "whitelist", new DataBuffer().writeByte(0));
				InformationManager.getManager().setInfo("whitelistActive", "false");
				cs.sendMessage("§cYou disabled the whitelist.");
				return;
			}else if(args[0].equalsIgnoreCase("status")){
				cs.sendMessage("§cWhitelist status: "+InformationManager.getManager().getInfo("whitelistActive"));
				return;
			}
			else if(args[0].equalsIgnoreCase("info")){
				cs.sendMessage("§aWhitelist active: §e"+"true".equalsIgnoreCase(InformationManager.getManager().getInfo("whitelistActive")));
				cs.sendMessage("§aWhitelist message: §r"+InformationManager.getManager().getInfo("whitelistMessage"));
			}
		}
		if(args.length>2){
			if(args[0].equalsIgnoreCase("message")){
				cs.sendMessage("§cYou set the disconnect message");
				InformationManager.getManager().setInfo("whitelistMessage", ChatColor.translateAlternateColorCodes('&', StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ")));
				return;
			}
		}
		cs.sendMessage("§c/whitelist <on/off/message/info>");
	}
}
