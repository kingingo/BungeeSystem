package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import lombok.Getter;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

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
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Language.getText(p, "BG_SERVER_NOT_EXIST"));
				return;
			}
			
			if(BungeeCord.getInstance().getPlayer(player) != null){
				BungeeCord.getInstance().getPlayer(player).connect( BungeeCord.getInstance().getServerInfo(server) );
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Language.getText(p, "BG_PLAYER_SEND"));
			}else{
				Main.getDatenServer().getClient().getPlayerAndLoad(player).setServerSync(server);
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Language.getText(p, "BG_SEND_A"));
			}
		}
	}
}
