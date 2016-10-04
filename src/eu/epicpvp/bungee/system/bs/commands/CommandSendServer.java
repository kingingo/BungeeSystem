package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.permission.PermissionManager;
import dev.wolveringer.bukkit.permissions.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandSendServer extends Command implements Listener {

	public CommandSendServer(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer)sender;
		if(!PermissionManager.getManager().hasPermission(p, PermissionType.SERVER_SEND,true))return;
		if(args.length==0){
			p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+"/sendserver [Player] [Server]");
		}else{
			String player = args[0];
			
			if(player.equalsIgnoreCase(p.getName())){
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+"§cDu kannst dich nicht selber senden!");
				return;
			}
			
			String server = (args.length>1?args[1]:null);
			
			if(server==null){
				server=p.getServer().getInfo().getName();
			}
			
			if(BungeeCord.getInstance().getServerInfo(server)==null){
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.sendserver.notexist", sender)); //§cServer does not exist!
				return;
			}
			
			if(BungeeCord.getInstance().getPlayer(player) != null){
				BungeeCord.getInstance().getPlayer(player).connect( BungeeCord.getInstance().getServerInfo(server) );
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.sendserver.sended", sender)); //§aPlayer was send to the server
			}else{
				Main.getDatenServer().getClient().getPlayerAndLoad(player).setServerSync(server);
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.sendserver.other.sended", sender)); //§aA request was send to the BGs
			}
		}
	}
}
//command.sendserver.notexist - §cServer does not exist!
//command.sendserver.sended - §aPlayer was send to the server
//command.sendserver.other.sended - §aA request was send to the BGs