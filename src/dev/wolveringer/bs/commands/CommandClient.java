package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
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
	public void execute(CommandSender p, String[] args) {
		if (args.length == 0) {
			p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.help.info", p));
			p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.help.reconnect", p));
			p.sendMessage(Main.getTranslationManager().translate("prefix", p) + "/client blocktraffic <in/out/both> §7| §aSimulate a traffic block.");
		} else {
			if (args[0].equalsIgnoreCase("info")) {
				p.sendMessage(Main.getTranslationManager().translate("prefix", p) + (Main.getDatenServer().getClient().getHandle().isConnected() ? Main.getTranslationManager().translate("command.client.info.connected", p) : Main.getTranslationManager().translate("command.client.info.disconnected", p)));
				p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.info.ping", p, Main.getDatenServer().getClient().getHandle().getPingManager().getPing()));
				p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.info.clientname", p, Main.getDatenServer().getClient().getHandle().getName()));
			} else if (args[0].equalsIgnoreCase("reconnect")) {
				if (reconnecting) {
					p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.info.allredyreconnecting", p));
					return;
				}
				reconnecting = true;
				BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
					@Override
					public void run() {
						if (Main.getDatenServer().getClient().getHandle().isConnected()) {
							p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.info.disconnecting", p));
							Main.getDatenServer().getClient().getHandle().disconnect("reconnecting (cmd)");
							p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.info.disconnected", p));
						}
						p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.info.connecting", p));
						while (!Main.getDatenServer().getClient().getHandle().isConnected()) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						p.sendMessage(Main.getTranslationManager().translate("prefix", p) + Main.getTranslationManager().translate("command.client.info.connected", p));
						reconnecting = false;
					}
				});
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("blocktraffic")) {
					if(!args[1].equalsIgnoreCase("out")){
						p.sendMessage("§aStopping reader thread.");
						Main.getDatenServer().getClient().getHandle().unsave().getReaderThread().unsave().getThread().stop();
						p.sendMessage("§aStopped reader thread!");
					}
					if(!args[1].equalsIgnoreCase("in")){
						p.sendMessage("§aStopping writer thread.");
						Main.getDatenServer().getClient().getHandle().unsave().getWriterThread().unsave().getThread().stop();
						p.sendMessage("§aStopped writer thread!");
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