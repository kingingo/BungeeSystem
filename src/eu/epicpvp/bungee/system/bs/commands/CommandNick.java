package eu.epicpvp.bungee.system.bs.commands;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandNick extends Command {

	public CommandNick() {
		super("nick");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!PermissionManager.getManager().hasPermission(sender, "command.nick", true))
			return;
		if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(sender.getName());
			sender.sendMessage("§aYour nickname is §e" + (player.getNickname() == null ? sender.getName() : player.getNickname()));
			sender.sendMessage("§aYou current displayed group is §e" + (player.hasDisplayedGroup() ? player.getDisplayedGroup() : "your current group"));
			return;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(args[2]);
			sender.sendMessage("§aThe player §e" + args[1] + " is nicked as §e" + (player.getNickname() == null ? sender.getName() : player.getNickname()));
			sender.sendMessage("§aThe current displayed group is §e" + (player.hasDisplayedGroup() ? player.getDisplayedGroup() : "his current group"));
			return;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(sender.getName());
			player.setNicknameSync(args[1], "");
			sender.sendMessage("§aYou are now known as §e" + args[1]);
			Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "nick", new DataBuffer().writeInt(0).writeInt(player.getPlayerId()));
			updatePlayer(sender.getName());
			return;
		} else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
			player.setNicknameSync(args[2], "");
			sender.sendMessage("§e" + args[1] + " §ais now known as §e" + args[2]);
			Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "nick", new DataBuffer().writeInt(0).writeInt(player.getPlayerId()));
			updatePlayer(args[1]);
			return;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("setgroup")) {
			if(!args[1].equalsIgnoreCase("null") && PermissionManager.getManager().getGroup(args[1]) == null){
				sender.sendMessage("§cGroup not found!");
				return;
			}
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(sender.getName());
			player.setDisplayedGroup(args[1].equalsIgnoreCase("null") ? null : args[1]);
			sender.sendMessage("§aYour group is now §e" + args[1]);
			sender.sendMessage("§aThe group only updates if your rejoining.");
			Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "nick", new DataBuffer().writeInt(1).writeInt(player.getPlayerId()));
			updatePlayer(sender.getName());
			return;
		} else if (args.length == 3 && args[0].equalsIgnoreCase("setgroup")) {
			if(!args[2].equalsIgnoreCase("null") && PermissionManager.getManager().getGroup(args[2]) == null){
				sender.sendMessage("§cGroup not found!");
				return;
			}
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
			player.setDisplayedGroup(args[2].equalsIgnoreCase("null") ? null : args[2]);
			sender.sendMessage("§e"+args[1]+" §agroup is now §e" + args[2]);
			sender.sendMessage("§aThe group only updates if your rejoining.");
			Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "nick", new DataBuffer().writeInt(1).writeInt(player.getPlayerId()));
			updatePlayer(args[1]);
			return;
		}
	}

	private void updatePlayer(String name){
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "bnick", new DataBuffer().writeString(name));
	}

}
