package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandPremium extends Command{

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
						Main.getDatenServer().getClient().kickPlayer(p.getUniqueId(), "§aApplay Changes");
						target.setPremiumSync(false);
					}
				}
			}else if(args[0].equalsIgnoreCase("off")){
				if(PermissionManager.getManager().hasPermission(p, PermissionType.PREMIUM_TOGGLE,true)){
					String name = args[1];
					LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(name);
					if(target.isPremiumSync()){
						p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_PREMIUM_OFF", name));	
						Main.getDatenServer().getClient().kickPlayer(target.getUUID(), "§aApplay Changes");
						target.setPremiumSync(false);
					}
				}
			}
		}
	}
}
