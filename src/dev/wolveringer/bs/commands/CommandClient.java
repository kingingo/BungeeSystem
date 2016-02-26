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

public class CommandClient extends Command implements Listener {

	private CommandSender ping;
	
	public CommandClient(String name) {
		super(name);
		BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender instanceof ProxiedPlayer){
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if (PermissionManager.getManager().hasPermission(p, PermissionType.CLIENT,true)) {
				if(args.length==0){
					p.sendMessage("§cNothink supported yet!");
					p.sendMessage(Language.getText(p, "PREFIX")+"/client send [Server] [Nachricht]");
					p.sendMessage(Language.getText(p, "PREFIX")+"/client ping");
					p.sendMessage(Language.getText(p, "PREFIX")+"/client reconnect");
				}else{
					/*
					if(args[0].equalsIgnoreCase("send")){
						if(args.length<=2){
							p.sendMessage(Language.getText(p, "PREFIX")+"/client send [Server] [Nachricht]");
						}else{
							String server = args[1];
							String msg = args[2];
							getInstance().getPacketManager().sendPacket(server, msg);
							p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_CLIENT_SEND", msg));
						}
					}else if(args[0].equalsIgnoreCase("ping")){
						ping=p;
						getInstance().getClient().sendMessageToServer("ping");
						p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_CLIENT_PING"));
					}if(args[0].equalsIgnoreCase("reconnect")){
						getInstance().getClient().disconnect(true);
						p.sendMessage(Language.getText(p, "PREFIX")+Language.getText(p, "BG_CLIENT_RECONNECT"));
					}
					*/
				}
			}
		}else{
			/*
			if(args.length==0){
				System.out.println("/client send [Nachricht]");
				System.out.println("/client ping");
				System.out.println("/client reconnect");
			}else{
				if(args[0].equalsIgnoreCase("send")){
					getInstance().getClient().sendMessageToServer(args[1]);
					System.out.println("Die Nachricht "+args[1]+" wurde gesendet!");
				}else if(args[0].equalsIgnoreCase("ping")){
					ping=sender;
					getInstance().getClient().sendMessageToServer("ping");
					System.out.println("Die Ping anfrage wurde gestellt!");
				}if(args[0].equalsIgnoreCase("reconnect")){
					getInstance().getClient().disconnect(true);
					System.out.println("Der Client reconnect ...!");
				}
			}
			*/
		}
	}
}
