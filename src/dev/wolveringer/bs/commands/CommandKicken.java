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

public class CommandKicken extends Command implements Listener{

	public CommandKicken(String name) {
		super("bkick",null,name,"kick");
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer){
			ProxiedPlayer player = (ProxiedPlayer)sender;
			if(!PermissionManager.getManager().hasPermission(player, PermissionType.KICKEN,true))return;
			
			if(args.length<1){
				player.sendMessages(Language.getText(player, "PREFIX")+"/kicken [Player] [Grund]");
			}else{
				String grund = "Kein Grund Angegeben";
				String banned=args[0];

				if(args.length>1){
					StringBuilder sb = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						sb.append(args[i]);
						sb.append(" ");
					}
					sb.setLength(sb.length() - 1);
					grund = sb.toString();
				}
				if(BungeeCord.getInstance().getPlayer(banned) != null)
					BungeeCord.getInstance().getPlayer(banned).disconnect("§cKick reson:§b "+grund);
				else
					Main.getDatenServer().getClient().kickPlayer(Main.getDatenServer().getClient().getPlayerAndLoad(banned).getUUID(), "§cKick reson:§b "+grund);
				Main.getDatenServer().teamMessage(Language.getText(player, "PREFIX")+Language.getText(player, "BG_KICK_MSG",new String[]{banned,player.getName(),grund}));
			}
		}
	}

}
