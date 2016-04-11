package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
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
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if (PermissionManager.getManager().hasPermission(p, PermissionType.CLIENT, true)) {
				if (args.length == 0) {
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.help.info", sender));
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.help.reconnect", sender));
				}
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("info")) {
						sender.sendMessage(Main.getTranslationManager().translate("prefix", sender) + (Main.getDatenServer().getClient().getHandle().isConnected() ? Main.getTranslationManager().translate("command.client.info.connected", sender) : Main.getTranslationManager().translate("command.client.info.disconnected", sender)));
						sender.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.info.ping", sender, Main.getDatenServer().getClient().getHandle().getPing()));
						sender.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.info.clientname", sender, Main.getDatenServer().getClient().getHandle().getName()));
					} else if (args[0].equalsIgnoreCase("reconnect")) {
						if (reconnecting) {
							sender.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.info.allredyreconnecting", sender));
							return;
						}
						reconnecting = true;
						BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
							@Override
							public void run() {
								if (Main.getDatenServer().getClient().getHandle().isConnected()) {
									sender.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.info.disconnecting", sender));
									Main.getDatenServer().getClient().getHandle().disconnect("reconnecting (cmd)");
									sender.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.info.disconnected", sender));
								}
								sender.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.info.connecting", sender));
								while (!Main.getDatenServer().getClient().getHandle().isConnected()) {
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								sender.sendMessage(Main.getTranslationManager().translate("prefix", sender) + Main.getTranslationManager().translate("command.client.info.connected", sender));
								reconnecting = false;
							}
						});
					}
				}
			}
		}
	}
}
//command.client.help.info - /client info §7| §aView client informations
//command.client.help.reconnect - /client reconnect §7| §aRestart the client
//command.client.info.connected - §bStatus: §aconnected
//command.client.info.disconnected - §bStatus: §cdisconnected
//command.client.info.ping - §bPing: §a%s0 [ping]
//command.client.info.clientname - §bClient known as: §a%s0 [name]
//command.client.info.allredyreconnecting - §cClient reconnecting alredy!
//command.client.info.disconnecting - §cDisconnecting client...
//command.client.info.connecting - §aReconnecting client...