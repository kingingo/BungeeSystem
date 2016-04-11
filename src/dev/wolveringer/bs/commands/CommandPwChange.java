package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.client.LoadedPlayer;
import me.kingingo.kBungeeCord.Language.Language;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandPwChange extends Command {

	public CommandPwChange(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if(args.length != 2){
			p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.help", sender));
		}
		if(args.length == 2){
			String old = args[0];
			String newpw = args[1];
			
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(p.getUniqueId());
			if(p.getPendingConnection().isOnlineMode()){
				sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.error.premium", sender));
				return;
			}
			if(!old.equals(player.getPasswordSync())){
				p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.error.oldNotMatch", sender));
				return;
			}
			player.setPasswordSync(newpw);
			p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.pwchange.changed", sender));
		}
	}

}
//command.pwchange.help - /pwchange §7[§4Altes Password§7] §7[§aNeues Password§7]
//command.pwchange.error.premium - §cYou are on premium. You cant change your password!
//command.pwchange.error.oldNotMatch - §cOld password dont match!
//command.pwchange.changed - §aYou changed your password.