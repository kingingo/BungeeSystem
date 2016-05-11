package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

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
				player.sendMessages(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.kick.help", sender));
			}else{
				String grund = "§6No reason set";
				String targetPlayer = args[0];

				if(args.length>1){
					StringBuilder sb = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						sb.append(args[i]);
						sb.append(" ");
					}
					sb.setLength(sb.length() - 1);
					grund = ChatColor.translateAlternateColorCodes('&', sb.toString());
				}
				//if(BungeeCord.getInstance().getPlayer(banned) != null)
				//	BungeeCord.getInstance().getPlayer(banned).disconnect("§cKick reson:§b "+grund);
				//else
				try{
					Main.getDatenServer().getClient().kickPlayer(Main.getDatenServer().getClient().getPlayerAndLoad(targetPlayer).getPlayerId(), "§cKick reason:§b "+grund);
				}catch(Exception e){
					sender.sendMessage("§xception: "+e.getMessage());
				}
				Main.getDatenServer().teamMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.kick.teammessage", sender,new String[]{targetPlayer,player.getName(),grund}));
			}
		}
	}

}
//command.kick.help - /kick [Player] [Grund] §7| §aKick a player
//command.kick.teammessage - §e%s0§c was kicked by §e%s1§c. Reason: §e%s2