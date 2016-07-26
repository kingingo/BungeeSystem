package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.permission.PermissionManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandNick extends Command{

	public CommandNick() {
		super("nick");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!PermissionManager.getManager().hasPermission(sender, "command.nick", true))
			return;
		if(args.length == 1 && args[0].equalsIgnoreCase("info")){
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(sender.getName());
			sender.sendMessage("§aYour nickname is §e"+(player.getNickname() == null ? sender.getName() : player.getNickname()));
			return;
		}else if(args.length == 2 && args[0].equalsIgnoreCase("info")){
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(args[2]);
			sender.sendMessage("§aThe player §e"+args[1]+" is nicked as §e"+(player.getNickname() == null ? sender.getName() : player.getNickname()));
			return;
		}else if(args.length == 2 && args[0].equalsIgnoreCase("set")){
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(sender.getName());
			player.setNicknameSync(args[1]);
			sender.sendMessage("§aYou are now known as §e"+args[1]);
			Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "nick", new DataBuffer().writeInt(0).writeInt(player.getPlayerId()));
			return;
		}
	}

}
