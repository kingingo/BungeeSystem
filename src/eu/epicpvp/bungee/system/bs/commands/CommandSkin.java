package eu.epicpvp.bungee.system.bs.commands;

import java.util.UUID;

import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import eu.epicpvp.datenserver.definitions.permissions.PermissionType;
import eu.epicpvp.datenserver.definitions.skin.Skin;
import eu.epicpvp.datenserver.definitions.skin.SteveSkin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;

public class CommandSkin extends Command {

	public CommandSkin() {
		super("skin");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if (cs instanceof ConsoleCommandSender)
			return;
		if (!PermissionManager.getManager().hasPermission(cs, PermissionType.SKIN_ADMIN, true))
			return;
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("set")) {
				cs.sendMessage("§aLoading player...");
				UUID skinUUID = null;
				String skinName = null;

				if (args[2].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
					skinUUID = UUID.fromString(args[2]);
				else {
					skinName = args[2];
					if (skinName.length() > 16) {
						cs.sendMessage("§cSkin player name cant be longer than 16.");
						return;
					}
				}

				LoadedPlayer player = null;

				if (args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
					player = Main.getDatenServer().getClient().getPlayerAndLoad(UUID.fromString(args[1]));
				else {
					if (args[1].length() > 16) {
						cs.sendMessage("§cPlayer name cant be longer than 16.");
						return;
					}
					player = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
				}
				cs.sendMessage("§aLoading skin...");
				Skin skin = null;
				if (skinUUID != null)
					skin = Main.getDatenServer().getClient().getSkin(skinUUID).getSync();
				else
					skin = Main.getDatenServer().getClient().getSkin(skinName).getSync();
				cs.sendMessage("§aSetting skin up");
				player.setOwnSkin(skin);
				//player.kickPlayer("§aYour skin was changed.\n§bRejoin for completion.");
				updateSkin(player.getName());
				cs.sendMessage("§aRejoin for successful applay.");
				return;
			}
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("set")) {
				UUID skinUUID = null;
				String skinName = null;

				if (args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
					skinUUID = UUID.fromString(args[1]);
				else {
					skinName = args[1];
					if (skinName.length() > 16) {
						cs.sendMessage("§cSkin player name cant be longer than 16.");
						return;
					}
				}
				cs.sendMessage("§aLoading skin.");
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(cs.getName());
				Skin skin = null;
				if (skinUUID != null)
					skin = Main.getDatenServer().getClient().getSkin(skinUUID).getSync();
				else
					skin = Main.getDatenServer().getClient().getSkin(skinName).getSync();
				player.setOwnSkin(skin);
				//((ProxiedPlayer) cs).disconnect("§aYour skin has changed.Rejoin for successful applay.");
				updateSkin(player.getName());
				return;
			}
			if (args[0].equalsIgnoreCase("reset")) {
				LoadedPlayer player = null;

				if (args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
					player = Main.getDatenServer().getClient().getPlayerAndLoad(UUID.fromString(args[1]));
				else {
					if (args[1].length() > 16) {
						cs.sendMessage("§cPlayer name cant be longer than 16.");
						return;
					}
					player = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
				}
				player.setOwnSkin(null);
				cs.sendMessage("§aYou set the skin of the player §e"+args[1]+"§a to default.");
				//player.kickPlayer("§aYour skin has changed.Rejoin for successful applay.");
				updateSkin(player.getName());
				return;
			}
			if(args[0].equalsIgnoreCase("info")){
				cs.sendMessage("§aLoading player...");
				LoadedPlayer player = null;

				if (args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
					player = Main.getDatenServer().getClient().getPlayerAndLoad(UUID.fromString(args[1]));
				else {
					if (args[1].length() > 16) {
						cs.sendMessage("§cPlayer name cant be longer than 16.");
						return;
					}
					player = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
				}
				cs.sendMessage("§aLoading skin data...");
				Skin skin = player.getOwnSkin().getSync();
				cs.sendMessage("§6Skin infotmations about §e"+player.getName());
				if(skin instanceof SteveSkin){
					cs.sendMessage("§aThis player use his default skin.");
				}
				else
				{
					cs.sendMessage("§aThis player has a costum skin.");
					cs.sendMessage("§aSkin owner: "+skin.getProfileName()+"("+skin.getUUID()+")");
					cs.sendMessage("§aSkin URL: "+skin.getSkinUrl());
				}
				return;
			}
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reset")) {
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(cs.getName());
				player.setOwnSkin(null);
				cs.sendMessage("§aYou set your skin to default");
				updateSkin(player.getName());
				//((ProxiedPlayer) cs).disconnect("§aYour skin has changed.Rejoin for successful applay.");
				return;
			}
		}
		cs.sendMessage("§6/skin set <UUID/Name> §7| §aSet your skin");
		cs.sendMessage("§6/skin set <Player> <UUID/Name> §7| §aSet the skin for an other player");
		cs.sendMessage("§6/skin info <Player> §7| §aGetting skin informations from a player.");
		cs.sendMessage("§6/skin reset §7| §aReset your skin");
		cs.sendMessage("§6/skin reset <Player> §7| §aReset a player skin");
	}

	public void updateSkin(String player){
		Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "skin", new DataBuffer().writeByte(0).writeString(player)); //0 for update
	}
}
