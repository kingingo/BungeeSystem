package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandClient extends Command implements Listener {

	private CommandSender ping;
	private boolean reconnecting;
	
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
					p.sendMessage(Language.getText(p, "PREFIX")+"/client info");
					p.sendMessage(Language.getText(p, "PREFIX")+"/client reconnect");
				}
				if(args.length == 1){
					if(args[0].equalsIgnoreCase("info")){
						sender.sendMessage("§bStatus: "+(Main.getDatenServer().getClient().getHandle().isConnected() ? "§aConnected":"§cDisconnected"));
						sender.sendMessage("§bPing: §a"+Main.getDatenServer().getClient().getHandle().getPing());
						sender.sendMessage("§bClient known as: §a"+Main.getDatenServer().getClient().getHandle().getName());
					}
					else if(args[0].equalsIgnoreCase("reconnect")){
						if(reconnecting){
							sender.sendMessage("§cClient reconnecting alredy!");
							return;
						}
						reconnecting = true;
						if(Main.getDatenServer().getClient().getHandle().isConnected()){
							sender.sendMessage("§cDisconnecting client...");
							Main.getDatenServer().getClient().getHandle().disconnect("reconnecting (cmd)");
							sender.sendMessage("§cDisconnected");
						}
						sender.sendMessage("§aReconnecting client...");
						while (!Main.getDatenServer().getClient().getHandle().isConnected()) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						sender.sendMessage("§aClient reconnected! :)");
						reconnecting = false;
					}
				}
			}
		}
	}
}
