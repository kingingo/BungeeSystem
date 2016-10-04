package eu.epicpvp.bungee.system.bs.commands;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.listener.WarzTexturePackListener;
import eu.epicpvp.bungee.system.bs.packets.PacketPlayOutResourcepack;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandResourcepack extends Command{

	static {
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.resourcepack.error.cant_reset_on_warz", "§cYou can't reset your warz resourcepack while you're playing on warz.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.resourcepack.error.alredy_reseted", "§cYour WarZ resourcepack has already been resetted.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.resourcepack.info.reset", "§aYour resourcepack will be resetted.");

		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.resourcepack.error.not_on_warz", "§cYou war not on the WarZ server.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.resourcepack.error.alredy_applayed", "§cYou alredy playing with the resourcepack.");
		Main.getTranslationManager().registerFallback(LanguageType.ENGLISH, "command.resourcepack.info.applay", "§aThe resourcepack will be applayed.");
	}

	public CommandResourcepack() {
		super("resourcepack",null,"rs");
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		if(!(cs instanceof Player))
			return;
		if(args.length == 1){
			if(args[0].equalsIgnoreCase("reset")){
				if(((Player)cs).getServer().getInfo().getName().equalsIgnoreCase("warz")){
					cs.sendMessage(Main.getTranslationManager().translate("command.resourcepack.error.cant_reset_on_warz", cs));
					return;
				}
				if(!WarzTexturePackListener.getInstance().textureUsing.contains((Player) cs)){
					cs.sendMessage(Main.getTranslationManager().translate("command.resourcepack.error.alredy_reseted", cs));
					return;
				}
				cs.sendMessage(Main.getTranslationManager().translate("command.resourcepack.info.reset", cs));
				((Player)cs).sendPacket(new PacketPlayOutResourcepack(WarzTexturePackListener.DEFAULT_URL, WarzTexturePackListener.DEFAULT_HASH));
				return;
			}
			else if(args[0].equalsIgnoreCase("applay")){
				if(!((Player)cs).getServer().getInfo().getName().equalsIgnoreCase("warz")){
					cs.sendMessage(Main.getTranslationManager().translate("command.resourcepack.error.not_on_warz", cs));
					return;
				}
				if(WarzTexturePackListener.getInstance().textureUsing.contains((Player) cs)){
					cs.sendMessage(Main.getTranslationManager().translate("command.resourcepack.error.alredy_applayed", cs));
					return;
				}
				cs.sendMessage(Main.getTranslationManager().translate("command.resourcepack.info.applay", cs));
				((Player)cs).sendPacket(new PacketPlayOutResourcepack(WarzTexturePackListener.WARZ_URL, WarzTexturePackListener.WARZ_HASH));
				return;
			}
		}
	}

}
