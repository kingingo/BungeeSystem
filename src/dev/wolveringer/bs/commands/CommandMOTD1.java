package dev.wolveringer.bs.commands;

import lombok.Getter;
import me.kingingo.kBungeeCord.kBungeeCord;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Packet.Events.PacketReceiveEvent;
import me.kingingo.kBungeeCord.Packet.Packets.BG_SET_MOTD;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandMOTD1 extends Command implements Listener {

	@Getter
	private kBungeeCord instance;
	
	public CommandMOTD1(String name,kBungeeCord instance) {
		super(name);
		this.instance=instance;
		BungeeCord.getInstance().getPluginManager().registerListener(instance, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer))return;
		ProxiedPlayer player =(ProxiedPlayer)sender;
		if (getInstance().getPermManager().hasPermission(player, PermissionType.MOTD,true)) {
			if (args.length > 0) {
				String motd = "";
				for (String s : args)
					motd += s + " ";
				motd = motd.substring(0, motd.length() - 1);
				getInstance().getPacketManager().SendPacket("BG", new BG_SET_MOTD(motd, getInstance().getMotd2()));
				motd = motd.replace("&", "§");
				getInstance().setMotd1(motd);
				sender.sendMessage(Language.getText(player, "PREFIX")+"§aDie MOTD-1 Wurde Zu : §c" + motd+ "§r §aGeändert!");
			} else {
				sender.sendMessage("§eFalscher Befehls Syntax: §c\"/"
						+ getName() + "\" §eRichtiger Syntax: §a\"/"
						+ getName() + " [MOTD] \" ");
			}

		}
	}
	
	@EventHandler
	public void MOTD(PacketReceiveEvent ev){
		if(ev.getPacket() instanceof BG_SET_MOTD){
			BG_SET_MOTD packet = (BG_SET_MOTD)ev.getPacket();
		
			getInstance().setMotd1(packet.getMotd1().replace("&", "§"));
			getInstance().setMotd2(packet.getMotd2().replace("&", "§"));
		}
	}

}
