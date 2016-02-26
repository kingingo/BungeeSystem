package dev.wolveringer.bs.commands;

import lombok.Getter;
import me.kingingo.kBungeeCord.kBungeeCord;
import me.kingingo.kBungeeCord.Packet.Packets.BG_SET_MOTD;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandMOTD2 extends Command {

	@Getter
	private kBungeeCord instance;
	
	public CommandMOTD2(String name,kBungeeCord instance) {
		super(name);
		this.instance=instance;
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
				getInstance().getPacketManager().SendPacket("BG", new BG_SET_MOTD(getInstance().getMotd1(),motd));
				motd = motd.replace("&", "�");
				getInstance().setMotd2(motd);
				sender.sendMessage("�aDie MOTD-2 Wurde Zu : �c" + motd+ "�r �aGe�ndert!");
			} else {
				sender.sendMessage("�eFalscher Befehls Syntax: �c\"/"
						+ getName() + "\" �eRichtiger Syntax: �a\"/"
						+ getName() + " [MOTD] \" ");
			}

		}
	}

}