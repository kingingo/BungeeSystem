package dev.wolveringer.bs.commands;

import java.util.UUID;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.skin.Skin;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;

public class CommandSkin extends Command{
	
	public CommandSkin() {
		super("skin");
	}
	
	@Override
	public void execute(CommandSender cs, String[] args) {
		if(cs instanceof ConsoleCommandSender) return;
		if(!PermissionManager.getManager().hasPermission(cs, PermissionType.SKIN_ADMIN,true)) return;
		
		if(args.length == 2){
			if(args[0].equalsIgnoreCase("set")){
				UUID skinUUID = null;
				String skinName = null;
				
				if(args[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
					skinUUID = UUID.fromString(args[1]);
				else{
					skinName = args[1];
					if(skinName.length()>16){
						cs.sendMessage("§cSkin player name cant be longer than 16.");
						return;
					}
				}
				cs.sendMessage("§aLoading skin.");
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(cs.getName());
				Skin skin = null;
				if(skinUUID != null)
					skin = Main.getDatenServer().getClient().getSkin(skinUUID).getSync();
				else
					skin = Main.getDatenServer().getClient().getSkin(skinName).getSync();
				player.setOwnSkin(skin);
				((ProxiedPlayer)cs).disconnect("§aRejoin for successful applay.");
				return;
			}
		}
		else if(args.length == 1){
			if(args[0].equalsIgnoreCase("reset")){
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(cs.getName());
				player.setOwnSkin(null);
				cs.sendMessage("§aYour skin is now youre default skin");
				return;
			}
		}
		cs.sendMessage("§6/skin set <UUID/Name> §7| §aSet your skin");
		cs.sendMessage("§6/skin reset §7| §aReset your skin");
	}
}
