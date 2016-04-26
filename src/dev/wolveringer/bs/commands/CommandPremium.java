package dev.wolveringer.bs.commands;

import dev.wolveringer.bs.Main;
import dev.wolveringer.bs.client.event.ServerMessageEvent;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import me.kingingo.kBungeeCord.Permission.PermissionManager;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandPremium extends Command implements Listener {

	public CommandPremium(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;

			if (args.length == 0) {
				if(p.getPendingConnection().isOnlineMode()){
					sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.premium.alredyPremium", sender));
					return;
				}
				for(String message : Main.getTranslationManager().translate("command.premium.warn", sender).split(";"))
					p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ message);
				return;
			}

			if (args[0].equalsIgnoreCase("on")) {
				if (args.length == 1) {
					if(p.getPendingConnection().isOnlineMode()){
						sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.premium.alredyPremium", sender));
						return;
					}
					LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(p.getUniqueId());
					if (!target.isPremiumSync()) {
						p.disconnect(Main.getTranslationManager().translate("command.premium.kickMessage", sender));
						target.setPremiumSync(true);
					} else
						sender.sendMessage(Main.getTranslationManager().translate("prefix", sender)+Main.getTranslationManager().translate("command.premium.alredyPremium", sender));
				}
				else if(args.length == 2){
					if(PermissionManager.getManager().hasPermission(p, PermissionType.PREMIUM_TOGGLE, true)){
						LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
						sender.sendMessage("§aChecking user details");
						if (!target.isPremiumSync()) {
							p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ Main.getTranslationManager().translate("command.premium.enable.other", sender,args[0])); //
							if (!target.isLoaded())
								target.loadPlayer();
							target.setPremiumSync(false);
							if(target.getServer().getSync() != null)
								if (BungeeCord.getInstance().getPlayer(args[1]) != null) {
									BungeeCord.getInstance().getPlayer(args[1]).disconnect(Main.getTranslationManager().translate("command.premium.kickMessage", target));
								} else {
									Main.getDatenServer().getClient().kickPlayer(target.getPlayerId(), Main.getTranslationManager().translate("command.premium.kickMessage", target)).getSync();
								}
							Main.getDatenServer().getClient().clearCacheForPlayer(target);
							Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "playerpremium", new DataBuffer().writeString(args[1]));
						} else
							sender.sendMessage("§cThe user is alredy premium");
					}
				}
			} else if (args[0].equalsIgnoreCase("off") && args.length == 2) {
				if (PermissionManager.getManager().hasPermission(p, PermissionType.PREMIUM_TOGGLE, true)) {
					LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
					sender.sendMessage("§aChecking user details");
					if (target.isPremiumSync()) {
						p.sendMessage(Main.getTranslationManager().translate("prefix", sender)+ Main.getTranslationManager().translate("command.premium.disabled.other", sender,args[0])); //
						if (!target.isLoaded())
							target.loadPlayer();
						target.setPremiumSync(false);
						if (BungeeCord.getInstance().getPlayer(args[1]) != null) {
							BungeeCord.getInstance().getPlayer(args[1]).disconnect(Main.getTranslationManager().translate("command.premium.kickMessage", target));
						} else {
							Main.getDatenServer().getClient().kickPlayer(target.getPlayerId(), Main.getTranslationManager().translate("command.premium.kickMessage", target)).getSync();
						}
						Main.getDatenServer().getClient().clearCacheForPlayer(target);
						Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "playerpremium", new DataBuffer().writeString(args[1]));
					} else
						sender.sendMessage("§cThe user isnt premium");
				}
			} else if (args[0].equalsIgnoreCase("check") && args.length == 2) {
				if (PermissionManager.getManager().hasPermission(p, PermissionType.PREMIUM_CHECK, true)) {
					LoadedPlayer target = Main.getDatenServer().getClient().getPlayerAndLoad(args[1]);
					if (target.isPremiumSync()) {
						sender.sendMessage("§aThis user is a §npremium§a user!");
					} else
						sender.sendMessage("§aThis user is a §ncracked§a auser!");
				}
			} else {
				sender.sendMessage("§a/premium on §7| §aSet you to a premium player!");
				if (PermissionManager.getManager().hasPermission(p, PermissionType.PREMIUM_TOGGLE, false)) {
					sender.sendMessage("§a/premium off <name> §7| §aSet you somebody to a cracked player!");
					sender.sendMessage("§a/premium on <name> §7| §aSet you somebody to a premium player!");
				}
				if (PermissionManager.getManager().hasPermission(p, PermissionType.PREMIUM_CHECK, false)) {
					sender.sendMessage("§a/premium check <name> §7| §aCheck premium state from a player!");
				}
			}
		}
	}

	@EventHandler
	public void a(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("playerpremium")) {
			Main.getDatenServer().getClient().clearCacheForPlayer(Main.getDatenServer().getClient().getPlayer(e.getBuffer().readString()));
		}
	}
}
//command.premium.warn - §4§lWARNING! §cIf you proceed you will be unable to use a cracked minecraft version<br>§7Please think carefully about this step and<br>§7only continue if you bought the game at<br>§eminecraft.net§7! §4You have been warned!<br>§aProceed §7(§cat your own risk!§7): §e§l/premium on
//command.premium.alredyPremium - §cYou are alredy on premium.
//command.premium.disabled.other - §c%s0 §ePremium-login§c was deactivated! [playerName]
//command.premium.kickMessage - §aYour account have been set to premium.\nPlease rejoin to success.
