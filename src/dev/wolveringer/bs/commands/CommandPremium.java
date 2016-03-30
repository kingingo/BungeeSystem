package dev.wolveringer.bs.commands;

import java.util.List;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.client.external.BungeeCordActionListener;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandPremium extends Command implements Listener{

	public CommandPremium(String name) {
		super(name);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer){
			ProxiedPlayer p = (ProxiedPlayer) sender;
			
			if(args.length == 0){
					p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_PREMIUM_MSG1"));
					p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_PREMIUM_MSG2"));
					p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_PREMIUM_MSG3"));
					p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_PREMIUM_MSG4"));
					p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_PREMIUM_MSG5"));
				return;
			}
			
			if(args[0].equalsIgnoreCase("on")){
				if(args.length==1){
					LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(p.getUniqueId());
					if(!target.isPremiumSync()){
						p.disconnect("§aApplay Changes");
						target.setPremiumSync(true);
					}
					else
					sender.sendMessage("§aYou are alredy on premium.");
				}
			}else if(args[0].equalsIgnoreCase("off") && args.length == 2){
				if(PermissionManager.getManager().hasPermission(p, PermissionType.PREMIUM_TOGGLE,true)){
					String name = args[1];
					LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(name);
					sender.sendMessage("§aChecking user details");
					if(target.isPremiumSync()){
						p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_PREMIUM_OFF", name));	
						if(BungeeCord.getInstance().getPlayer(name) != null){
							BungeeCord.getInstance().getPlayer(name).disconnect("§aApplay Changes");
						}else{
							Main.getDatenServer().getClient().kickPlayer(target.getUUID(), "§aApplay Changes").getSync();
						}
						if(!target.isLoaded())
							target.load();
						target.setPremiumSync(false);
						target.unload();
						Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "playerpremium", new DataBuffer().writeString(name));
					}
					else
						sender.sendMessage("§cThe user isnt premium");
				}
			}
		}
	}
	
	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("playerpremium")){
			Main.getDatenServer().getClient().clearCacheForPlayer(Main.getDatenServer().getClient().getPlayer(e.getBuffer().readString()));
		}
	}
}
